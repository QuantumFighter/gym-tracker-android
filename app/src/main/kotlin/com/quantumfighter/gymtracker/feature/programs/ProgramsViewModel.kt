package com.quantumfighter.gymtracker.feature.programs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quantumfighter.gymtracker.data.exercise.ExerciseRepository
import com.quantumfighter.gymtracker.data.program.ProgramRepository
import com.quantumfighter.gymtracker.domain.Exercise
import com.quantumfighter.gymtracker.domain.LoadInput
import com.quantumfighter.gymtracker.domain.ProgramLibrary
import com.quantumfighter.gymtracker.domain.RoutineExerciseDraft
import com.quantumfighter.gymtracker.domain.SetTarget
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

/** Text drafts deliberately retain malformed input so validation never silently invents targets. */
internal data class SetDraft(val min: String = "8", val max: String = "12", val kg: String = "", val rest: String = "90") {
    fun target(): SetTarget = SetTarget(
        requireNotNull(min.trim().toIntOrNull()) { "Minimum reps must be a whole number." },
        requireNotNull(max.trim().toIntOrNull()) { "Maximum reps must be a whole number." },
        LoadInput.parseKg(kg),
        requireNotNull(rest.trim().toIntOrNull()) { "Rest must be whole seconds." },
    )
}

internal data class ExerciseDraft(val id: String = UUID.randomUUID().toString(), val exercise: Exercise, val sets: List<SetDraft> = listOf(SetDraft()))

internal data class ProgramsUiState(
    val library: ProgramLibrary = ProgramLibrary(),
    val catalogue: List<Exercise> = emptyList(),
    val loading: Boolean = true,
    val loadFailed: Boolean = false,
    val saving: Boolean = false,
    val error: String? = null,
    val creatingProgram: Boolean = false,
    val programName: String = "",
    val routineProgramId: String? = null,
    val routineName: String = "",
    val draft: List<ExerciseDraft> = emptyList(),
    val choosingExercise: Boolean = false,
    val detailMembershipId: String? = null,
)

/**
 * Owns best-effort creation drafts; saved plans are always read back from Room.
 * @param library Restartable observation, defaulting to the repository's cold Room flow.
 */
internal class ProgramsViewModel(
    private val programs: ProgramRepository,
    private val exercises: ExerciseRepository,
    private val library: Flow<ProgramLibrary> = programs.observeLibrary(),
) : ViewModel() {
    private val mutable = MutableStateFlow(ProgramsUiState())
    private var observationJob: Job? = null
    val state = mutable.asStateFlow()

    init {
        load()
    }

    /** Starts one observer, or retries a terminated one, retaining drafts and save/validation errors. */
    fun load() {
        if (observationJob?.isActive == true) return
        mutable.update { it.copy(loading = true, loadFailed = false) }
        observationJob = viewModelScope.launch {
            try {
                val catalogue = exercises.recordableExercises()
                library.collect { library ->
                    mutable.update { it.copy(library = library, catalogue = catalogue, loading = false, loadFailed = false) }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutable.update { it.copy(loading = false, loadFailed = true) }
            }
        }
    }

    fun newProgram() = edit { it.copy(creatingProgram = true, programName = "", error = null) }
    fun programName(name: String) = edit { it.copy(programName = name) }
    fun routineName(name: String) = edit { it.copy(routineName = name) }
    fun newRoutine(programId: String) = edit { it.copy(routineProgramId = programId, routineName = "", draft = emptyList(), error = null) }
    fun chooseExercise() = edit { it.copy(choosingExercise = true) }
    fun detail(id: String) = edit { it.copy(detailMembershipId = id) }

    fun back(): Boolean {
        val current = mutable.value
        if (current.saving) return true
        when {
            current.choosingExercise -> edit { it.copy(choosingExercise = false) }
            current.routineProgramId != null -> edit { it.copy(routineProgramId = null, draft = emptyList(), error = null) }
            current.creatingProgram -> edit { it.copy(creatingProgram = false, error = null) }
            current.detailMembershipId != null -> edit { it.copy(detailMembershipId = null, error = null) }
            else -> return false
        }
        return true
    }

    fun addExercise(exercise: Exercise) = edit {
        it.copy(draft = it.draft + ExerciseDraft(exercise = exercise), choosingExercise = false)
    }

    fun move(id: String, delta: Int) = edit { state ->
        val items = state.draft.toMutableList()
        val from = items.indexOfFirst { it.id == id }
        val to = from + delta
        if (from >= 0 && to in items.indices) items.add(to, items.removeAt(from))
        state.copy(draft = items)
    }

    fun removeExercise(id: String) = edit { state -> state.copy(draft = state.draft.filterNot { it.id == id }) }
    fun addSet(id: String) = changeExercise(id) { it.copy(sets = it.sets + (it.sets.lastOrNull() ?: SetDraft())) }
    fun removeSet(id: String, index: Int) = changeExercise(id) { it.copy(sets = it.sets.filterIndexed { position, _ -> position != index }) }
    fun set(id: String, index: Int, value: SetDraft) = changeExercise(id) { exercise ->
        exercise.copy(sets = exercise.sets.mapIndexed { position, old -> if (position == index) value else old })
    }

    fun saveProgram() = save {
        programs.createProgram(mutable.value.programName)
        mutable.update { it.copy(creatingProgram = false, programName = "") }
    }

    fun saveRoutine() = save {
        val state = mutable.value
        val draft = state.draft.map { occurrence ->
            RoutineExerciseDraft(occurrence.id, occurrence.exercise.id, occurrence.sets.map { it.target().also { target -> target.validateFor(occurrence.exercise) } })
        }
        val id = programs.createRoutine(requireNotNull(state.routineProgramId), state.routineName, draft)
        mutable.update { it.copy(routineProgramId = null, draft = emptyList(), detailMembershipId = id) }
    }

    private fun changeExercise(id: String, transform: (ExerciseDraft) -> ExerciseDraft) = edit { state ->
        state.copy(draft = state.draft.map { if (it.id == id) transform(it) else it })
    }

    private fun edit(transform: (ProgramsUiState) -> ProgramsUiState) {
        if (!mutable.value.saving) mutable.update(transform)
    }

    private fun save(action: suspend () -> Unit) {
        if (mutable.value.saving) return
        mutable.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            try {
                action()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (invalid: IllegalArgumentException) {
                mutable.update { it.copy(error = invalid.message ?: "Check the entered values.") }
            } catch (_: Exception) {
                mutable.update { it.copy(error = "Could not save. Your draft is still here. Try again.") }
            } finally {
                mutable.update { it.copy(saving = false) }
            }
        }
    }
}
