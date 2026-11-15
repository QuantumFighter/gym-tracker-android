package com.quantumfighter.gymtracker.feature.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.quantumfighter.gymtracker.R
import com.quantumfighter.gymtracker.data.workout.RecordedSetEntity
import com.quantumfighter.gymtracker.data.workout.SessionExerciseGraph
import com.quantumfighter.gymtracker.domain.LoadConvention
import com.quantumfighter.gymtracker.domain.LoadInput

@Composable
internal fun ExerciseRecorder(
    exercise: SessionExerciseGraph,
    state: WorkoutUiState,
    model: WorkoutViewModel,
    recording: Boolean,
    restRemainingMs: Long?,
    onDetails: () -> Unit,
) {
    val definition = exercise.exercise.definition
    val sets = exercise.sets.sortedBy { it.sequence }
    val editing = state.editing
    val next = exercise.nextPrescription()
    val enabled = !state.saving && !state.loadFailed
    val usesWeight = definition.loadConvention != LoadConvention.BODYWEIGHT

    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(definition.name, Modifier.weight(1f).semantics { heading() }, style = MaterialTheme.typography.headlineMedium)
        TextButton(onClick = onDetails, enabled = !state.saving) { Text("Details") }
    }
    Text(statusLabel(exercise.exercise.status), color = MaterialTheme.colorScheme.onSurfaceVariant)
    Text("Saved sets", Modifier.semantics { heading() }, style = MaterialTheme.typography.titleMedium)
    if (sets.isEmpty()) {
        Text("No saved sets")
    } else {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Set", Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
            if (usesWeight) Text("Weight", Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
            Text("Reps", Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.size(48.dp))
        }
    }
    sets.forEach { set ->
        key(set.id) {
            HorizontalDivider()
            SavedSetRow(set, usesWeight, enabled, onEdit = { model.editSet(set) })
            val correction = state.correction
            if (editing?.id == set.id && correction != null) {
                SetEditor(
                    title = "Correct saved set ${set.sequence}",
                    draft = correction,
                    usesWeight = usesWeight,
                    enabled = enabled,
                    correction = true,
                    onEdit = model::editCorrection,
                    onSave = model::correct,
                    cancelLabel = "Cancel correction",
                    onCancel = { model.editSet(null) },
                    error = state.error,
                    retryLabel = state.retryLabel,
                    onRetry = model::retryCommand,
                )
            }
        }
    }
    if (recording) {
        val draft = state.drafts[exercise.exercise.id]
        if (editing == null && draft != null) {
            if (next != null || state.addingExtraSet) {
                SetEditor(
                    title = if (state.addingExtraSet) "Extra set ${sets.size + 1}" else "Current set ${sets.size + 1}",
                    draft = draft,
                    usesWeight = usesWeight,
                    enabled = enabled,
                    onEdit = model::edit,
                    onSave = model::save,
                    cancelLabel = if (state.addingExtraSet) "Cancel extra set" else null,
                    onCancel = { model.setExtraEditor(false) },
                )
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    FilledTonalIconButton(onClick = { model.setExtraEditor(true) }, enabled = enabled, shape = CircleShape, modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)) {
                        Icon(painterResource(R.drawable.ic_add_set), contentDescription = "Add an extra set")
                    }
                }
            }
        }
        Text("Rest · prescribed ${exercise.prescriptions.minByOrNull { it.ordinal }?.restSeconds ?: 0} seconds")
        if (restRemainingMs != null) Text("Rest remaining: ${(restRemainingMs + 999) / 1000}s")
        Button(onClick = model::startRest, enabled = enabled && editing == null) { Text("Start rest") }
        Button(onClick = model::complete, enabled = enabled && editing == null && sets.isNotEmpty()) { Text("Complete Exercise") }
    }
    Button(onClick = { model.select(null) }, enabled = !state.saving) { Text("Back to Exercises") }
}

