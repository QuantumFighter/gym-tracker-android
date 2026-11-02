package com.quantumfighter.gymtracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.quantumfighter.gymtracker.R

/** Only the three approved top-level destinations; each item has a >=48 dp target. */
internal enum class MainDestination(val label: String, val icon: Int) {
    HOME("Home", R.drawable.ic_home),
    PROGRAMS("Programs", R.drawable.ic_programs),
    PROGRESS("Progress", R.drawable.ic_progress),
}

@Composable
internal fun MainNavigation(selected: MainDestination, onSelect: (MainDestination) -> Unit) {
    Column(Modifier.navigationBarsPadding()) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(Modifier.fillMaxWidth().selectableGroup().padding(horizontal = 12.dp, vertical = 5.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            MainDestination.entries.forEach { destination ->
                val active = destination == selected
                Surface(
                    color = if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    contentColor = if (active) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).selectable(selected = active, role = Role.Tab, onClick = { onSelect(destination) }),
                ) {
                    Column(Modifier.heightIn(min = 54.dp).padding(vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(painterResource(destination.icon), contentDescription = null, modifier = Modifier.size(30.dp))
                        Text(destination.label, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
