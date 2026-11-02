package com.quantumfighter.gymtracker

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import com.quantumfighter.gymtracker.data.exercise.BuiltInCatalogue
import com.quantumfighter.gymtracker.data.exercise.ExerciseRepository
import com.quantumfighter.gymtracker.data.exercise.GymDatabase
import com.quantumfighter.gymtracker.data.program.ProgramRepository
import com.quantumfighter.gymtracker.data.settings.UiPreferencesRepository
import com.quantumfighter.gymtracker.data.workout.WorkoutRepository
import com.quantumfighter.gymtracker.domain.RoutineExerciseDraft
import com.quantumfighter.gymtracker.domain.SaveSetCommand
import com.quantumfighter.gymtracker.domain.SetTarget
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.UUID

/** T-03/T-11: all v4 content survives migration; preference and cursor cannot overwrite each other. */
class UiPreferencesDatabaseTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val name = "ui-preferences-test.db"
    private val oldName = "ui-preferences-v4-test.db"
    private lateinit var database: GymDatabase

    @get:Rule val migration = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), GymDatabase::class.java, emptyList(), FrameworkSQLiteOpenHelperFactory())

    @Before fun open() {
        context.deleteDatabase(name)
        context.deleteDatabase(oldName)
        database = GymDatabase.build(context, name)
    }

    @After fun close() {
        database.close()
        context.deleteDatabase(name)
        context.deleteDatabase(oldName)
    }
    private suspend fun seed(): List<String> {
        ExerciseRepository(database).seedCatalogue()
        val plans = ProgramRepository(database)
        val program = plans.createProgram("Synthetic preserved program")
        return (1..2).map { plans.createRoutine(program, "Routine $it", listOf(RoutineExerciseDraft(UUID.randomUUID().toString(), BuiltInCatalogue.exercises.first().id, listOf(SetTarget(8, 12, 12345, 90))))) }
    }

    @Test fun t11AdditiveV4MigrationPreservesCataloguePlansActiveAndFinishedResults() = runBlocking {
        val memberships = seed()
        val workouts = WorkoutRepository(database)
        val finishedId = workouts.start(memberships[0])
        workouts.warmup(false)
        workouts.save(SaveSetCommand(UUID.randomUUID().toString(), workouts.active()!!.exercises.first().exercise.id, 12345, 9, "WORKING"))
        workouts.warmdown(finishedId, true)
        workouts.finish(finishedId)
        workouts.start(memberships[1])
        val finished = workouts.finished(finishedId)
        val active = workouts.active()
        val plans = database.programDao().programs()
        val cursor = database.programDao().settings()
        val catalogue = database.exerciseDao().all()
        database.close()
        migration.createDatabase(oldName, 4).use { old ->
            old.execSQL("ATTACH DATABASE ? AS seeded", arrayOf(context.getDatabasePath(name).path))
            listOf("exercises", "programs", "routines", "program_routines", "routine_exercises", "set_prescriptions", "app_settings", "workout_sessions", "session_exercises", "session_prescriptions", "recorded_sets", "active_workout").forEach { old.execSQL("INSERT INTO $it SELECT * FROM seeded.$it") }
            old.execSQL("DETACH DATABASE seeded")
        }
        migration.runMigrationsAndValidate(oldName, 5, true, GymDatabase.MIGRATION_4_5).close()
        val upgraded = GymDatabase.build(context, oldName)
        try {
            assertEquals(catalogue, upgraded.exerciseDao().all())
            assertEquals(plans, upgraded.programDao().programs())
            assertEquals(cursor, upgraded.programDao().settings())
            assertEquals(finished, WorkoutRepository(upgraded).finished(finishedId))
            assertEquals(active, WorkoutRepository(upgraded).active())
            assertFalse(UiPreferencesRepository(upgraded).observeHiddenCompleted().first())
            upgraded.openHelper.readableDatabase.query("PRAGMA foreign_key_check").use { assertFalse(it.moveToFirst()) }
        } finally {
            upgraded.close()
        }
    }

    @Test fun t03PreferenceSurvivesReopenSelectionAndFinalisationWithoutChangingRecordedWork() = runBlocking {
        val memberships = seed()
        val preferences = UiPreferencesRepository(database)
        assertFalse(preferences.observeHiddenCompleted().first())
        preferences.hideCompleted(true)
        val plans = ProgramRepository(database)
        plans.selectNextRoutine(memberships[1])
        val workouts = WorkoutRepository(database)
        val id = workouts.start(memberships[1])
        workouts.warmup(false)
        val set = workouts.save(SaveSetCommand(UUID.randomUUID().toString(), workouts.active()!!.exercises.first().exercise.id, null, 11, "WORKING"))
        workouts.warmdown(id, true)
        workouts.finish(id)
        assertTrue(preferences.observeHiddenCompleted().first())
        assertEquals(memberships[0], plans.readLibrary().nextMembershipId)
        val finished = workouts.finished(id)
        assertEquals(set, finished!!.exercises.single().sets.single())
        database.close()
        database = GymDatabase.build(context, name)
        assertTrue(UiPreferencesRepository(database).observeHiddenCompleted().first())
        assertEquals(finished, WorkoutRepository(database).finished(id))
        assertEquals(memberships[0], ProgramRepository(database).readLibrary().nextMembershipId)
    }
}
