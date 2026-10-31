package com.quantumfighter.gymtracker

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.quantumfighter.gymtracker.app.GymTrackerApp
import com.quantumfighter.gymtracker.data.exercise.BuiltInCatalogue
import com.quantumfighter.gymtracker.data.exercise.ExerciseRepository
import com.quantumfighter.gymtracker.data.exercise.GymDatabase
import com.quantumfighter.gymtracker.data.program.ProgramRepository
import com.quantumfighter.gymtracker.domain.RoutineExerciseDraft
import com.quantumfighter.gymtracker.domain.SetTarget
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

/** Two external instrumentation phases prove the saved graph and Home cursor survive process death. */
@RunWith(AndroidJUnit4::class)
class ProgramRelaunchTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun t01SavedRoutineAndHomeSelectionSurviveProcessRestart() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val phase = InstrumentationRegistry.getArguments().getString("programPhase")
        val name = "b03-relaunch-test.db"
        val preferences = context.getSharedPreferences("b03-relaunch-test", 0)
        if (phase == null || phase == "seed") context.deleteDatabase(name)
        var database = GymDatabase.build(context, name)
        try {
            if (phase == null || phase == "seed") {
                ExerciseRepository(database).seedCatalogue()
                val repository = ProgramRepository(database)
                val program = repository.createProgram("Restart sentinel program")
                repository.createRoutine(program, "First routine", listOf(draft(BuiltInCatalogue.exercises[1].id)))
                val next = repository.createRoutine(program, "Restart sentinel routine", listOf(draft(), draft()))
                repository.selectNextRoutine(next)
                database.openHelper.writableDatabase.execSQL("UPDATE exercises SET description = 'Preserved restart metadata' WHERE id = ?", arrayOf(BuiltInCatalogue.exercises.first().id))
                assertTrue(preferences.edit().putString("snapshot", snapshot(database)).commit())
            }
            if (phase == "seed") return@runBlocking
            database.close()
            database = GymDatabase.build(context, name)
            val expected = preferences.getString("snapshot", null)
            assertNotNull("Seed phase must run before verification", expected)
            // No catalogue initialization before this comparison. Lost rows cannot be reconstructed.
            assertEquals(expected, snapshot(database))
            compose.setContent { GymTrackerApp(ExerciseRepository(database), ProgramRepository(database)) }
            compose.waitUntil(10_000) { compose.onAllNodes(hasText("Restart sentinel routine")).fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText("Restart sentinel routine").performScrollTo().assertIsDisplayed()
            assertEquals(expected, snapshot(database))
        } finally {
            compose.activityRule.scenario.close()
            database.close()
            if (phase != "seed") {
                context.deleteDatabase(name)
                assertTrue(preferences.edit().clear().commit())
            }
        }
    }

    private fun draft(exerciseId: String = BuiltInCatalogue.exercises.first().id) = RoutineExerciseDraft(
        UUID.randomUUID().toString(),
        exerciseId,
        listOf(SetTarget(6, 10, null, 90), SetTarget(8, 12, 0, 0), SetTarget(10, 15, 1250, 120)),
    )

    private suspend fun snapshot(database: GymDatabase): String = listOf(
        database.exerciseDao().all(),
        database.programDao().programs(),
        database.programDao().settings(),
    ).toString()
}
