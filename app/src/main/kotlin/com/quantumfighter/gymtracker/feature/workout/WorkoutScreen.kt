package com.quantumfighter.gymtracker.feature.workout

import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quantumfighter.gymtracker.domain.LoadInput
import kotlinx.coroutines.delay

/** Focused recorder. The saved table always comes from Room, independently of editable drafts. */
@Composable
internal fun WorkoutRoute(model: WorkoutViewModel, onHome: () -> Unit, onHistory: () -> Unit = {}, hideCompleted: Boolean = false) {
    val state by model.uiState.collectAsStateWithLifecycle()
    val workout = state.workout
    var revealCompleted by rememberSaveable(workout?.session?.id, hideCompleted) { mutableStateOf(false) }
    val exercise = workout?.exercises?.find { it.exercise.id == state.selected }
    var details by remember { mutableStateOf(false) }
    var abandon by remember { mutableStateOf(false) }
    var now by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    LaunchedEffect(workout?.session?.warmupAnchorMs) {
        while (true) {
            now = SystemClock.elapsedRealtime()
            delay(500)
        }
    }
    BackHandler {
        if (!state.saving) {
            when {
                state.editing != null -> model.editSet(null)
                state.addingExtraSet -> model.setExtraEditor(false)
                exercise != null -> model.select(null)
                else -> onHome()
            }
        }
    }
    Scaffold(bottomBar = {
        if (workout != null && workout.session.state != "WARM_DOWN" && exercise == null) {
            Button(onClick = model::requestWarmdown, enabled = !state.saving && !state.loadFailed, modifier = Modifier.padding(12.dp)) { Text("Warm Down") }
        }
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (state.loading) Text("Loading workout…")
            if (state.loadFailed) {
                Text("Could not read saved workout. Storage has not been reset.")
                Button(onClick = model::retry) { Text("Retry") }
            }
            if (state.editing == null) {
                state.error?.let { Text(it) }
                state.retryLabel?.let { label -> Button(onClick = model::retryCommand, enabled = !state.saving) { Text(label) } }
            }
            state.acknowledgement?.let { Text(it) }
            if (workout == null && !state.loading) {
                if (state.finishedId != null) {
                    Button(onClick = onHistory) { Text("Workout history") }
                } else {
                    Text("Choose a saved routine on Home.")
                }
            }
            if (workout != null && workout.session.state == "WARM_UP") {
                Text(workout.session.routineName, style = MaterialTheme.typography.headlineMedium)
                val elapsed = workout.session.warmupElapsedMs + (workout.session.warmupAnchorMs?.let { (now - it).coerceAtLeast(0) } ?: 0)
                Text("Warm-up: ${elapsed / 1000} seconds")
                Button(onClick = { model.warmup(true) }, enabled = !state.saving && workout.session.warmupAnchorMs == null) { Text("Start Warm-up") }
                Button(onClick = { model.warmup(false) }, enabled = !state.saving) { Text("Stop Warm-up") }
            } else if (workout != null && workout.session.state == "WARM_DOWN" && exercise == null) {
                Text("Warm-down", style = MaterialTheme.typography.headlineMedium)
                Text("Saved sets are preserved. You can correct them before Finish.")
                val elapsed = workout.session.warmdownElapsedMs + (workout.session.warmdownAnchorMs?.let { (now - it).coerceAtLeast(0) } ?: 0)
                Text("Warm-down: ${elapsed / 1000} seconds (optional)")
                Button(onClick = { model.warmdownTimer(workout.session.warmdownAnchorMs == null) }, enabled = !state.saving) { Text(if (workout.session.warmdownAnchorMs == null) "Start Warm-down Timer" else "Stop Warm-down Timer") }
                workout.exercises.sortedBy { it.exercise.ordinal }.forEach { item ->
                    Text("${item.exercise.definition.name} · ${statusLabel(item.exercise.status)} · ${item.sets.size} saved")
                    if (item.sets.isNotEmpty()) Button(onClick = { model.select(item.exercise.id) }, enabled = !state.saving) { Text("Review ${item.exercise.definition.name}") }
                }
                Button(onClick = model::returnToRecording, enabled = !state.saving && !state.loadFailed) { Text("Return to Recording") }
                Button(onClick = model::finish, enabled = !state.saving && !state.loadFailed) { Text("Finish Workout") }
            } else if (workout != null && exercise == null) {
                Text("Exercises", style = MaterialTheme.typography.headlineMedium)
                Text("Choose any exercise. Saved sets remain when you switch.")
                val completedCount = workout.exercises.count { it.exercise.status == "COMPLETE" }
                if (hideCompleted && completedCount > 0) {
                    TextButton(onClick = { revealCompleted = !revealCompleted }) { Text(if (revealCompleted) "Hide completed exercises" else "Show completed exercises ($completedCount)") }
                }
                workout.exercises.sortedBy { it.exercise.ordinal }.filter { !hideCompleted || revealCompleted || it.exercise.status != "COMPLETE" }.forEach { item ->
                    Button(onClick = { model.select(item.exercise.id) }, enabled = !state.saving) {
                        Text(
                            "${item.exercise.definition.name} · ${if (item.exercise.status == "COMPLETE") {
                                "Complete · ${item.sets.size} saved"
                            } else if (item.sets.isEmpty()) {
                                "No saved sets"
                            } else {
                                "Partial · ${item.sets.size} saved"
                            }}",
                            style = TextStyle(textDecoration = if (item.exercise.status == "COMPLETE") TextDecoration.LineThrough else null),
                        )
                    }
                    if (item.sets.isEmpty()) Text("Not started")
                }
            } else if (exercise != null) {
                val definition = exercise.exercise.definition
                val rest = workout.session.restAnchorMs?.let { (workout.session.restDurationMs - (now - it).coerceAtLeast(0)).coerceAtLeast(0) }
                ExerciseRecorder(exercise, state, model, workout.session.state == "RECORDING", rest, onDetails = { details = true })
                if (details) {
                    AlertDialog(onDismissRequest = { details = false }, dismissButton = {
                        if (workout.session.state != "WARM_DOWN") {
                            TextButton(onClick = {
                                details = false
                                model.requestWarmdown()
                            }, enabled = !state.saving) { Text("Warm Down") }
                        }
                    }, confirmButton = { TextButton(onClick = { details = false }) { Text("Close") } }, title = { Text(definition.name) }, text = {
                        Column(Modifier.verticalScroll(rememberScrollState())) {
                            Text(definition.description)
                            Text("${definition.muscleGroups.joinToString()} · ${definition.equipment.joinToString()}")
                            exercise.prescriptions.sortedBy { it.ordinal }.forEach { Text("${it.ordinal + 1}: ${it.repMin}–${it.repMax} reps · ${it.loadGrams?.let(LoadInput::formatKg) ?: "unset"} kg · ${it.restSeconds}s rest") }
                        }
                    })
                }
            }
            if (workout != null) TextButton(onClick = { abandon = true }, enabled = !state.saving) { Text("Abandon Workout") }
            Button(onClick = onHome, enabled = !state.saving) { Text("Home") }
        }
    }
    if (state.confirmWarmdown) {
        AlertDialog(onDismissRequest = model::cancelWarmdown, title = { Text("Unfinished exercises remain") }, text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Saved sets will be kept. Exercises with saved sets stay partial; unstarted exercises are skipped. You can return before Finish.")
                state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        }, confirmButton = {
            TextButton(onClick = {
                if (state.retryLabel != null) model.retryCommand() else model.enterWarmdown(true)
            }, enabled = !state.saving) { Text(state.retryLabel ?: "Enter Warm-down") }
        }, dismissButton = { TextButton(onClick = model::cancelWarmdown, enabled = !state.saving) { Text("Keep Recording") } })
    }
    if (abandon) {
        AlertDialog(onDismissRequest = { abandon = false }, title = { Text("Abandon this workout?") }, text = { Text("Saved sets are retained with an abandoned status. The next routine will not advance.") }, confirmButton = {
            TextButton(onClick = {
                abandon = false
                model.abandon()
            }, enabled = !state.saving) { Text("Confirm Abandon") }
        }, dismissButton = { TextButton(onClick = { abandon = false }) { Text("Cancel") } })
    }
}

internal fun statusLabel(status: String): String = when (status) {
    "COMPLETE" -> "Complete"
    "PARTIAL" -> "Partial"
    "SKIPPED" -> "Skipped"
    else -> "Not started"
}
