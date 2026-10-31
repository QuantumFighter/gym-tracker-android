package com.quantumfighter.gymtracker.feature.programs

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quantumfighter.gymtracker.domain.LoadConvention
import com.quantumfighter.gymtracker.domain.LoadInput
import com.quantumfighter.gymtracker.domain.PreparedRoutine

/** Minimal list → creation → saved-detail flow. All writes go through the ViewModel. */
@Composable
internal fun ProgramsRoute(viewModel: ProgramsViewModel, onHome: () -> Unit, onResumeWorkout: (() -> Unit)? = null) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val back = { if (!viewModel.back()) onHome() }
    BackHandler(onBack = back)
    Scaffold { padding ->
        Column(
            modifier = Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TextButton(onClick = back, enabled = !state.saving) { Text("Back") }
            if (onResumeWorkout != null) Button(onClick = onResumeWorkout, enabled = !state.saving) { Text("Resume workout") }
            Text(
                when {
                    state.choosingExercise -> "Choose exercise"
                    state.routineProgramId != null -> "Create routine"
                    state.creatingProgram -> "Create program"
                    state.detailMembershipId != null -> "Saved routine"
                    else -> "Programs"
                },
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.semantics { heading() },
            )
            if (state.loading) Text("Opening saved programs…")
            if (state.loadFailed) {
                Text(
                    "Could not open saved programs. Automatic reset is disabled. Retry to open again.",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.testTag("programLoadError"),
                )
                Button(onClick = viewModel::load) { Text("Retry") }
            }
            state.error?.let { error ->
                Text(error, color = MaterialTheme.colorScheme.error, modifier = Modifier.testTag("programError"))
            }
            when {
                state.loading -> Unit
                state.choosingExercise -> {
                    state.catalogue.forEach { exercise ->
                        Button(onClick = { viewModel.addExercise(exercise) }, modifier = Modifier.fillMaxWidth()) { Text(exercise.name) }
                    }
                }
                state.routineProgramId != null -> {
                    Input("Routine name", state.routineName, viewModel::routineName, state.saving)
                    Text("Unsaved draft. Save routine when all exercises and targets are ready.")
                    state.draft.forEach { exercise ->
                        ExerciseEditor(exercise, state.draft.indexOf(exercise), state.draft.size, state.saving, viewModel)
                    }
                    Button(onClick = viewModel::chooseExercise, enabled = !state.saving) { Text("Add exercise") }
                    Button(onClick = viewModel::saveRoutine, enabled = !state.saving) { Text(if (state.saving) "Saving…" else "Save routine") }
                }
                state.creatingProgram -> {
                    Input("Program name", state.programName, viewModel::programName, state.saving)
                    Button(onClick = viewModel::saveProgram, enabled = !state.saving) { Text(if (state.saving) "Saving…" else "Save program") }
                }
                state.detailMembershipId != null -> {
                    val routine = state.library.programs.flatMap { it.routines }.find { it.membershipId == state.detailMembershipId }
                    if (routine != null) RoutineDetails(routine) else Text("Loading saved routine…")
                    Button(onClick = {
                        viewModel.back()
                        onHome()
                    }) { Text("Home") }
                }
                else -> {
                    Button(onClick = viewModel::newProgram, enabled = state.catalogue.isNotEmpty()) { Text("Create program") }
                    if (state.library.programs.isEmpty()) Text("Create a program, then prepare its first routine.")
                    state.library.programs.forEach { program ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(program.name, style = MaterialTheme.typography.titleLarge)
                                program.routines.forEach { routine ->
                                    TextButton(onClick = { viewModel.detail(routine.membershipId) }) { Text(routine.name) }
                                }
                                Button(onClick = { viewModel.newRoutine(program.id) }) { Text("Add routine to ${program.name}") }
                            }
                        }
                    }
                }
            }
            Text("")
        }
    }
}

@Composable
private fun ExerciseEditor(exercise: ExerciseDraft, index: Int, count: Int, saving: Boolean, viewModel: ProgramsViewModel) {
    Card(modifier = Modifier.fillMaxWidth().testTag("draftExercise$index")) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("${index + 1}. ${exercise.exercise.name}", style = MaterialTheme.typography.titleLarge)
            Row {
                TextButton(onClick = { viewModel.move(exercise.id, -1) }, enabled = !saving && index > 0) { Text("Move up") }
                TextButton(onClick = { viewModel.move(exercise.id, 1) }, enabled = !saving && index < count - 1) { Text("Move down") }
            }
            TextButton(onClick = { viewModel.removeExercise(exercise.id) }, enabled = !saving) { Text("Remove exercise") }
            Text(loadLabel(exercise.exercise.loadConvention))
            exercise.sets.forEachIndexed { setIndex, set ->
                Text("Set ${setIndex + 1}", style = MaterialTheme.typography.titleMedium)
                Input("Minimum reps", set.min, { viewModel.set(exercise.id, setIndex, set.copy(min = it)) }, saving, KeyboardType.Number)
                Input("Maximum reps", set.max, { viewModel.set(exercise.id, setIndex, set.copy(max = it)) }, saving, KeyboardType.Number)
                if (exercise.exercise.loadConvention != LoadConvention.BODYWEIGHT) {
                    Input("Starting weight kg (optional)", set.kg, { viewModel.set(exercise.id, setIndex, set.copy(kg = it)) }, saving, KeyboardType.Decimal)
                }
                Input("Rest seconds (0 skips rest)", set.rest, { viewModel.set(exercise.id, setIndex, set.copy(rest = it)) }, saving, KeyboardType.Number)
                TextButton(onClick = { viewModel.removeSet(exercise.id, setIndex) }, enabled = !saving) { Text("Remove set ${setIndex + 1}") }
            }
            Button(onClick = { viewModel.addSet(exercise.id) }, enabled = !saving) { Text("+ Add set") }
        }
    }
}

@Composable
private fun Input(label: String, value: String, onChange: (String) -> Unit, saving: Boolean, keyboard: KeyboardType = KeyboardType.Text) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        enabled = !saving,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
internal fun RoutineDetails(routine: PreparedRoutine) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(routine.name, style = MaterialTheme.typography.titleLarge)
        routine.exercises.forEachIndexed { index, occurrence ->
            Text("${index + 1}. ${occurrence.exercise.name}", style = MaterialTheme.typography.titleMedium)
            Text(loadLabel(occurrence.exercise.loadConvention))
            occurrence.sets.forEachIndexed { setIndex, target ->
                val load = target.loadGrams?.let { "${LoadInput.formatKg(it)} kg" } ?: if (occurrence.exercise.loadConvention == LoadConvention.BODYWEIGHT) "Bodyweight" else "Weight unset"
                Text("Set ${setIndex + 1}: ${target.repMin}–${target.repMax} reps · $load · ${target.restSeconds} s rest")
            }
        }
    }
}

private fun loadLabel(convention: LoadConvention): String = when (convention) {
    LoadConvention.TOTAL -> "Weight is total external load, including the bar. Blank means unknown."
    LoadConvention.PER_HAND -> "Weight is per hand. Blank means unknown."
    LoadConvention.ASSISTANCE -> "Weight is machine assistance; more assistance makes this easier. Blank means unknown."
    LoadConvention.BODYWEIGHT -> "Bodyweight only; no external starting weight."
}
