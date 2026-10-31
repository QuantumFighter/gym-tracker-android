package com.quantumfighter.gymtracker.app

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.quantumfighter.gymtracker.feature.home.HomeRoute
import com.quantumfighter.gymtracker.ui.theme.GymTrackerTheme
import kotlinx.serialization.Serializable

@Serializable
private data object Home : NavKey

@Composable
fun GymTrackerApp() {
    GymTrackerTheme {
        val backStack = rememberNavBackStack(Home)
        NavDisplay(
            backStack = backStack,
            entryProvider = entryProvider {
                entry<Home> { HomeRoute() }
            },
        )
    }
}
