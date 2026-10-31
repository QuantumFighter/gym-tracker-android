package com.quantumfighter.gymtracker.app

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.quantumfighter.gymtracker.data.exercise.ExerciseRepository
import com.quantumfighter.gymtracker.feature.home.HomeRoute
import com.quantumfighter.gymtracker.feature.home.HomeViewModel
import com.quantumfighter.gymtracker.ui.theme.GymTrackerTheme
import kotlinx.serialization.Serializable

@Serializable
private data object Home : NavKey

/**
 * Applies the app theme and owns Navigation 3, starting at Home.
 * @param exercises Application-owned catalogue repository injected into Home's ViewModel.
 */
@Composable
fun GymTrackerApp(exercises: ExerciseRepository) {
    GymTrackerTheme {
        val backStack = rememberNavBackStack(Home)
        NavDisplay(
            backStack = backStack,
            entryProvider = entryProvider {
                entry<Home> {
                    val homeViewModel: HomeViewModel = viewModel(
                        factory = viewModelFactory { initializer { HomeViewModel(exercises) } },
                    )
                    HomeRoute(homeViewModel)
                }
            },
        )
    }
}