@Composable
private fun SavedSetRow(set: RecordedSetEntity, usesWeight: Boolean, enabled: Boolean, onEdit: () -> Unit) {
    val type = if (set.setType == "WARM_UP") "Warm-up" else "Working"
    val weight = set.loadGrams?.let { "${LoadInput.formatKg(it)} ${set.unit}" }
    val summary = buildString {
        append("Set ${set.sequence}, $type")
        if (usesWeight) append(", ${weight ?: "unset weight"}")
        append(", ${set.repetitions} reps")
    }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Row(Modifier.weight(1f).clearAndSetSemantics { contentDescription = summary }, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f).padding(end = 4.dp)) {
                Text(set.sequence.toString())
                Text(type, style = MaterialTheme.typography.bodySmall)
            }
            if (usesWeight) Text(weight ?: "Unset", Modifier.weight(1f).padding(end = 4.dp))
            Text(set.repetitions.toString(), Modifier.weight(1f))
        }
        IconButton(onClick = onEdit, enabled = enabled, modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)) {
            Icon(painterResource(R.drawable.ic_edit_set), contentDescription = "Correct set ${set.sequence}")
        }
    }
}

@Composable
private fun SetEditor(
    title: String,
    draft: SetDraft,
    usesWeight: Boolean,
    enabled: Boolean,
    onEdit: (SetDraft) -> Unit,
    onSave: () -> Unit,
    correction: Boolean = false,
    cancelLabel: String? = null,
    onCancel: () -> Unit = {},
    error: String? = null,
    retryLabel: String? = null,
    onRetry: () -> Unit = {},
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, Modifier.semantics { heading() }, style = MaterialTheme.typography.titleMedium)
            Text(if (correction) "Unsaved correction" else "Not saved", style = MaterialTheme.typography.bodySmall)
            if (usesWeight) {
                NumberControl(if (correction) "Corrected weight (kg; blank is unset)" else "Weight (kg; blank is unset)", draft.weight, enabled, KeyboardType.Decimal, { onEdit(draft.copy(weight = it)) }) { delta ->
                    val grams = runCatching { LoadInput.parseKg(draft.weight) }.getOrNull() ?: 0
                    val next = if (delta > 0 && grams > Long.MAX_VALUE - 2500) grams else (grams + delta * 2500L).coerceAtLeast(0)
                    onEdit(draft.copy(weight = LoadInput.formatKg(next)))
                }
            }
            NumberControl(if (correction) "Corrected repetitions" else "Repetitions", draft.reps, enabled, KeyboardType.Number, { onEdit(draft.copy(reps = it)) }) { delta ->
                val reps = draft.reps.toIntOrNull() ?: 0
                onEdit(draft.copy(reps = (reps.toLong() + delta).coerceIn(1, Int.MAX_VALUE.toLong()).toString()))
            }
            TextButton(onClick = { onEdit(draft.copy(type = if (draft.type == "WORKING") "WARM_UP" else "WORKING")) }, enabled = enabled) {
                Text("${if (correction) "Corrected type" else "Set type"}: ${draft.type}")
            }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(onClick = onSave, enabled = enabled, modifier = Modifier.fillMaxWidth()) { Text(if (correction) "Save Correction" else "Save Set") }
            retryLabel?.let { TextButton(onClick = onRetry, enabled = enabled) { Text(it) } }
            cancelLabel?.let { TextButton(onClick = onCancel, enabled = enabled) { Text(it) } }
        }
    }
}

@Composable
private fun NumberControl(label: String, value: String, enabled: Boolean, keyboardType: KeyboardType, edit: (String) -> Unit, step: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { step(-1) }, enabled = enabled, modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)) {
            Icon(painterResource(R.drawable.ic_decrease_set), contentDescription = "Decrease $label")
        }
        OutlinedTextField(value, edit, label = { Text(label) }, enabled = enabled, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = keyboardType), modifier = Modifier.weight(1f))
        IconButton(onClick = { step(1) }, enabled = enabled, modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)) {
            Icon(painterResource(R.drawable.ic_add_set), contentDescription = "Increase $label")
        }
    }
}
