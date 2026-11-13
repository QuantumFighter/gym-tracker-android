package com.quantumfighter.gymtracker

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.test.platform.app.InstrumentationRegistry
import com.quantumfighter.gymtracker.app.GymTrackerApp
import com.quantumfighter.gymtracker.data.exercise.BuiltInCatalogue
import com.quantumfighter.gymtracker.data.exercise.ExerciseRepository
import com.quantumfighter.gymtracker.data.exercise.GymDatabase
import com.quantumfighter.gymtracker.data.program.ProgramRepository
import com.quantumfighter.gymtracker.data.settings.UiPreferencesRepository
import com.quantumfighter.gymtracker.data.workout.WorkoutRepository
import com.quantumfighter.gymtracker.domain.ProgramLibrary
import com.quantumfighter.gymtracker.domain.RoutineExerciseDraft
import com.quantumfighter.gymtracker.domain.SaveSetCommand
import com.quantumfighter.gymtracker.domain.SetTarget
import com.quantumfighter.gymtracker.feature.home.HomeScreen
import com.quantumfighter.gymtracker.feature.home.HomeUiState
import com.quantumfighter.gymtracker.ui.theme.GymTrackerTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.util.UUID

/** T-03/T-14 subset: native navigation, committed preferences and synthetic screenshot evidence. */
class UiFoundationTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val name = "ui-foundation-test.db"
    private lateinit var database: GymDatabase

    @Before fun open() {
        context.deleteDatabase(name)
        database = GymDatabase.build(context, name)
    }

    @After fun close() {
        compose.activityRule.scenario.close()
        database.close()
        context.deleteDatabase(name)
    }

    private suspend fun prepare(): String {
        ExerciseRepository(database).seedCatalogue()
        val plans = ProgramRepository(database)
        val program = plans.createProgram("Synthetic program")
        return plans.createRoutine(program, "Upper Body A", BuiltInCatalogue.exercises.take(2).map { RoutineExerciseDraft(UUID.randomUUID().toString(), it.id, listOf(SetTarget(8, 12, null, 90))) })
    }

    private fun start(preferences: UiPreferencesRepository = UiPreferencesRepository(database)) {
        compose.setContent { GymTrackerApp(ExerciseRepository(database), ProgramRepository(database), WorkoutRepository(database), preferences) }
    }

    @Test fun t03HomePlayNavigationSettingsAndLibraryUseRealData() = runBlocking {
        val membership = prepare()
        start()
        waitFor("Upper Body A")
        compose.onNodeWithContentDescription("Record Workout").assertIsDisplayed()
        capture("home-native")
        click("Progress")
        waitFor("Workout history")
        click("Workout history")
        waitFor("No finished workouts yet.")
        click("Progress")
        waitFor("Exercise trends are not available yet.")
        click("Workout history")
        waitFor("No finished workouts yet.")
        compose.runOnIdle { compose.activity.onBackPressedDispatcher.onBackPressed() }
        waitFor("Exercise trends are not available yet.")
        click("Home")
        compose.onNodeWithContentDescription("Settings").performClick()
        waitFor("Completed exercises")
        capture("settings-native")
        click("Exercise Library")
        waitFor(BuiltInCatalogue.exercises.first().name)
        assertEquals(membership, ProgramRepository(database).readLibrary().nextMembershipId)
        assertEquals(null, WorkoutRepository(database).active())
    }

    @Test fun t03HistoryReadFailureOffersRetryWithoutAnEmptyMessage() = runBlocking {
        prepare()
        val sqlite = database.openHelper.writableDatabase
        sqlite.execSQL("ALTER TABLE workout_sessions RENAME TO unavailable_workout_sessions")
        start()
        waitFor("Upper Body A")
        click("Progress")
        waitFor("Workout history")
        click("Workout history")
        waitFor("Retry history")
        compose.onNodeWithText("No finished workouts yet.").assertDoesNotExist()
        sqlite.execSQL("ALTER TABLE unavailable_workout_sessions RENAME TO workout_sessions")
        click("Retry history")
        waitFor("No finished workouts yet.")
        compose.onNodeWithText("Retry history").assertDoesNotExist()
    }

    @Test fun t03ChoosingNextRoutineDuringWorkoutPreservesSavedSetsAndFinishAdvances() = runBlocking {
        val membership = prepare()
        val plans = ProgramRepository(database)
        val program = plans.readLibrary().programs.single()
        val exerciseId = BuiltInCatalogue.exercises.first().id
        val successor = plans.createRoutine(program.id, "Upper Body B", listOf(RoutineExerciseDraft(UUID.randomUUID().toString(), exerciseId, listOf(SetTarget(8, 12, null, 90)))))
        val manualChoice = plans.createRoutine(program.id, "Upper Body C", listOf(RoutineExerciseDraft(UUID.randomUUID().toString(), exerciseId, listOf(SetTarget(8, 12, null, 90)))))
        val workouts = WorkoutRepository(database)
        val id = workouts.start(membership)
        workouts.warmup(false)
        val exercise = workouts.active()!!.exercises.first().exercise
        val saved = workouts.save(SaveSetCommand(UUID.randomUUID().toString(), exercise.id, null, 10, "WORKING"))

        start()
        waitFor("Resume workout")
        click("Choose next routine")
        click("Synthetic program · Upper Body C")
        compose.waitUntil(10_000) { runBlocking { plans.readLibrary().nextMembershipId == manualChoice } }
        compose.onNodeWithContentDescription("Record Workout").assertDoesNotExist()
        compose.onNodeWithContentDescription("Resume workout").performClick()
        waitFor("${exercise.definition.name} · Partial · 1 saved")
        val resumed = workouts.active()!!
        assertEquals(id, resumed.session.id)
        assertEquals(listOf(saved), resumed.exercises.flatMap { it.sets })

        click("Warm Down")
        waitFor("Unfinished exercises remain")
        click("Enter Warm-down")
        waitFor("Finish Workout")
        click("Finish Workout")
        waitFor("Workout finished")
        assertEquals(successor, plans.readLibrary().nextMembershipId)
        assertEquals(listOf(saved), workouts.finished(id)!!.exercises.flatMap { it.sets })
    }

    @Test fun t03HiddenCompletedWorkCanBeRevealedAndResumeCannotStartAgain() = runBlocking {
        val membership = prepare()
        val workouts = WorkoutRepository(database)
        val id = workouts.start(membership)
        workouts.warmup(false)
        val exercise = workouts.active()!!.exercises.first()
        workouts.save(SaveSetCommand(UUID.randomUUID().toString(), exercise.exercise.id, null, 10, "WORKING"))
        workouts.complete(id, exercise.exercise.id)
        UiPreferencesRepository(database).hideCompleted(true)
        start()
        waitFor("Resume workout")
        compose.onNodeWithContentDescription("Record Workout").assertDoesNotExist()
        compose.onNodeWithContentDescription("Resume workout").performClick()
        waitFor("Show completed exercises (1)")
        val completed = "${exercise.exercise.definition.name} · Complete · 1 saved"
        compose.onNodeWithText(completed).assertDoesNotExist()
        click("Show completed exercises (1)")
        compose.onNodeWithText(completed).performScrollTo().assertIsDisplayed()
        click(completed)
        waitFor("Saved sets")
        assertEquals(id, workouts.active()!!.session.id)
        assertEquals(1, workouts.active()!!.exercises.flatMap { it.sets }.size)
    }

    @Test fun t03PreferenceFailureRetainsCommittedValueAndRetryPersists() = runBlocking {
        prepare()
        var fail = true
        val preferences = object : UiPreferencesRepository(database) {
            override suspend fun hideCompleted(value: Boolean) {
                if (fail) error("Synthetic write failure")
                super.hideCompleted(value)
            }
        }
        start(preferences)
        compose.onNodeWithContentDescription("Settings").performClick()
        waitFor("Show with strikethrough")
        click("Show with strikethrough")
        click("Hide from active list")
        waitFor("Retry preference")
        compose.onNodeWithText("Show with strikethrough").assertIsDisplayed()
        assertFalse(preferences.observeHiddenCompleted().first())
        fail = false
        click("Retry preference")
        compose.waitUntil(10_000) { runBlocking { preferences.observeHiddenCompleted().first() } }
        assertTrue(preferences.observeHiddenCompleted().first())
    }

    @Test fun t14CaptureHomeDarkAndLargeTextWithoutChangingDeviceSettings() = runBlocking {
        prepare()
        val library: ProgramLibrary = ProgramRepository(database).readLibrary()
        val dark = mutableStateOf(false)
        val scale = mutableStateOf(1f)
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, scale.value)) {
                GymTrackerTheme(darkTheme = dark.value) { HomeScreen(HomeUiState(catalogueLoading = false, library = library), recordingAvailable = true) }
            }
        }
        capture("home-light")
        compose.runOnIdle { dark.value = true }
        capture("home-dark")
        compose.runOnIdle {
            dark.value = false
            scale.value = 2f
        }
        compose.onNodeWithContentDescription("Record Workout").performScrollTo().assertIsDisplayed()
        capture("home-200-percent")
        compose.onNodeWithText("Choose next routine").performScrollTo().assertIsDisplayed()
        Unit
    }

    private fun capture(label: String) {
        compose.waitForIdle()
        val directory = File(context.getExternalFilesDir(null), "ui-evidence").apply { mkdirs() }
        File(directory, "$label.png").outputStream().use { compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
    private fun waitFor(label: String) {
        compose.waitUntil(10_000) { compose.onAllNodes(hasText(label) or hasContentDescription(label)).fetchSemanticsNodes().isNotEmpty() }
    }
    private fun click(label: String) {
        val node = compose.onNodeWithText(label)
        if (compose.onAllNodes(hasText(label) and hasAnyAncestor(hasScrollAction())).fetchSemanticsNodes().isNotEmpty()) node.performScrollTo()
        node.assertIsDisplayed().performClick()
    }
}
