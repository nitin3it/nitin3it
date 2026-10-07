package com.nitin3it.kidsafe.ui.child

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nitin3it.kidsafe.data.ProfileRepository
import com.nitin3it.kidsafe.data.model.DailyUsage
import com.nitin3it.kidsafe.data.model.UserProfile
import com.nitin3it.kidsafe.usage.UsageCollector
import com.nitin3it.kidsafe.usage.UsageSyncer
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChildUiState(
    val profile: UserProfile,
    val hasPermission: Boolean = false,
    val syncing: Boolean = false,
    val today: DailyUsage? = null,
    val message: String? = null,
)

class ChildViewModel(app: Application, profile: UserProfile) : AndroidViewModel(app) {

    private val collector = UsageCollector(app)
    private val syncer = UsageSyncer(app, collector)

    private val _state = MutableStateFlow(ChildUiState(profile))
    val state: StateFlow<ChildUiState> = _state.asStateFlow()

    init {
        // Keep lastSyncAt up to date, including syncs done by the background worker.
        viewModelScope.launch {
            ProfileRepository().observe(profile.id)
                .catch { /* signed out or offline: keep the last known profile */ }
                .collect { updated ->
                    if (updated != null) _state.update { it.copy(profile = updated) }
                }
        }
    }

    /** Called whenever the screen resumes, e.g. after returning from the usage-access settings. */
    fun onResume() {
        val granted = collector.hasPermission()
        _state.update { it.copy(hasPermission = granted) }
        if (granted) syncNow()
    }

    fun syncNow() {
        if (_state.value.syncing) return
        viewModelScope.launch {
            _state.update { it.copy(syncing = true, message = null) }
            val days = try {
                syncer.collect()
            } catch (e: Exception) {
                _state.update { it.copy(syncing = false, message = e.localizedMessage ?: "Could not read usage") }
                return@launch
            }
            _state.update { it.copy(today = days.last()) }
            val message = try {
                syncer.upload(_state.value.profile.id, days)
                null
            } catch (_: TimeoutCancellationException) {
                "You're offline — usage is saved and will upload automatically."
            } catch (e: Exception) {
                e.localizedMessage ?: "Sync failed"
            }
            _state.update { it.copy(syncing = false, message = message) }
        }
    }
}
