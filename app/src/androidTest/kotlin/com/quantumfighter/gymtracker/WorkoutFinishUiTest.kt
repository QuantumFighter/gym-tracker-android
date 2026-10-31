package com.quantumfighter.gymtracker

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.util.UUID

/** M1 demonstration uses real Compose navigation and isolated synthetic Room data only. */
class WorkoutFinishUiTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun t01ManualWorkoutFinishAndHistoryAfterProcessDeath() = runBlocking {
        val phase = InstrumentationRegistry.getArguments().getString("finishPhase")
        val name = "b05-m1-test.db"
        val prefs = context.getSharedPreferences("b05-m1-test", 0)
        val ready = java.io.File(context.filesDir, "b05-kill-ready")
        val cleanup = java.io.File(context.filesDir, "b05-kill-cleanup")
        val abrupt = phase == "abrupt"
        if (phase != "verify") context.deleteDatabase(name)
        val database = GymDatabase.build(context, name)
        val repository = WorkoutRepository(database)
        try {
            compose.setContent { GymTrackerApp(ExerciseRepository(database), ProgramRepository(database), repository) }
            waitFor("Programs")
            if (phase != "verify") {
                click("Programs")
                click("Create program")
                input("Program name", "M1 synthetic program")
                click("Save program")
                waitFor("Add routine to M1 synthetic program")
                click("Add routine to M1 synthetic program")
                input("Routine name", "M1 routine")
                click("Add exercise")
                click(BuiltInCatalogue.exercises[0].name)
                click("Add exercise")
                click(BuiltInCatalogue.exercises[1].name)
                click("Save routine")
                waitFor("Saved routine")
                click("Home")
                waitFor("Record Workout")
                click("Record Workout")
                waitFor("Start Warm-up")
                click("Start Warm-up")
                click("Stop Warm-up")
                waitFor("Exercises")
                click("${BuiltInCatalogue.exercises[0].name} · No saved sets")
                input("Weight (kg; blank is unset)", "12.345")
                click("Save Set")
                waitFor("Set saved")
                click("Back to Exercises")
                click("${BuiltInCatalogue.exercises[1].name} · No saved sets")
                input("Weight (kg; blank is unset)", "0")
                input("Repetitions", "9")
                click("Save Set")
                waitFor("Set saved")
                click("Back to Exercises")
                click("${BuiltInCatalogue.exercises[0].name} · Partial · 1 saved")
                input("Repetitions", "10")
                click("Save Set")
                waitFor("Set saved")
                click("Complete Exercise")
                waitFor("Exercises")
                compose.onNodeWithText("${BuiltInCatalogue.exercises[0].name} · Complete · 2 saved").performScrollTo().assertIsDisplayed()
                click("${BuiltInCatalogue.exercises[1].name} · Partial · 1 saved")
                click("Complete Exercise")
                waitFor("Exercises")
                click("Warm Down")
                waitFor("Warm-down")
                click("Start Warm-down Timer")
                waitFor("Stop Warm-down Timer")
                click("Stop Warm-down Timer")
                click("Finish Workout")
                waitFor("Workout finished")
                assertNull(repository.active())
                val saved = repository.observeHistory().first().single()
                val snapshot = repository.finished(saved.id)!!
                assertEquals(listOf(8, 9, 10), snapshot.exercises.flatMap { it.sets }.sortedBy { it.recordingOrder }.map { it.repetitions })
                assertEquals(listOf(12345L, 0L, 12345L), snapshot.exercises.flatMap { it.sets }.sortedBy { it.recordingOrder }.map { it.loadGrams })
                assertTrue(snapshot.exercises.all { it.exercise.status == "COMPLETE" })
                assertTrue(prefs.edit().putString("id", saved.id).putString("snapshot", snapshot.toString()).commit())
                database.openHelper.writableDatabase.execSQL("UPDATE routines SET name = 'Changed source'")
                database.openHelper.writableDatabase.execSQL("UPDATE exercises SET name = 'Changed source', description = 'Changed source'")
                database.openHelper.writableDatabase.execSQL("UPDATE set_prescriptions SET repMax = 99")
                if (abrupt) {
                    cleanup.delete()
                    ready.writeText(android.os.Process.myPid().toString())
                    delay(120_000)
                    error("Host did not kill the finalised process")
                }
                if (phase == "seed") return@runBlocking
                click("Home")
            }
            val id = requireNotNull(prefs.getString("id", null))
            assertEquals(prefs.getString("snapshot", null), repository.finished(id).toString())
            assertNull(repository.active())
            click("Workout history")
            compose.waitUntil(10_000) { compose.onAllNodes(hasText("M1 routine ·", substring = true)).fetchSemanticsNodes().isNotEmpty() }
            compose.onNode(hasText("M1 routine ·", substring = true)).performScrollTo().performClick()
            waitFor("Finished · Read-only")
            listOf("1 · WORKING · 12.345 kg · 8 reps", "2 · WORKING · 12.345 kg · 10 reps", "1 · WORKING · 0 kg · 9 reps").forEach { compose.onNodeWithText(it).performScrollTo().assertIsDisplayed() }
            compose.onNodeWithText("${BuiltInCatalogue.exercises[0].name} · Complete").performScrollTo().assertIsDisplayed()
            compose.onNodeWithText("${BuiltInCatalogue.exercises[1].name} · Complete").performScrollTo().assertIsDisplayed()
            assertEquals(0, compose.onAllNodes(hasText("Save Set")).fetchSemanticsNodes().size)
            assertEquals(prefs.getString("snapshot", null), repository.finished(id).toString())
        } finally {
            if (abrupt) cleanup.writeText("Orderly cleanup ran")
            compose.activityRule.scenario.close()
            database.close()
            if (phase != "seed") {
                context.deleteDatabase(name)
                prefs.edit().clear().commit()
                ready.delete()
                cleanup.delete()
            }
        }
    }

    @Test fun t03EarlyWarmdownCorrectionsBacktrackingAndFailedFinish() = runBlocking {
        val name = "b05-early-ui-test.db"
        context.deleteDatabase(name)
        val database = GymDatabase.build(context, name)
        val repository = WorkoutRepository(database)
        try {
            ExerciseRepository(database).seedCatalogue()
            val plans = ProgramRepository(database)
            val member = plans.createRoutine(plans.createProgram("Early fixture"), "Early routine", BuiltInCatalogue.exercises.take(3).map { RoutineExerciseDraft(UUID.randomUUID().toString(), it.id, listOf(SetTarget(8, 12, 12345, 90))) })
            repository.start(member)
            compose.setContent { GymTrackerApp(ExerciseRepository(database), plans, repository) }
            waitFor("Resume workout")
            click("Resume workout")
            waitFor("Start Warm-up")
            click("Warm Down")
            waitFor("Unfinished exercises remain")
            compose.onNodeWithText("Keep Recording").performClick()
            click("Stop Warm-up")
            waitFor("Exercises")
            click("${BuiltInCatalogue.exercises[0].name} · No saved sets")
            click("Save Set")
            waitFor("Set saved")
            click("Correct set 1")
            compose.onNode(hasText("Warm Down") and hasAnyAncestor(isDialog())).performClick()
            waitFor("Unfinished exercises remain")
            compose.onNodeWithText("Keep Recording").performClick()
            // Details is itself an active subview with a direct Warm Down action.
            click("Details")
            compose.onNode(hasText("Warm Down") and hasAnyAncestor(isDialog())).performClick()
            waitFor("Unfinished exercises remain")
            compose.onNodeWithText("Enter Warm-down").performClick()
            waitFor("Warm-down")
            assertEquals(listOf("PARTIAL", "SKIPPED", "SKIPPED"), repository.active()!!.exercises.sortedBy { it.exercise.ordinal }.map { it.exercise.status })
            click("Review ${BuiltInCatalogue.exercises[0].name}")
            click("Correct set 1")
            input("Corrected repetitions", "11")
            database.openHelper.writableDatabase.execSQL("CREATE TRIGGER b05_correct_reject BEFORE UPDATE ON recorded_sets BEGIN SELECT RAISE(ABORT, 'synthetic failure'); END")
            compose.onNodeWithText("Save Correction").performClick()
            waitFor("Could not correct set. Previous saved values are retained.")
            assertEquals(8, repository.active()!!.exercises.flatMap { it.sets }.single().repetitions)
            database.openHelper.writableDatabase.execSQL("DROP TRIGGER b05_correct_reject")
            compose.onNodeWithText("Save Correction").performClick()
            waitFor("Correction saved")
            assertEquals(11, repository.active()!!.exercises.flatMap { it.sets }.single().repetitions)
            click("Back to Exercises")
            click("Return to Recording")
            waitFor("Exercises")
            assertEquals("PENDING", repository.active()!!.exercises.single { it.exercise.ordinal == 2 }.exercise.status)
            click("${BuiltInCatalogue.exercises[0].name} · Partial · 1 saved")
            click("Complete Exercise")
            waitFor("Exercises")
            click("${BuiltInCatalogue.exercises[1].name} · No saved sets")
            click("Save Set")
            waitFor("Set saved")
            click("Warm Down")
            waitFor("Unfinished exercises remain")
            compose.onNodeWithText("Enter Warm-down").performClick()
            waitFor("Warm-down")
            database.openHelper.writableDatabase.execSQL("CREATE TRIGGER b05_finish_reject BEFORE DELETE ON active_workout BEGIN SELECT RAISE(ABORT, 'synthetic failure'); END")
            click("Finish Workout")
            waitFor("Retry Finish Workout")
            assertNotNull(repository.active())
            assertTrue(repository.observeHistory().first().isEmpty())
            assertEquals(0, compose.onAllNodes(hasText("Workout finished")).fetchSemanticsNodes().size)
            database.openHelper.writableDatabase.execSQL("DROP TRIGGER b05_finish_reject")
            val id = repository.active()!!.session.id
            click("Retry Finish Workout")
            waitFor("Workout finished")
            assertEquals(listOf("COMPLETE", "PARTIAL", "SKIPPED"), repository.finished(id)!!.exercises.sortedBy { it.exercise.ordinal }.map { it.exercise.status })
            click("Workout history")
            compose.waitUntil(10_000) { compose.onAllNodes(hasText("Early routine ·", substring = true)).fetchSemanticsNodes().isNotEmpty() }
            compose.onNode(hasText("Early routine ·", substring = true)).performScrollTo().performClick()
            waitFor("Finished · Read-only")
            listOf("Complete", "Partial", "Skipped").forEachIndexed { i, status -> compose.onNodeWithText("${BuiltInCatalogue.exercises[i].name} · $status").performScrollTo().assertIsDisplayed() }
        } finally {
            compose.activityRule.scenario.close()
            database.close()
            context.deleteDatabase(name)
        }
    }

    @Test fun t06WarmdownBeforeFinishSurvivesProcessDeath() = runBlocking {
        val phase = InstrumentationRegistry.getArguments().getString("warmdownPhase")
        val name = "b05-pending-test.db"
        val prefs = context.getSharedPreferences("b05-pending-test", 0)
        val ready = java.io.File(context.filesDir, "b05-pending-ready")
        val cleanup = java.io.File(context.filesDir, "b05-pending-cleanup")
        val abrupt = phase == "abrupt"
        if (phase != "verify") context.deleteDatabase(name)
        val database = GymDatabase.build(context, name)
        val repository = WorkoutRepository(database)
        try {
            if (phase != "verify") {
                ExerciseRepository(database).seedCatalogue()
                val plans = ProgramRepository(database)
                val member = plans.createRoutine(plans.createProgram("Pending fixture"), "Pending routine", BuiltInCatalogue.exercises.take(3).map { RoutineExerciseDraft(UUID.randomUUID().toString(), it.id, listOf(SetTarget(8, 12, 12345, 90))) })
                val id = repository.start(member)
                repository.warmup(false)
                val exercise = repository.active()!!.exercises.sortedBy { it.exercise.ordinal }
                repository.save(SaveSetCommand(UUID.randomUUID().toString(), exercise[0].exercise.id, 12345, 8, "WORKING"))
                repository.complete(id, exercise[0].exercise.id)
                repository.save(SaveSetCommand(UUID.randomUUID().toString(), exercise[1].exercise.id, 0, 9, "WORKING"))
                repository.warmdown(id, true)
                assertTrue(prefs.edit().putString("snapshot", repository.active().toString()).putString("cursor", member).commit())
            }
            assertEquals(prefs.getString("snapshot", null), repository.active().toString())
            assertEquals(prefs.getString("cursor", null), database.programDao().settings()!!.nextProgramRoutineId)
            compose.setContent { GymTrackerApp(ExerciseRepository(database), ProgramRepository(database), repository) }
            waitFor("Resume workout")
            click("Resume workout")
            waitFor("Warm-down")
            if (abrupt) {
                cleanup.delete()
                ready.writeText(android.os.Process.myPid().toString())
                delay(120_000)
                error("Host did not kill the pending finalisation process")
            }
            val id = repository.active()!!.session.id
            click("Review ${BuiltInCatalogue.exercises[1].name}")
            click("Correct set 1")
            input("Corrected repetitions", "12")
            compose.onNodeWithText("Save Correction").performClick()
            waitFor("Correction saved")
            click("Back to Exercises")
            click("Return to Recording")
            waitFor("Exercises")
            assertEquals(listOf("COMPLETE", "PARTIAL", "PENDING"), repository.active()!!.exercises.sortedBy { it.exercise.ordinal }.map { it.exercise.status })
            click("Warm Down")
            waitFor("Unfinished exercises remain")
            compose.onNodeWithText("Enter Warm-down").performClick()
            waitFor("Warm-down")
            click("Finish Workout")
            waitFor("Workout finished")
            val finished = repository.finished(id)!!
            assertEquals(listOf("COMPLETE", "PARTIAL", "SKIPPED"), finished.exercises.sortedBy { it.exercise.ordinal }.map { it.exercise.status })
            assertEquals(listOf(8, 12), finished.exercises.flatMap { it.sets }.sortedBy { it.recordingOrder }.map { it.repetitions })
        } finally {
            if (abrupt) cleanup.writeText("Orderly cleanup ran")
            compose.activityRule.scenario.close()
            database.close()
            context.deleteDatabase(name)
            prefs.edit().clear().commit()
            ready.delete()
            cleanup.delete()
        }
    }

    private fun waitFor(text: String) {
        compose.waitUntil(10_000) { compose.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty() }
    }
    private fun click(text: String) {
        val node = compose.onNodeWithText(text)
        if (text != "Warm Down") node.performScrollTo()
        node.performClick()
    }
    private fun input(label: String, value: String) {
        compose.onNodeWithText(label).performScrollTo().performTextReplacement(value)
    }
}
