package com.quantumfighter.gymtracker.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quantumfighter.gymtracker.R
import com.quantumfighter.gymtracker.ui.components.MainDestination
import com.quantumfighter.gymtracker.ui.components.MainNavigation

/** Lifecycle-aware Home binding; session commands remain owned by the workout model. */
@Composable
internal fun HomeRoute(homeViewModel: HomeViewModel, onPrograms: () -> Unit, onWorkout: (String?) -> Unit = {}, hasActive: Boolean = false, recordingAvailable: Boolean = false, onHistory: (() -> Unit)? = null, onSettings: () -> Unit = {}, activeRoutineName: String? = null) {
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(uiState, onRetryCatalogue = homeViewModel::retryCatalogue, onPrograms = onPrograms, onSelectRoutine = homeViewModel::selectRoutine, onWorkout = onWorkout, hasActive = hasActive, recordingAvailable = recordingAvailable, onHistory = onHistory, onSettings = onSettings, activeRoutineName = activeRoutineName)
}

/** Approved minimal Home. Unknown estimates stay absent; recovery controls remain real. */
@Composable
internal fun HomeScreen(uiState: HomeUiState, modifier: Modifier = Modifier, onRetryCatalogue: () -> Unit = {}, onPrograms: () -> Unit = {}, onSelectRoutine: (String) -> Unit = {}, onWorkout: (String?) -> Unit = {}, hasActive: Boolean = false, recordingAvailable: Boolean = false, onHistory: (() -> Unit)? = null, onSettings: () -> Unit = {}, activeRoutineName: String? = null) {
    var choosing by rememberSaveable { mutableStateOf(false) }
    Scaffold(modifier = modifier, topBar = {
        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(start = 26.dp, end = 14.dp, top = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(uiState.title), style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = onSettings) { Icon(painterResource(R.drawable.ic_settings), "Settings") }
        }
    }, bottomBar = {
        MainNavigation(MainDestination.HOME) {
            when (it) {
                MainDestination.HOME -> Unit
                MainDestination.PROGRAMS -> onPrograms()
                MainDestination.PROGRESS -> onHistory?.invoke()
            }
        }
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 26.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Spacer(Modifier.height(72.dp))
            if (hasActive) {
                Text("Workout in progress", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                RoutineHeading(activeRoutineName ?: "Active workout", "Resume workout", onClick = { onWorkout(null) })
                Text("Resume your saved session", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (uiState.catalogueLoading) Text(stringResource(R.string.catalogue_loading))
            if (uiState.catalogueFailed) {
                Text(stringResource(R.string.catalogue_failed), color = MaterialTheme.colorScheme.error)
                Button(onClick = onRetryCatalogue) { Text(stringResource(R.string.retry)) }
            }
            if (!uiState.catalogueLoading && !uiState.catalogueFailed) {
                val next = uiState.library.nextRoutine
                if (!hasActive) {
                    Text("NEXT ROUTINE", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (next == null) {
                        Text("Prepare your first routine", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.semantics { heading() })
                        Text("Create a program to get started.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Button(onClick = onPrograms) { Text("Open Programs") }
                    } else {
                        RoutineHeading(next.name, "Record Workout", enabled = recordingAvailable, onClick = { onWorkout(next.membershipId) })
                        Text("${next.exercises.size} exercises", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(18.dp))
                        TextButton(onClick = {}, enabled = false, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Edit routine") }
                        Text("Routine editing is not available yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (uiState.library.programs.any { it.routines.isNotEmpty() }) {
                    TextButton(onClick = { choosing = true }, enabled = !uiState.selecting, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Choose next routine") }
                }
                if (uiState.selectionError) Text("Could not select routine. Check that its exercises are still available, then retry.", color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(32.dp))
        }
    }
    if (choosing) {
        AlertDialog(onDismissRequest = { choosing = false }, title = { Text("Choose next routine") }, text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                uiState.library.programs.forEach { program ->
                    program.routines.forEach { routine ->
                        TextButton(onClick = {
                            onSelectRoutine(routine.membershipId)
                            choosing = false
                        }, enabled = !uiState.selecting) { Text("${program.name} · ${routine.name}") }
                    }
                }
            }
        }, confirmButton = { TextButton(onClick = { choosing = false }) { Text("Cancel") } })
    }
}

@Composable
private fun RoutineHeading(name: String, label: String, enabled: Boolean = true, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(name, style = MaterialTheme.typography.headlineLarge, modifier = Modifier.weight(1f).semantics { heading() })
        Surface(onClick = onClick, enabled = enabled, shape = CircleShape, color = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(62.dp).semantics { contentDescription = label }) {
            Box(contentAlignment = Alignment.Center) { Icon(painterResource(R.drawable.ic_play), contentDescription = null, modifier = Modifier.size(33.dp)) }
        }
    }
}
