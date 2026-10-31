package com.quantumfighter.gymtracker.feature.history

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quantumfighter.gymtracker.domain.LoadInput
import com.quantumfighter.gymtracker.feature.workout.statusLabel
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Composable
internal fun HistoryRoute(model: HistoryViewModel, onHome: () -> Unit) {
    val state by model.uiState.collectAsStateWithLifecycle()
    BackHandler { if (state.selected != null) model.select(null) else onHome() }
    Scaffold { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Workout history", style = MaterialTheme.typography.headlineMedium)
            if (state.loading) Text("Loading history…")
            if (state.failed) {
                Text("Could not read workout history.")
                Button(onClick = model::retry) { Text("Retry history") }
            }
            if (state.selected == null) {
                if (!state.loading && state.sessions.isEmpty()) Text("No finished workouts yet.")
                state.sessions.forEach { session ->
                    Button(onClick = { model.select(session.id) }) { Text("${session.routineName} · ${historicalDate(session.startedAt, session.offsetSeconds)}") }
                }
            } else {
                val graph = state.detail
                if (!state.loading && graph == null && !state.failed) Text("Finished workout unavailable.")
                if (graph != null) {
                    val session = graph.session
                    Text(session.routineName, style = MaterialTheme.typography.titleLarge)
                    Text(session.programName)
                    Text("Started: ${historicalDate(session.startedAt, session.offsetSeconds)}")
                    session.finishedAt?.let { Text("Finished: ${historicalDate(it, session.finishedOffsetSeconds ?: session.offsetSeconds)}") }
                    Text("Finished · Read-only")
                    Text("Warm-up: ${session.warmupElapsedMs / 1000}s · Warm-down: ${session.warmdownElapsedMs / 1000}s")
                    graph.exercises.sortedBy { it.exercise.ordinal }.forEach { exercise ->
                        Text("${exercise.exercise.definition.name} · ${statusLabel(exercise.exercise.status)}", style = MaterialTheme.typography.titleMedium)
                        Text(exercise.exercise.definition.description)
                        Text("${exercise.exercise.definition.measurementKind} · ${exercise.exercise.definition.loadConvention}")
                        exercise.prescriptions.sortedBy { it.ordinal }.forEach { Text("Prescribed ${it.ordinal + 1}: ${it.setType} · ${it.repMin}–${it.repMax} reps · ${it.loadGrams?.let(LoadInput::formatKg) ?: "unset"} ${it.unit} · ${it.restSeconds}s rest") }
                        if (exercise.sets.isEmpty()) Text("No saved sets")
                        exercise.sets.sortedBy { it.sequence }.forEach { set ->
                            Text("${set.sequence} · ${set.setType} · ${set.loadGrams?.let { "${LoadInput.formatKg(it)} ${set.unit}" } ?: "Unset weight"} · ${set.repetitions} reps")
                        }
                    }
                }
                Button(onClick = { model.select(null) }) { Text("Back to History") }
            }
            Button(onClick = onHome) { Text("Home") }
        }
    }
}

private fun historicalDate(millis: Long, offset: Int): String = Instant.ofEpochMilli(millis).atOffset(ZoneOffset.ofTotalSeconds(offset)).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss xxx"))
