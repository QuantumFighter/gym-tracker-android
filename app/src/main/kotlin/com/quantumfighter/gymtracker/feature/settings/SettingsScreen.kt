package com.quantumfighter.gymtracker.feature.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quantumfighter.gymtracker.R

@Composable
internal fun SettingsRoute(model: SettingsViewModel?, onBack: () -> Unit, onLibrary: () -> Unit) {
    val state = model?.state?.collectAsStateWithLifecycle()?.value ?: SettingsUiState(error = "Preferences are unavailable.")
    var choosing by remember { mutableStateOf(false) }
    BackHandler(onBack = onBack)
    Scaffold { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 26.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            TextButton(onClick = onBack) { Text("Back") }
            Text("Settings", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.semantics { heading() })
            if (state.loading && state.error == null) Text("Loading preferences…")
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Completed exercises", style = MaterialTheme.typography.titleSmall)
                OutlinedButton(
                    onClick = { choosing = true },
                    enabled = !state.loading && !state.saving,
                    shape = RoundedCornerShape(9.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                    modifier = Modifier.fillMaxWidth().semantics {
                        contentDescription = "Completed exercise display"
                        stateDescription = if (state.hideCompleted) "Hide from active list" else "Show with strikethrough"
                    },
                ) {
                    Text(if (state.hideCompleted) "Hide from active list" else "Show with strikethrough", modifier = Modifier.weight(1f))
                    Icon(painterResource(R.drawable.ic_chevron_down), contentDescription = null, modifier = Modifier.size(18.dp))
                }
                DropdownMenu(expanded = choosing, onDismissRequest = { choosing = false }) {
                    listOf(false to "Show with strikethrough", true to "Hide from active list").forEach { (value, label) ->
                        DropdownMenuItem(text = { Text(label) }, onClick = {
                            choosing = false
                            model?.hideCompleted(value)
                        })
                    }
                }
                Text("Recorded results stay in your workout history.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (state.saving) Text("Saving preference…")
            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
                if (model != null) Button(onClick = model::retry, enabled = !state.saving) { Text("Retry preference") }
            }
            TextButton(onClick = onLibrary) { Text("Exercise Library") }
        }
    }
}
