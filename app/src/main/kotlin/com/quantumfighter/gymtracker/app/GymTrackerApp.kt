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
import com.quantumfighter.gymtracker.data.program.ProgramRepository
import com.quantumfighter.gymtracker.feature.home.HomeRoute
import com.quantumfighter.gymtracker.feature.home.HomeViewModel
import com.quantumfighter.gymtracker.feature.programs.ProgramsRoute
import com.quantumfighter.gymtracker.feature.programs.ProgramsViewModel
import com.quantumfighter.gymtracker.ui.theme.GymTrackerTheme
import kotlinx.serialization.Serializable

@Serializable
private data object Home : NavKey

@Serializable
private data object Programs : NavKey

/**
 * Applies the app theme and owns Navigation 3, starting at Home.
 * @param exercises Application-owned catalogue repository injected into Home's ViewModel.
 * @param programs Shared-database plan repository injected into Home and Programs.
 */
@Composable
internal fun GymTrackerApp(exercises: ExerciseRepository, programs: ProgramRepository) {
    GymTrackerTheme {
        val backStack = rememberNavBackStack(Home)
        NavDisplay(
            backStack = backStack,
            entryProvider = entryProvider {
                entry<Home> {
                    val homeViewModel: HomeViewModel = viewModel(
                        factory = viewModelFactory { initializer { HomeViewModel(exercises, programs) } },
                    )
                    HomeRoute(homeViewModel, onPrograms = { backStack.add(Programs) })
                }
                entry<Programs> {
                    val programsViewModel: ProgramsViewModel = viewModel(
                        factory = viewModelFactory { initializer { ProgramsViewModel(programs, exercises) } },
                    )
                    ProgramsRoute(programsViewModel, onHome = { backStack.removeLastOrNull() })
                }
            },
        )
    }
}
