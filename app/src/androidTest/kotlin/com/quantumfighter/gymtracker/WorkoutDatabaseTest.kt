package com.quantumfighter.gymtracker

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import com.quantumfighter.gymtracker.data.exercise.BuiltInCatalogue
import com.quantumfighter.gymtracker.data.exercise.ExerciseRepository
import com.quantumfighter.gymtracker.data.exercise.GymDatabase
import com.quantumfighter.gymtracker.data.program.ProgramRepository
import com.quantumfighter.gymtracker.data.workout.WorkoutRepository
import com.quantumfighter.gymtracker.domain.RoutineExerciseDraft
import com.quantumfighter.gymtracker.domain.SaveSetCommand
import com.quantumfighter.gymtracker.domain.SetTarget
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.UUID

class WorkoutDatabaseTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val name = "b04-room-test.db"
    private lateinit var database: GymDatabase
    private var tick = 1000L
    private val repository get() = WorkoutRepository(database) { tick }

    @get:Rule val migration = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), GymDatabase::class.java, emptyList(), FrameworkSQLiteOpenHelperFactory())

    @Before fun open() {
        context.deleteDatabase(name)
        database = GymDatabase.build(context, name)
    }

    @After fun close() {
        database.close()
        context.deleteDatabase(name)
    }

    private suspend fun prepare(): String {
        ExerciseRepository(database).seedCatalogue()
        val plans = ProgramRepository(database)
        val program = plans.createProgram("Synthetic B04")
        return plans.createRoutine(
            program,
            "Out of order",
            BuiltInCatalogue.exercises.take(2).map {
                RoutineExerciseDraft(UUID.randomUUID().toString(), it.id, listOf(SetTarget(8, 12, null, 90), SetTarget(4, 6, 12345, 0)))
            },
        )
    }

    @Test fun t01SnapshotCopiesWholeGraphAndConcurrentStartResumes() = runBlocking {
        val membership = prepare()
        val ids = coroutineScope { (1..8).map { async { repository.start(membership) } }.awaitAll() }
        assertEquals(1, ids.distinct().size)
        val snapshot = requireNotNull(repository.active())
        assertEquals(BuiltInCatalogue.exercises.take(2), snapshot.exercises.sortedBy { it.exercise.ordinal }.map { it.exercise.definition })
        val original = database.programDao().programs().single().memberships.single().routine.exercises.sortedBy { it.occurrence.ordinal }
        snapshot.exercises.sortedBy { it.exercise.ordinal }.zip(original).forEach { (saved, source) ->
            assertEquals(source.occurrence.id, saved.exercise.sourceOccurrenceId)
            assertEquals(source.sets.sortedBy { it.ordinal }.map { listOf(it.id, it.repMin, it.repMax, it.loadGrams, it.restSeconds, it.setType) }, saved.prescriptions.sortedBy { it.ordinal }.map { listOf(it.sourcePrescriptionId, it.repMin, it.repMax, it.loadGrams, it.restSeconds, it.setType) })
        }
        database.openHelper.writableDatabase.execSQL("UPDATE exercises SET name = 'Changed', description = 'Changed metadata'")
        database.openHelper.writableDatabase.execSQL("UPDATE set_prescriptions SET repMax = 99")
        database.openHelper.writableDatabase.execSQL("UPDATE routines SET name = 'Changed routine'")
        assertEquals(snapshot, repository.active())
        assertEquals(ids.first(), repository.start("unavailable source still resumes"))
    }

    @Test fun t02AtomicIdempotentConcurrentSavesAndReopen() = runBlocking {
        repository.start(prepare())
        repository.warmup(true)
        tick = 5432
        repository.warmup(false)
        assertEquals(4432L, repository.active()!!.session.warmupElapsedMs)
        val exercises = repository.active()!!.exercises.sortedBy { it.exercise.ordinal }
        val command = SaveSetCommand(UUID.randomUUID().toString(), exercises[1].exercise.id, 0, 9, "WORKING")
        val rows = coroutineScope { (1..12).map { async { repository.save(command) } }.awaitAll() }
        assertEquals(1, rows.distinct().size)
        repository.save(command.copy(id = UUID.randomUUID().toString(), exerciseId = exercises[0].exercise.id, loadGrams = null))
        repository.save(command.copy(id = UUID.randomUUID().toString(), loadGrams = 12345, setType = "WARM_UP"))
        val before = repository.active()!!
        assertEquals(listOf(0L, null, 12345L), before.exercises.flatMap { it.sets }.sortedBy { it.recordingOrder }.map { it.loadGrams })
        assertEquals(listOf(1, 2), before.exercises.single { it.exercise.id == command.exerciseId }.sets.sortedBy { it.sequence }.map { it.sequence })
        assertTrue(runCatching { repository.save(command.copy(repetitions = 10)) }.isFailure)
        assertTrue(runCatching { repository.save(command.copy(id = UUID.randomUUID().toString(), exerciseId = UUID.randomUUID().toString())) }.isFailure)
        database.close()
        database = GymDatabase.build(context, name)
        assertEquals(before, repository.active())
        assertEquals(rows.first(), repository.save(command))
    }

    @Test fun t02FailedInsertRollsBackAndSameCommandRemainsRetryable() = runBlocking {
        repository.start(prepare())
        repository.warmup(false)
        val before = repository.active()!!
        val command = SaveSetCommand(UUID.randomUUID().toString(), before.exercises.first().exercise.id, 1, 8, "WORKING")
        database.openHelper.writableDatabase.execSQL("CREATE TRIGGER b04_reject BEFORE INSERT ON recorded_sets BEGIN SELECT RAISE(ABORT, 'synthetic failure'); END")
        assertTrue(runCatching { repository.save(command) }.isFailure)
        assertEquals(before, repository.active())
        database.openHelper.writableDatabase.execSQL("DROP TRIGGER b04_reject")
        assertEquals(command.id, repository.save(command).commandId)
        assertEquals(1, repository.active()!!.exercises.sumOf { it.sets.size })
    }

    @Test fun t02InvalidSnapshotRollsBackEntireSessionAndDistinctConcurrentCommandsAreOrdered() = runBlocking {
        val member = prepare()
        database.openHelper.writableDatabase.execSQL("UPDATE set_prescriptions SET repMin = 0")
        assertTrue(runCatching { repository.start(member) }.isFailure)
        assertEquals(null, repository.active())
        database.openHelper.readableDatabase.query("SELECT COUNT(*) FROM workout_sessions").use {
            assertTrue(it.moveToFirst())
            assertEquals(0, it.getInt(0))
        }
        database.openHelper.writableDatabase.execSQL("UPDATE set_prescriptions SET repMin = 1")
        repository.start(member)
        repository.warmup(false)
        val id = repository.active()!!.exercises.first().exercise.id
        coroutineScope { (1..8).map { reps -> async { repository.save(SaveSetCommand(UUID.randomUUID().toString(), id, null, reps, "WORKING")) } }.awaitAll() }
        val sets = repository.active()!!.exercises.single { it.exercise.id == id }.sets
        assertEquals((1..8).toList(), sets.map { it.sequence }.sorted())
        assertEquals((1..8).toList(), sets.map { it.repetitions }.sorted())
        assertEquals(8, sets.map { it.commandId }.distinct().size)
    }

    @Test fun t11MigrationPreservesCataloguePlansAndCursor() = runBlocking {
        prepare()
        val catalogue = database.exerciseDao().all()
        val plans = database.programDao().programs()
        val cursor = database.programDao().settings()
        database.close()
        // Build a genuine v2 file by copying only the old tables through exported schema SQL.
        val oldName = "b04-v2-test.db"
        context.deleteDatabase(oldName)
        migration.createDatabase(oldName, 2).use { old ->
            old.execSQL("ATTACH DATABASE ? AS seeded", arrayOf(context.getDatabasePath(name).path))
            listOf("exercises", "programs", "routines", "program_routines", "routine_exercises", "set_prescriptions", "app_settings").forEach { old.execSQL("INSERT INTO $it SELECT * FROM seeded.$it") }
            old.execSQL("DETACH DATABASE seeded")
        }
        migration.runMigrationsAndValidate(oldName, 3, true, GymDatabase.MIGRATION_2_3).close()
        val upgraded = GymDatabase.build(context, oldName)
        try {
            assertEquals(catalogue, upgraded.exerciseDao().all())
            assertEquals(plans, upgraded.programDao().programs())
            assertEquals(cursor, upgraded.programDao().settings())
            upgraded.openHelper.readableDatabase.query("PRAGMA foreign_key_check").use { assertFalse(it.moveToFirst()) }
            upgraded.openHelper.readableDatabase.query("PRAGMA integrity_check").use {
                assertTrue(it.moveToFirst())
                assertEquals("ok", it.getString(0))
            }
        } finally {
            upgraded.close()
        }
        context.deleteDatabase(oldName)
        Unit
    }
}
