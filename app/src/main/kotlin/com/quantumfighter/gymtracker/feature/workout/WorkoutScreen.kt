package com.quantumfighter.gymtracker.feature.workout

import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quantumfighter.gymtracker.domain.LoadConvention
import com.quantumfighter.gymtracker.domain.LoadInput
import kotlinx.coroutines.delay

/** Focused recorder. The saved table always comes from Room, independently of editable drafts. */
@Composable
internal fun WorkoutRoute(model: WorkoutViewModel, onHome: () -> Unit) {
    val state by model.uiState.collectAsStateWithLifecycle()
    val workout = state.workout
    val exercise = workout?.exercises?.find { it.exercise.id == state.selected }
    var details by remember { mutableStateOf(false) }
    var now by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    LaunchedEffect(workout?.session?.warmupAnchorMs) {
        while (true) {
            now = SystemClock.elapsedRealtime()
            delay(500)
        }
    }
    BackHandler {
        if (!state.saving) {
            if (exercise != null) model.select(null) else onHome()
        }
    }
    Scaffold { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (state.loading) Text("Loading workout…")
            if (state.loadFailed) {
                Text("Could not read saved workout. Storage has not been reset.")
                Button(onClick = model::retry) { Text("Retry") }
            }
            state.error?.let { Text(it) }
            state.retryLabel?.let { label -> Button(onClick = model::retryCommand, enabled = !state.saving) { Text(label) } }
            state.acknowledgement?.let { Text(it) }
            if (workout == null && !state.loading) Text("Choose a saved routine on Home.")
            if (workout != null && workout.session.state == "WARM_UP") {
                Text(workout.session.routineName, style = MaterialTheme.typography.headlineMedium)
                val elapsed = workout.session.warmupElapsedMs + (workout.session.warmupAnchorMs?.let { (now - it).coerceAtLeast(0) } ?: 0)
                Text("Warm-up: ${elapsed / 1000} seconds")
                Button(onClick = { model.warmup(true) }, enabled = !state.saving && workout.session.warmupAnchorMs == null) { Text("Start Warm-up") }
                Button(onClick = { model.warmup(false) }, enabled = !state.saving) { Text("Stop Warm-up") }
            } else if (workout != null && exercise == null) {
                Text("Exercises", style = MaterialTheme.typography.headlineMedium)
                Text("Choose any exercise. Saved sets remain when you switch.")
                workout.exercises.sortedBy { it.exercise.ordinal }.forEach { item ->
                    Button(onClick = { model.select(item.exercise.id) }, enabled = !state.saving) {
                        Text("${item.exercise.definition.name} · ${if (item.sets.isEmpty()) "No saved sets" else "Partial · ${item.sets.size} saved"}")
                    }
                }
            } else if (exercise != null) {
                val definition = exercise.exercise.definition
                Text(definition.name, style = MaterialTheme.typography.headlineMedium)
                TextButton(onClick = { details = true }) { Text("Details") }
                Text("Saved sets", style = MaterialTheme.typography.titleMedium)
                if (exercise.sets.isEmpty()) Text("No saved sets")
                exercise.sets.sortedBy { it.sequence }.forEach { set ->
                    Text("${set.sequence} · ${set.setType} · ${set.loadGrams?.let { "${LoadInput.formatKg(it)} kg" } ?: "Unset weight"} · ${set.repetitions} reps")
                }
                val draft = state.drafts[exercise.exercise.id] ?: SetDraft()
                Text("Current set — unsaved draft", style = MaterialTheme.typography.titleMedium)
                Text("Load convention: ${definition.loadConvention}")
                if (definition.loadConvention != LoadConvention.BODYWEIGHT) {
                    NumberControl("Weight (kg; blank is unset)", draft.weight, !state.saving, { model.edit(draft.copy(weight = it)) }, { delta ->
                        val grams = runCatching { LoadInput.parseKg(draft.weight) }.getOrNull() ?: 0
                        val next = if (delta > 0 && grams > Long.MAX_VALUE - 2500) grams else (grams + delta * 2500L).coerceAtLeast(0)
                        model.edit(draft.copy(weight = LoadInput.formatKg(next)))
                    })
                }
                NumberControl("Repetitions", draft.reps, !state.saving, { model.edit(draft.copy(reps = it)) }, { delta ->
                    val reps = draft.reps.toIntOrNull() ?: 0
                    model.edit(draft.copy(reps = (reps.toLong() + delta).coerceIn(1, Int.MAX_VALUE.toLong()).toString()))
                })
                Button(onClick = { model.edit(draft.copy(type = if (draft.type == "WORKING") "WARM_UP" else "WORKING")) }, enabled = !state.saving) { Text("Set type: ${draft.type}") }
                Button(onClick = model::save, enabled = !state.saving && !state.loadFailed) { Text(if (state.saving) "Saving…" else "Save Set") }
                Text("Rest · prescribed ${exercise.prescriptions.minByOrNull { it.ordinal }?.restSeconds ?: 0} seconds")
                val rest = workout.session.restAnchorMs?.let { (workout.session.restDurationMs - (now - it).coerceAtLeast(0)).coerceAtLeast(0) }
                if (rest != null) Text("Rest remaining: ${(rest + 999) / 1000}s")
                Button(onClick = model::startRest, enabled = !state.saving) { Text("Start rest") }
                Button(onClick = { model.select(null) }, enabled = !state.saving) { Text("Back to Exercises") }
                if (details) {
                    AlertDialog(onDismissRequest = { details = false }, confirmButton = { TextButton(onClick = { details = false }) { Text("Close") } }, title = { Text(definition.name) }, text = {
                        Column(Modifier.verticalScroll(rememberScrollState())) {
                            Text(definition.description)
                            Text("${definition.muscleGroups.joinToString()} · ${definition.equipment.joinToString()}")
                            exercise.prescriptions.sortedBy { it.ordinal }.forEach { Text("${it.ordinal + 1}: ${it.repMin}–${it.repMax} reps · ${it.loadGrams?.let(LoadInput::formatKg) ?: "unset"} kg · ${it.restSeconds}s rest") }
                        }
                    })
                }
            }
            Button(onClick = onHome, enabled = !state.saving) { Text("Home") }
        }
    }
}

@Composable
private fun NumberControl(label: String, value: String, enabled: Boolean, edit: (String) -> Unit, step: (Int) -> Unit) {
    OutlinedTextField(value, edit, label = { Text(label) }, enabled = enabled, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Button(onClick = { step(-1) }, enabled = enabled, modifier = Modifier.sizeIn(minWidth = 64.dp, minHeight = 48.dp).semantics { contentDescription = "Decrease $label" }) { Text("−") }
        Button(onClick = { step(1) }, enabled = enabled, modifier = Modifier.sizeIn(minWidth = 64.dp, minHeight = 48.dp).semantics { contentDescription = "Increase $label" }) { Text("+") }
    }
}
