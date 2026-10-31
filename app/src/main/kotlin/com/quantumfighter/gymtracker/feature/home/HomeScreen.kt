package com.quantumfighter.gymtracker.feature.home

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quantumfighter.gymtracker.R

/**
 * Connects [HomeViewModel] to [HomeScreen] using lifecycle-aware state collection.
 *
 * @param homeViewModel State owner constructed by the application composition root.
 */
@Composable
internal fun HomeRoute(homeViewModel: HomeViewModel, onPrograms: () -> Unit, onWorkout: (String?) -> Unit = {}, hasActive: Boolean = false, recordingAvailable: Boolean = false) {
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(uiState, onRetryCatalogue = homeViewModel::retryCatalogue, onPrograms = onPrograms, onSelectRoutine = homeViewModel::selectRoutine, onWorkout = onWorkout, hasActive = hasActive, recordingAvailable = recordingAvailable)
}

/**
 * Renders Home from caller-owned [uiState], without performing persistence or navigation.
 *
 * @param uiState Immutable presentation state to display.
 * @param modifier Layout and semantics applied to the screen's scaffold.
 * @param onRetryCatalogue Requests another non-destructive storage initialisation attempt.
 */
@Composable
internal fun HomeScreen(uiState: HomeUiState, modifier: Modifier = Modifier, onRetryCatalogue: () -> Unit = {}, onPrograms: () -> Unit = {}, onSelectRoutine: (String) -> Unit = {}, onWorkout: (String?) -> Unit = {}, hasActive: Boolean = false, recordingAvailable: Boolean = false) {
    var choosing by rememberSaveable { mutableStateOf(false) }
    Scaffold(modifier = modifier) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        ) {
            Text(
                text = stringResource(uiState.title),
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = stringResource(R.string.foundation_message),
                style = MaterialTheme.typography.bodyLarge,
            )
            if (uiState.catalogueLoading) {
                Text(stringResource(R.string.catalogue_loading))
            }
            if (hasActive) Button(onClick = { onWorkout(null) }) { Text("Resume workout") }
            if (uiState.catalogueFailed) {
                Text(stringResource(R.string.catalogue_failed))
                Button(onClick = onRetryCatalogue) { Text(stringResource(R.string.retry)) }
            }
            if (!uiState.catalogueLoading && !uiState.catalogueFailed) {
                val next = uiState.library.nextRoutine
                if (next == null) {
                    Text("Prepare a routine for your next workout.")
                } else {
                    Text("Next workout", style = MaterialTheme.typography.titleMedium)
                    Text(next.name, style = MaterialTheme.typography.titleLarge)
                    Text("${next.exercises.size} exercises · ${next.exercises.sumOf { it.sets.size }} prescribed sets")
                    if (recordingAvailable && !hasActive) Button(onClick = { onWorkout(next.membershipId) }) { Text("Record Workout") }
                }
                Button(onClick = onPrograms) { Text("Programs") }
                if (uiState.library.programs.any { it.routines.isNotEmpty() }) {
                    Button(onClick = { choosing = !choosing }, enabled = !uiState.selecting) { Text("Choose next routine") }
                }
                if (uiState.selectionError) Text("Could not select routine. Check that its exercises are still available, then retry.")
                if (choosing) {
                    uiState.library.programs.forEach { program ->
                        program.routines.forEach { routine ->
                            Button(onClick = {
                                onSelectRoutine(routine.membershipId)
                                choosing = false
                            }, enabled = !uiState.selecting) {
                                Text("${program.name} · ${routine.name}")
                            }
                        }
                    }
                }
            }
        }
    }
}
