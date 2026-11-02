package com.quantumfighter.gymtracker.app

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.quantumfighter.gymtracker.data.exercise.ExerciseRepository
import com.quantumfighter.gymtracker.data.program.ProgramRepository
import com.quantumfighter.gymtracker.data.settings.UiPreferencesRepository
import com.quantumfighter.gymtracker.data.workout.WorkoutRepository
import com.quantumfighter.gymtracker.feature.history.HistoryRoute
import com.quantumfighter.gymtracker.feature.history.HistoryViewModel
import com.quantumfighter.gymtracker.feature.history.ProgressScreen
import com.quantumfighter.gymtracker.feature.home.HomeRoute
import com.quantumfighter.gymtracker.feature.home.HomeViewModel
import com.quantumfighter.gymtracker.feature.library.LibraryRoute
import com.quantumfighter.gymtracker.feature.programs.ProgramsRoute
import com.quantumfighter.gymtracker.feature.programs.ProgramsViewModel
import com.quantumfighter.gymtracker.feature.settings.SettingsRoute
import com.quantumfighter.gymtracker.feature.settings.SettingsViewModel
import com.quantumfighter.gymtracker.feature.workout.WorkoutRoute
import com.quantumfighter.gymtracker.feature.workout.WorkoutViewModel
import com.quantumfighter.gymtracker.ui.theme.GymTrackerTheme
import kotlinx.serialization.Serializable

@Serializable
private data object Home : NavKey

@Serializable
private data object Programs : NavKey

@Serializable
private data object Workout : NavKey

@Serializable
private data object History : NavKey

@Serializable
private data object Progress : NavKey

@Serializable
private data object Settings : NavKey

@Serializable
private data object Library : NavKey

/**
 * Applies the app theme and owns Navigation 3, starting at Home.
 * @param exercises Application-owned catalogue repository injected into Home's ViewModel.
 * @param programs Shared-database plan repository injected into Home and Programs.
 */
@Composable
internal fun GymTrackerApp(exercises: ExerciseRepository, programs: ProgramRepository, workouts: WorkoutRepository? = null, preferences: UiPreferencesRepository? = null) {
    GymTrackerTheme {
        val workoutModel: WorkoutViewModel? = if (workouts == null) null else viewModel(factory = viewModelFactory { initializer { WorkoutViewModel(workouts) } })
        val workoutState = workoutModel?.uiState?.collectAsStateWithLifecycle()?.value
        val backStack = rememberNavBackStack(Home)
        val settingsModel: SettingsViewModel? = if (preferences == null) null else viewModel(factory = viewModelFactory { initializer { SettingsViewModel(preferences) } })
        val settingsState = settingsModel?.state?.collectAsStateWithLifecycle()?.value
        val home = {
            backStack.clear()
            backStack.add(Home)
            Unit
        }
        val toPrograms = {
            backStack.clear()
            backStack.add(Home)
            backStack.add(Programs)
            Unit
        }
        val toProgress = {
            backStack.clear()
            backStack.add(Home)
            backStack.add(Progress)
            Unit
        }
        NavDisplay(
            backStack = backStack,
            entryProvider = entryProvider {
                entry<Home> {
                    val homeViewModel: HomeViewModel = viewModel(
                        factory = viewModelFactory { initializer { HomeViewModel(exercises, programs) } },
                    )
                    HomeRoute(homeViewModel, onPrograms = toPrograms, onWorkout = { id ->
                        if (id != null) workoutModel?.start(id)
                        backStack.add(Workout)
                    }, hasActive = workoutState?.workout != null, recordingAvailable = workoutModel != null, onHistory = toProgress, onSettings = { backStack.add(Settings) }, activeRoutineName = workoutState?.workout?.session?.routineName)
                }
                entry<Programs> {
                    val programsViewModel: ProgramsViewModel = viewModel(
                        factory = viewModelFactory { initializer { ProgramsViewModel(programs, exercises) } },
                    )
                    ProgramsRoute(programsViewModel, onHome = home, onResumeWorkout = if (workoutState?.workout != null) ({ backStack.add(Workout) }) else null, onProgress = toProgress)
                }
                entry<Workout> {
                    if (workoutModel != null) {
                        WorkoutRoute(workoutModel, onHome = {
                            backStack.clear()
                            backStack.add(Home)
                        }, onHistory = { backStack.add(History) }, hideCompleted = settingsState?.hideCompleted ?: false)
                    }
                }
                entry<History> {
                    if (workouts != null) {
                        val model: HistoryViewModel = viewModel(factory = viewModelFactory { initializer { HistoryViewModel(workouts) } })
                        HistoryRoute(model, onHome = {
                            backStack.clear()
                            backStack.add(Home)
                        }, onBack = { backStack.removeLastOrNull() })
                    }
                }
                entry<Progress> {
                    ProgressScreen(onHome = home, onPrograms = toPrograms, onHistory = { backStack.add(History) })
                }
                entry<Settings> {
                    SettingsRoute(settingsModel, onBack = { backStack.removeLastOrNull() }, onLibrary = { backStack.add(Library) })
                }
                entry<Library> {
                    val model: ProgramsViewModel = viewModel(key = "library", factory = viewModelFactory { initializer { ProgramsViewModel(programs, exercises) } })
                    LibraryRoute(model, onBack = { backStack.removeLastOrNull() })
                }
            },
        )
    }
}
