package com.nitin3it.kidsafe.ui.parent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nitin3it.kidsafe.data.ProfileRepository
import com.nitin3it.kidsafe.data.PublicIds
import com.nitin3it.kidsafe.data.UsageRepository
import com.nitin3it.kidsafe.data.model.DailyUsage
import com.nitin3it.kidsafe.data.model.UserProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ParentUiState(
    val parent: UserProfile,
    val loadingChildren: Boolean = true,
    val children: List<UserProfile> = emptyList(),
    val selectedChild: UserProfile? = null,
    val selectedDate: LocalDate = LocalDate.now(),
    /** The last 7 days, oldest first; days with no data have zero usage. */
    val week: List<DailyUsage> = emptyList(),
    val error: String? = null,
) {
    val selectedDay: DailyUsage? get() = week.firstOrNull { it.date == selectedDate.toString() }
}

data class AddChildState(val open: Boolean = false, val busy: Boolean = false, val error: String? = null)

@OptIn(ExperimentalCoroutinesApi::class)
class ParentViewModel(parent: UserProfile) : ViewModel() {

    private val profiles = ProfileRepository()
    private val usage = UsageRepository()

    private val parentFlow = profiles.observe(parent.id).map { it ?: parent }.onStart { emit(parent) }
    private val selectedChildId = MutableStateFlow<String?>(null)
    private val selectedDate = MutableStateFlow(LocalDate.now())

    private val _addChild = MutableStateFlow(AddChildState())
    val addChild: StateFlow<AddChildState> = _addChild.asStateFlow()

    private val childrenFlow = parentFlow
        .map { it.linkedChildren }
        .distinctUntilChanged()
        .flatMapLatest { profiles.observeAll(it) }

    private val weekFlow = selectedChildId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else usage.observeRecent(id).map(::fillWeek)
    }.catch { emit(emptyList()) }

    val state: StateFlow<ParentUiState> = combine(
        parentFlow, childrenFlow, selectedChildId, selectedDate, weekFlow,
    ) { p, children, childId, date, week ->
        ParentUiState(
            parent = p,
            loadingChildren = false,
            children = children,
            selectedChild = children.firstOrNull { it.id == childId } ?: children.firstOrNull(),
            selectedDate = date,
            week = week,
        )
    }
        .catch { e -> emit(ParentUiState(parent, loadingChildren = false, error = e.localizedMessage)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ParentUiState(parent))

    init {
        // Default to the first linked child once the list arrives.
        viewModelScope.launch {
            childrenFlow.catch { emit(emptyList()) }.collect { children ->
                if (selectedChildId.value !in children.map { it.id }) {
                    selectedChildId.value = children.firstOrNull()?.id
                }
            }
        }
    }

    fun selectChild(id: String) {
        selectedChildId.value = id
        selectedDate.value = LocalDate.now()
    }

    fun selectDate(date: LocalDate) {
        selectedDate.value = date
    }

    fun openAddChild() = _addChild.update { AddChildState(open = true) }
    fun closeAddChild() = _addChild.update { AddChildState() }

    fun addChild(code: String) {
        val normalized = PublicIds.normalize(code)
        if (normalized.length != 9) {
            _addChild.update { it.copy(error = "Child IDs look like K7QM-29TX") }
            return
        }
        viewModelScope.launch {
            _addChild.update { it.copy(busy = true, error = null) }
            try {
                val child = profiles.findChildByPublicId(normalized)
                val parent = state.value.parent
                when {
                    child == null -> _addChild.update {
                        it.copy(busy = false, error = "No child found with ID $normalized. Check the ID on your child's phone.")
                    }
                    child.id in parent.linkedChildren -> _addChild.update {
                        it.copy(busy = false, error = "${child.displayName} is already added.")
                    }
                    else -> {
                        profiles.linkChild(parent.id, child.id)
                        selectChild(child.id)
                        _addChild.update { AddChildState() }
                    }
                }
            } catch (e: Exception) {
                _addChild.update { it.copy(busy = false, error = e.localizedMessage ?: "Could not add child") }
            }
        }
    }

    fun removeChild(childId: String) {
        viewModelScope.launch {
            runCatching { profiles.unlinkChild(state.value.parent.id, childId) }
        }
    }

    private fun fillWeek(days: List<DailyUsage>): List<DailyUsage> {
        val byDate = days.associateBy { it.date }
        val today = LocalDate.now()
        return (6 downTo 0).map { offset ->
            val date = today.minusDays(offset.toLong()).toString()
            byDate[date] ?: DailyUsage(date = date)
        }
    }
}
