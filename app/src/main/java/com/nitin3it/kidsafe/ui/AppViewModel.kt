package com.nitin3it.kidsafe.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nitin3it.kidsafe.KidSafeApp
import com.nitin3it.kidsafe.data.AuthRepository
import com.nitin3it.kidsafe.data.ProfileRepository
import com.nitin3it.kidsafe.data.RolePreferences
import com.nitin3it.kidsafe.data.model.Role
import com.nitin3it.kidsafe.data.model.UserProfile
import com.nitin3it.kidsafe.usage.UsageSyncWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AppState {
    data object Loading : AppState
    data object FirebaseNotConfigured : AppState
    data object ChooseRole : AppState
    data class SignIn(val role: Role) : AppState
    data class Ready(val profile: UserProfile, val role: Role) : AppState
    data class Error(val message: String) : AppState
}

/** Decides which top-level screen to show: role picker → login → child/parent home. */
class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = RolePreferences(app)
    private val configured = KidSafeApp.isFirebaseConfigured(app)
    private val auth by lazy { AuthRepository() }
    private val profiles by lazy { ProfileRepository() }

    private val _state = MutableStateFlow<AppState>(AppState.Loading)
    val state: StateFlow<AppState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        if (!configured) {
            _state.value = AppState.FirebaseNotConfigured
            return
        }
        val role = prefs.role ?: run {
            _state.value = AppState.ChooseRole
            return
        }
        val user = auth.currentUser ?: run {
            _state.value = AppState.SignIn(role)
            return
        }
        _state.value = AppState.Loading
        viewModelScope.launch {
            _state.value = try {
                val profile = profiles.getOrCreate(user, role)
                if (role == Role.CHILD) UsageSyncWorker.schedule(getApplication())
                AppState.Ready(profile, role)
            } catch (e: Exception) {
                AppState.Error(e.localizedMessage ?: "Could not load your profile")
            }
        }
    }

    fun chooseRole(role: Role) {
        prefs.role = role
        refresh()
    }

    /** Back from the login screen to the role picker. */
    fun changeRole() {
        prefs.role = null
        refresh()
    }

    fun signOut() {
        viewModelScope.launch {
            UsageSyncWorker.cancel(getApplication())
            auth.signOut(getApplication())
            prefs.role = null
            refresh()
        }
    }
}
