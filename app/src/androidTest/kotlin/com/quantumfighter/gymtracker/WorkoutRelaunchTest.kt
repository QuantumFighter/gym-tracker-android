package com.quantumfighter.gymtracker

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.platform.app.InstrumentationRegistry
import com.quantumfighter.gymtracker.app.GymTrackerApp
import com.quantumfighter.gymtracker.data.exercise.BuiltInCatalogue
import com.quantumfighter.gymtracker.data.exercise.ExerciseRepository
import com.quantumfighter.gymtracker.data.exercise.GymDatabase
import com.quantumfighter.gymtracker.data.program.ProgramRepository
import com.quantumfighter.gymtracker.data.workout.WorkoutRepository
import com.quantumfighter.gymtracker.domain.RoutineExerciseDraft
import com.quantumfighter.gymtracker.domain.SaveSetCommand
import com.quantumfighter.gymtracker.domain.SetTarget
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.util.UUID

/** Host runs seed, force-stop, launcher, verify in separate app processes. */
class WorkoutRelaunchTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun t06CommittedSetsSurviveProcessDeathAndRetry() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val phase = InstrumentationRegistry.getArguments().getString("workoutPhase")
        val name = "b04-relaunch-test.db"
        val prefs = context.getSharedPreferences("b04-relaunch-test", 0)
        if (phase == null || phase == "seed") context.deleteDatabase(name)
        val database = GymDatabase.build(context, name)
        try {
            val repository = WorkoutRepository(database)
            if (phase == null || phase == "seed") {
                ExerciseRepository(database).seedCatalogue()
                val plans = ProgramRepository(database)
                val member = plans.createRoutine(plans.createProgram("Recovery fixture"), "Recovery routine", BuiltInCatalogue.exercises.take(2).map { RoutineExerciseDraft(UUID.randomUUID().toString(), it.id, listOf(SetTarget(8, 12, null, 90))) })
                repository.start(member)
                repository.warmup(false)
                repository.active()!!.exercises.sortedByDescending { it.exercise.ordinal }.forEachIndexed { index, exercise ->
                    repository.save(SaveSetCommand(UUID.randomUUID().toString(), exercise.exercise.id, if (index == 0) 0 else 12345, 8 + index, "WORKING"))
                }
                assertTrue(prefs.edit().putString("snapshot", repository.active().toString()).commit())
            }
            if (phase == "seed") return@runBlocking
            val expected = prefs.getString("snapshot", null)
            assertNotNull("Seed required; missing records must never be recreated", expected)
            assertEquals(expected, repository.active().toString())
            val active = repository.active()!!
            assertEquals(active.session.id, repository.start("source no longer needed"))
            val saved = active.exercises.flatMap { it.sets }.first()
            assertEquals(saved, repository.save(SaveSetCommand(saved.commandId, saved.exerciseId, saved.loadGrams, saved.repetitions, saved.setType, saved.unit)))
            assertEquals(expected, repository.active().toString())
            compose.setContent { GymTrackerApp(ExerciseRepository(database), ProgramRepository(database), repository) }
            compose.waitUntil(10_000) { compose.onAllNodes(hasText("Resume workout")).fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText("Resume workout").performScrollTo().performClick()
            val name = active.exercises.single { it.exercise.id == saved.exerciseId }.exercise.definition.name
            compose.onNodeWithText("$name · Partial · 1 saved").performScrollTo().performClick()
            compose.waitUntil(10_000) { compose.onAllNodes(hasText("Saved sets")).fetchSemanticsNodes().isNotEmpty() }
            assertEquals(2, repository.active()!!.exercises.sumOf { it.sets.size })
        } finally {
            compose.activityRule.scenario.close()
            database.close()
            if (phase != "seed") {
                context.deleteDatabase(name)
                prefs.edit().clear().commit()
            }
        }
    }
}
