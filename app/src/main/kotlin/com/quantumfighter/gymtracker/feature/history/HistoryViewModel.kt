package com.quantumfighter.gymtracker.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quantumfighter.gymtracker.data.workout.WorkoutGraph
import com.quantumfighter.gymtracker.data.workout.WorkoutRepository
import com.quantumfighter.gymtracker.data.workout.WorkoutSessionEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal data class HistoryUiState(val sessions: List<WorkoutSessionEntity> = emptyList(), val selected: String? = null, val detail: WorkoutGraph? = null, val loading: Boolean = true, val failed: Boolean = false)

/** Read-only queries use only session-owned records, including names, targets and units. */
internal class HistoryViewModel(private val repository: WorkoutRepository) : ViewModel() {
    private val state = MutableStateFlow(HistoryUiState())
    val uiState = state.asStateFlow()
    private var observer: Job? = null
    init {
        select(null)
    }

    fun select(id: String?) {
        observer?.cancel()
        state.value = state.value.copy(selected = id, detail = null, loading = true, failed = false)
        observer = viewModelScope.launch {
            try {
                if (id == null) {
                    repository.observeHistory().collect { state.value = state.value.copy(sessions = it, loading = false) }
                } else {
                    repository.observeFinished(id).collect { state.value = state.value.copy(detail = it, loading = false) }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                state.value = state.value.copy(loading = false, failed = true)
            }
        }
    }
    fun retry() {
        select(state.value.selected)
    }
}
