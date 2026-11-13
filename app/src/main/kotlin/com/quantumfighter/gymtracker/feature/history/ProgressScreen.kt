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
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.quantumfighter.gymtracker.ui.components.MainDestination
import com.quantumfighter.gymtracker.ui.components.MainNavigation

/** History is separate. Trends wait for comparable-data rules, never sample data. */
@Composable
internal fun ProgressScreen(onHome: () -> Unit, onPrograms: () -> Unit, onHistory: () -> Unit) {
    BackHandler(onBack = onHome)
    Scaffold(bottomBar = {
        MainNavigation(MainDestination.PROGRESS) {
            when (it) {
                MainDestination.HOME -> onHome()
                MainDestination.PROGRAMS -> onPrograms()
                MainDestination.PROGRESS -> Unit
            }
        }
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(26.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text("Progress", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.semantics { heading() })
            Text("Exercise trends are not available yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = onHistory) { Text("Workout history") }
        }
    }
}
