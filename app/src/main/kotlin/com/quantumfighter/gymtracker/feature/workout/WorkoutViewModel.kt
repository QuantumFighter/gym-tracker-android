package com.quantumfighter.gymtracker.feature.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quantumfighter.gymtracker.data.workout.RecordedSetEntity
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
internal data class WorkoutUiState(val workout: WorkoutGraph? = null, val loading: Boolean = true, val loadFailed: Boolean = false, val saving: Boolean = false, val selected: String? = null, val drafts: Map<String, SetDraft> = emptyMap(), val error: String? = null, val acknowledgement: String? = null, val retryLabel: String? = null, val confirmWarmdown: Boolean = false, val editing: RecordedSetEntity? = null, val correction: SetDraft? = null, val finishedId: String? = null)

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

    fun start(membershipId: String) = command("Could not start workout.", "Retry Start Workout") {
        repository.start(membershipId)
        state.value = state.value.copy(finishedId = null, selected = null, drafts = emptyMap())
    }
    fun complete() {
        val current = state.value
        val session = current.workout?.session?.id ?: return
        val exercise = current.selected ?: return
        command("Could not complete exercise.", "Retry Complete Exercise") {
            repository.complete(session, exercise)
            state.value = state.value.copy(selected = null)
        }
    }
    fun requestWarmdown() {
        if (state.value.saving) return
        if (state.value.workout?.exercises?.any { it.exercise.status != "COMPLETE" } == true) {
            state.value = state.value.copy(confirmWarmdown = true)
        } else {
            enterWarmdown(false)
        }
    }
    fun cancelWarmdown() {
        state.value = state.value.copy(confirmWarmdown = false)
    }
    fun enterWarmdown(confirmed: Boolean) {
        val id = state.value.workout?.session?.id ?: return
        command("Could not enter warm-down.", "Retry Warm Down") {
            repository.warmdown(id, confirmed)
            state.value = state.value.copy(confirmWarmdown = false, selected = null, editing = null)
        }
    }
    fun returnToRecording() {
        val id = state.value.workout?.session?.id ?: return
        command("Could not return to recording.", "Retry Return to Recording") {
            repository.returnToRecording(id)
            state.value = state.value.copy(selected = null)
        }
    }
    fun warmdownTimer(start: Boolean) {
        val id = state.value.workout?.session?.id ?: return
        command("Could not update warm-down timer.", "Retry Warm-down Timer") { repository.warmdownTimer(id, start) }
    }
    fun finish() {
        val id = state.value.workout?.session?.id ?: return
        command("Could not finish workout. Your saved sets are retained.", "Retry Finish Workout") {
            repository.finish(id)
            state.value = state.value.copy(finishedId = id, selected = null, editing = null, acknowledgement = "Workout finished")
        }
    }
    fun abandon() {
        val id = state.value.workout?.session?.id ?: return
        command("Could not abandon workout.", "Retry Abandon Workout") {
            repository.abandon(id)
            state.value = state.value.copy(selected = null, acknowledgement = "Workout abandoned. Saved sets retained.")
        }
    }
    fun editSet(set: RecordedSetEntity?) {
        if (state.value.saving) return
        state.value = state.value.copy(editing = set, correction = set?.let { SetDraft(it.loadGrams?.let(LoadInput::formatKg).orEmpty(), it.repetitions.toString(), it.setType) }, error = null, retryLabel = null)
        retryAction = null
    }
    fun editCorrection(draft: SetDraft) {
        if (state.value.saving) return
        state.value = state.value.copy(correction = draft, error = null, retryLabel = null)
        retryAction = null
    }
    fun correct() {
        val original = state.value.editing ?: return
        val draft = state.value.correction ?: return
        command("Could not correct set. Previous saved values are retained.", "Retry Correction") {
            repository.correct(original, LoadInput.parseKg(draft.weight), draft.reps.toIntOrNull() ?: throw IllegalArgumentException("Enter positive whole repetitions."), draft.type)
            state.value = state.value.copy(editing = null, correction = null, acknowledgement = "Correction saved")
        }
    }
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
