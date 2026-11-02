package com.quantumfighter.gymtracker.feature.library

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quantumfighter.gymtracker.feature.programs.ProgramsViewModel

/** Read-only catalogue access uses the existing repository-backed catalogue state. */
@Composable
internal fun LibraryRoute(model: ProgramsViewModel, onBack: () -> Unit) {
    val state by model.state.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    BackHandler(onBack = onBack)
    Scaffold { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 26.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TextButton(onClick = onBack) { Text("Back") }
            Text("Exercise Library", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.semantics { heading() })
            OutlinedTextField(query, { query = it }, label = { Text("Search exercises") }, modifier = Modifier.fillMaxWidth())
            if (state.loading) Text("Loading exercises…")
            if (state.loadFailed) {
                Text("Could not read exercise library.", color = MaterialTheme.colorScheme.error)
                TextButton(onClick = model::load) { Text("Retry library") }
            }
            val matches = state.catalogue.filter { it.name.contains(query, ignoreCase = true) }
            if (!state.loading && !state.loadFailed && matches.isEmpty()) Text("No matching exercises.")
            matches.forEach { exercise ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(exercise.name, style = MaterialTheme.typography.titleMedium)
                        Text("${exercise.muscleGroups.joinToString()} · ${exercise.equipment.joinToString()}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(exercise.description)
                    }
                }
            }
        }
    }
}
