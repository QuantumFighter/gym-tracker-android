package com.quantumfighter.gymtracker

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.platform.app.InstrumentationRegistry
import com.quantumfighter.gymtracker.data.exercise.BuiltInCatalogue
import com.quantumfighter.gymtracker.data.exercise.ExerciseRepository
import com.quantumfighter.gymtracker.data.exercise.GymDatabase
import com.quantumfighter.gymtracker.data.program.ProgramRepository
import com.quantumfighter.gymtracker.data.workout.WorkoutRepository
import com.quantumfighter.gymtracker.domain.RoutineExerciseDraft
import com.quantumfighter.gymtracker.domain.SetTarget
import com.quantumfighter.gymtracker.feature.workout.WorkoutRoute
import com.quantumfighter.gymtracker.feature.workout.WorkoutViewModel
import com.quantumfighter.gymtracker.ui.theme.GymTrackerTheme
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.util.UUID

/** Actual Compose callbacks and real Room, including a deterministic SQLite write failure. */
class WorkoutUiTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun t01OutOfOrderPartialRevisitAndRetryableFailedSave() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "b04-ui-test.db"
        context.deleteDatabase(name)
        val database = GymDatabase.build(context, name)
        try {
            ExerciseRepository(database).seedCatalogue()
            val plans = ProgramRepository(database)
            val membership = plans.createRoutine(plans.createProgram("UI fixture"), "UI routine", BuiltInCatalogue.exercises.take(2).map { RoutineExerciseDraft(UUID.randomUUID().toString(), it.id, listOf(SetTarget(8, 12, 12345, 90))) })
            val repository = WorkoutRepository(database)
            repository.start(membership)
            val model = WorkoutViewModel(repository)
            compose.setContent { GymTrackerTheme { WorkoutRoute(model, {}) } }
            waitFor("Start Warm-up")
            click("Start Warm-up")
            compose.waitUntil(5000) { !model.uiState.value.saving && model.uiState.value.workout?.session?.warmupAnchorMs != null }
            click("Stop Warm-up")
            waitFor("Exercises")
            click("${BuiltInCatalogue.exercises[1].name} · No saved sets")
            waitFor("Save Set")
            database.openHelper.writableDatabase.execSQL("CREATE TRIGGER b04_ui_reject BEFORE INSERT ON recorded_sets BEGIN SELECT RAISE(ABORT, 'synthetic failure'); END")
            val originalDraft = model.uiState.value.drafts.values.single()
            click("Save Set")
            compose.waitUntil(5000) { model.uiState.value.error != null }
            assertEquals(originalDraft, model.uiState.value.drafts.values.single())
            assertEquals(0, repository.active()!!.exercises.sumOf { it.sets.size })
            database.openHelper.writableDatabase.execSQL("DROP TRIGGER b04_ui_reject")
            click("Save Set")
            waitFor("Set saved")
            click("Back to Exercises")
            click("${BuiltInCatalogue.exercises[0].name} · No saved sets")
            click("Save Set")
            waitFor("Set saved")
            click("Back to Exercises")
            click("${BuiltInCatalogue.exercises[1].name} · Partial · 1 saved")
            compose.onNodeWithText("1 · WORKING · 12.345 kg · 8 reps").performScrollTo().assertIsDisplayed()
            click("Details")
            compose.onNodeWithText(BuiltInCatalogue.exercises[1].description).assertIsDisplayed()
            compose.onNodeWithText("Close").performClick()
            val sets = repository.active()!!.exercises.flatMap { it.sets }.sortedBy { it.recordingOrder }
            assertEquals(2, sets.size)
            assertTrue(sets[0].exerciseId != sets[1].exerciseId)
        } finally {
            compose.activityRule.scenario.close()
            database.close()
            context.deleteDatabase(name)
        }
    }

    private fun waitFor(text: String) {
        compose.waitUntil(10_000) { compose.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty() }
    }
    private fun click(text: String) {
        compose.onNodeWithText(text).performScrollTo().performClick()
    }
}
