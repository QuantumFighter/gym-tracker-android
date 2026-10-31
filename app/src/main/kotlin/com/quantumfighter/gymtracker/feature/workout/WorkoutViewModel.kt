package com.quantumfighter.gymtracker.feature.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quantumfighter.gymtracker.data.workout.WorkoutGraph
import com.quantumfighter.gymtracker.data.workout.WorkoutRepository
import com.quantumfighter.gymtracker.domain.LoadInput
import com.quantumfighter.gymtracker.domain.SaveSetCommand
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

internal data class SetDraft(val weight: String = "", val reps: String = "", val type: String = "WORKING", val commandId: String = UUID.randomUUID().toString())
internal data class WorkoutUiState(val workout: WorkoutGraph? = null, val loading: Boolean = true, val loadFailed: Boolean = false, val saving: Boolean = false, val selected: String? = null, val drafts: Map<String, SetDraft> = emptyMap(), val error: String? = null, val acknowledgement: String? = null, val retryLabel: String? = null)

/** Drafts survive navigation in this ViewModel; only Room records are authoritative after process death. */
internal class WorkoutViewModel(private val repository: WorkoutRepository) : ViewModel() {
    private val state = MutableStateFlow(WorkoutUiState())
    val uiState = state.asStateFlow()
    private var observer: Job? = null
    private var retryAction: (() -> Unit)? = null

    fun retryCommand() {
        retryAction?.invoke()
    }

    init {
        retry()
    }

    fun retry() {
        if (observer?.isActive == true) return
        state.value = state.value.copy(loading = true, loadFailed = false)
        observer = viewModelScope.launch {
            try {
                repository.observeActive().collect { state.value = state.value.copy(workout = it, loading = false, loadFailed = false) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                state.value = state.value.copy(loading = false, loadFailed = true)
            }
        }
    }

    fun start(membershipId: String) = command("Could not start workout.", "Retry Start Workout") { repository.start(membershipId) }
    fun warmup(start: Boolean) = command(if (start) "Could not start warm-up." else "Could not stop warm-up.", if (start) "Retry Start Warm-up" else "Retry Stop Warm-up") { repository.warmup(start) }
    fun startRest() {
        val id = state.value.selected ?: return
        command("Could not start rest.", "Retry Start Rest") { repository.startRest(id) }
    }
    fun select(id: String?) {
        if (state.value.saving) return
        retryAction = null
        val current = state.value
        val exercise = current.workout?.exercises?.find { it.exercise.id == id }
        val target = exercise?.prescriptions?.minByOrNull { it.ordinal }
        val drafts = if (id != null && id !in current.drafts) current.drafts + (id to SetDraft(target?.loadGrams?.let(LoadInput::formatKg).orEmpty(), target?.repMin?.toString().orEmpty())) else current.drafts
        state.value = current.copy(selected = id, drafts = drafts, error = null, acknowledgement = null, retryLabel = null)
    }

    fun edit(draft: SetDraft) {
        val current = state.value
        val id = current.selected ?: return
        if (current.saving) return
        retryAction = null
        state.value = current.copy(drafts = current.drafts + (id to draft.copy(commandId = UUID.randomUUID().toString())), error = null, acknowledgement = null, retryLabel = null)
    }

    fun save() {
        val current = state.value
        val id = current.selected ?: return
        val draft = current.drafts[id] ?: return
        command("Could not save set. Your draft is still here.", "Retry Save Set") {
            val payload = SaveSetCommand(draft.commandId, id, LoadInput.parseKg(draft.weight), draft.reps.toIntOrNull() ?: throw IllegalArgumentException("Enter positive whole repetitions."), draft.type)
            repository.save(payload)
            state.value = state.value.copy(drafts = state.value.drafts + (id to draft.copy(commandId = UUID.randomUUID().toString())), acknowledgement = "Set saved")
        }
    }

    private fun command(failureMessage: String, retryLabel: String, action: suspend () -> Unit) {
        if (state.value.saving) return
        retryAction = null
        state.value = state.value.copy(saving = true, error = null, acknowledgement = null, retryLabel = null)
        viewModelScope.launch {
            try {
                action()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (invalid: IllegalArgumentException) {
                state.value = state.value.copy(error = invalid.message ?: "Check the entered values.")
            } catch (_: Exception) {
                retryAction = { command(failureMessage, retryLabel, action) }
                state.value = state.value.copy(error = failureMessage, retryLabel = retryLabel)
            } finally {
                state.value = state.value.copy(saving = false)
            }
        }
    }
}
