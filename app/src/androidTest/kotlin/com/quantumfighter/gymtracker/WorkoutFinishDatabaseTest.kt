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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.UUID

/** B-05 / T-01, T-06, T-07, T-11: actual SQLite serialization, rollback and immutable history. */
class WorkoutFinishDatabaseTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val name = "b05-room-test.db"
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

    private suspend fun prepare(): List<String> {
        ExerciseRepository(database).seedCatalogue()
        val plans = ProgramRepository(database)
        val program = plans.createProgram("Synthetic B05")
        return (1..3).map { index -> plans.createRoutine(program, "Routine $index", BuiltInCatalogue.exercises.take(3).map { RoutineExerciseDraft(UUID.randomUUID().toString(), it.id, listOf(SetTarget(8, 12, 12345, 90))) }) }
    }
    private suspend fun save(exercise: String, reps: Int = 8) = repository.save(SaveSetCommand(UUID.randomUUID().toString(), exercise, 12345, reps, "WORKING"))

    @Test fun t01CompleteWarmdownBacktrackAndOptionalTimerPreserveEverySet() = runBlocking {
        val id = repository.start(prepare()[0])
        repository.warmup(true)
        tick = 2000
        repository.warmup(false)
        val exercises = repository.active()!!.exercises.sortedBy { it.exercise.ordinal }
        save(exercises[0].exercise.id)
        save(exercises[1].exercise.id)
        repository.complete(id, exercises[0].exercise.id)
        val before = repository.active()!!
        assertTrue(runCatching { repository.warmdown(id, false) }.isFailure)
        assertEquals(before, repository.active())
        repository.startRest(exercises[1].exercise.id)
        repository.warmdown(id, true)
        assertEquals(listOf("COMPLETE", "PARTIAL", "SKIPPED"), repository.active()!!.exercises.sortedBy { it.exercise.ordinal }.map { it.exercise.status })
        assertNull(repository.active()!!.session.restAnchorMs)
        assertEquals(before.exercises.flatMap { it.sets }, repository.active()!!.exercises.flatMap { it.sets })
        assertTrue(runCatching { save(exercises[1].exercise.id) }.isFailure)
        repository.warmdownTimer(id, true)
        tick = 4500
        repository.returnToRecording(id)
        assertEquals(2500L, repository.active()!!.session.warmdownElapsedMs)
        assertEquals(listOf("COMPLETE", "PARTIAL", "PENDING"), repository.active()!!.exercises.sortedBy { it.exercise.ordinal }.map { it.exercise.status })
        assertEquals(before.exercises.flatMap { it.sets }, repository.active()!!.exercises.flatMap { it.sets })
    }

    @Test fun t06CorrectionsAreAtomicRetryableAndReadOnlyAfterFinish() = runBlocking {
        val id = repository.start(prepare()[0])
        repository.warmup(false)
        val exercise = repository.active()!!.exercises.first().exercise.id
        val original = save(exercise)
        repository.complete(id, exercise)
        repository.warmdown(id, true)
        val before = repository.active()
        database.openHelper.writableDatabase.execSQL("CREATE TRIGGER b05_reject BEFORE UPDATE ON recorded_sets BEGIN SELECT RAISE(ABORT, 'synthetic failure'); END")
        assertTrue(runCatching { repository.correct(original, 0, 12, "WARM_UP") }.isFailure)
        assertEquals(before, repository.active())
        database.openHelper.writableDatabase.execSQL("DROP TRIGGER b05_reject")
        val corrected = repository.correct(original, 0, 12, "WARM_UP")
        assertEquals(corrected, repository.correct(original, 0, 12, "WARM_UP"))
        assertTrue(runCatching { repository.correct(original, 1, 3, "WORKING") }.isFailure)
        assertEquals(1, repository.active()!!.exercises.sumOf { it.sets.size })
        assertEquals("COMPLETE", repository.active()!!.exercises.single { it.exercise.id == exercise }.exercise.status)
        repository.finish(id)
        val snapshot = repository.finished(id)
        assertTrue(runCatching { repository.correct(corrected, 1, 9, "WORKING") }.isFailure)
        assertTrue(runCatching { save(exercise) }.isFailure)
        assertEquals(snapshot, repository.finished(id))
    }

    @Test fun t06FailedFinalisationRollsBackCursorStatusAndActiveReference() = runBlocking {
        val members = prepare()
        val id = repository.start(members[0])
        repository.warmup(false)
        save(repository.active()!!.exercises.first().exercise.id)
        repository.warmdown(id, true)
        val before = repository.active()
        // Failure at the last write proves earlier session and cursor writes roll back.
        database.openHelper.writableDatabase.execSQL("CREATE TRIGGER b05_finish_reject BEFORE DELETE ON active_workout BEGIN SELECT RAISE(ABORT, 'synthetic failure'); END")
        assertTrue(runCatching { repository.finish(id) }.isFailure)
        assertEquals(before, repository.active())
        assertEquals(members[0], database.programDao().settings()!!.nextProgramRoutineId)
        assertTrue(repository.observeHistory().first().isEmpty())
        database.openHelper.writableDatabase.execSQL("DROP TRIGGER b05_finish_reject")
        repository.finish(id)
        assertNull(repository.active())
        assertEquals(members[1], database.programDao().settings()!!.nextProgramRoutineId)
    }

    @Test fun t06RecordingCorrectionsPreserveIdentityAndRestAndRejectConcurrentOverwrite() = runBlocking {
        repository.start(prepare()[0])
        repository.warmup(false)
        val exercise = repository.active()!!.exercises.first().exercise.id
        val original = save(exercise)
        repository.startRest(exercise)
        val timer = repository.active()!!.session
        val results = coroutineScope {
            listOf(9, 10).map { reps -> async(Dispatchers.Default) { runCatching { repository.correct(original, null, reps, "WORKING") } } }.awaitAll()
        }
        assertEquals(1, results.count { it.isSuccess })
        val corrected = repository.active()!!.exercises.flatMap { it.sets }.single()
        assertEquals(original.id, corrected.id)
        assertEquals(original.commandId, corrected.commandId)
        assertEquals(original.completedAt, corrected.completedAt)
        assertEquals(original.recordingOrder, corrected.recordingOrder)
        assertEquals(timer, repository.active()!!.session)
        assertNull(corrected.loadGrams)
        assertTrue(runCatching { repository.correct(corrected, -1, 8, "WORKING") }.isFailure)
        assertEquals(corrected, repository.active()!!.exercises.flatMap { it.sets }.single())
    }

    @Test fun t06RepeatedConcurrentFinishAdvancesOnceAndManualChoiceWraps() = runBlocking {
        val members = prepare()
        ProgramRepository(database).selectNextRoutine(members[2])
        val id = repository.start(members[2])
        assertEquals(members[2], database.programDao().settings()!!.nextProgramRoutineId)
        assertEquals(id, repository.start(members[0]))
        repository.warmdown(id, true)
        coroutineScope { (1..12).map { async(Dispatchers.Default) { repository.finish(id) } }.awaitAll() }
        assertEquals(members[0], database.programDao().settings()!!.nextProgramRoutineId)
        assertEquals(1, repository.observeHistory().first().size)
        val next = repository.start(members[0])
        repository.finish(id) // A late retry must not finish the newer session or move its cursor.
        assertEquals(next, repository.active()!!.session.id)
        assertEquals(members[0], database.programDao().settings()!!.nextProgramRoutineId)
        repository.abandon(next)
        assertEquals(members[0], database.programDao().settings()!!.nextProgramRoutineId)
        assertEquals("ABANDONED", database.workoutDao().session(next)!!.session.state)
    }

    @Test fun t06SaveRacingWarmdownAndFinishIsIncludedOrExplicitlyRejected() = runBlocking {
        val members = prepare()
        repeat(10) {
            val id = repository.start(members[0])
            repository.warmup(false)
            val exercise = repository.active()!!.exercises.first().exercise.id
            val outcomes = coroutineScope {
                val save = async(Dispatchers.Default) { runCatching { save(exercise) } }
                val finish = async(Dispatchers.Default) {
                    repository.warmdown(id, true)
                    repository.finish(id)
                }
                finish.await()
                save.await()
            }
            val saved = repository.finished(id)!!.exercises.flatMap { it.sets }
            assertEquals(if (outcomes.isSuccess) listOf(outcomes.getOrThrow()) else emptyList(), saved)
            assertNull(repository.active())
        }
    }

    @Test fun t07FinishedHistorySurvivesSourceChangesAndDatabaseReopen() = runBlocking {
        val id = repository.start(prepare()[0])
        repository.warmup(false)
        val exercises = repository.active()!!.exercises.sortedBy { it.exercise.ordinal }
        save(exercises[0].exercise.id, 11)
        repository.complete(id, exercises[0].exercise.id)
        save(exercises[1].exercise.id, 7)
        repository.warmdown(id, true)
        repository.finish(id)
        val before = repository.finished(id)!!
        assertNotNull(before.session.finishedAt)
        assertEquals(listOf("COMPLETE", "PARTIAL", "SKIPPED"), before.exercises.sortedBy { it.exercise.ordinal }.map { it.exercise.status })
        database.openHelper.writableDatabase.execSQL("UPDATE exercises SET name = 'Changed', description = 'Changed'")
        database.openHelper.writableDatabase.execSQL("UPDATE routines SET name = 'Changed'")
        database.openHelper.writableDatabase.execSQL("UPDATE set_prescriptions SET repMax = 99, loadGrams = 999")
        database.close()
        database = GymDatabase.build(context, name)
        assertEquals(before, repository.finished(id))
        assertEquals(before, repository.observeFinished(id).first())
        assertEquals(listOf(before.session), repository.observeHistory().first())
        assertNull(repository.active())
    }

    @Test fun t11V3MigrationPreservesAllRowsAndRestoresPartialState() = runBlocking {
        val id = repository.start(prepare()[0])
        repository.warmup(false)
        save(repository.active()!!.exercises.first().exercise.id)
        val before = repository.active()!!
        val plans = database.programDao().programs()
        val cursor = database.programDao().settings()
        val catalogue = database.exerciseDao().all()
        database.close()
        val oldName = "b05-v3-test.db"
        context.deleteDatabase(oldName)
        migration.createDatabase(oldName, 3).use { old ->
            old.execSQL("ATTACH DATABASE ? AS seeded", arrayOf(context.getDatabasePath(name).path))
            listOf("exercises", "programs", "routines", "program_routines", "routine_exercises", "set_prescriptions", "app_settings").forEach { old.execSQL("INSERT INTO $it SELECT * FROM seeded.$it") }
            val sessionColumns = "id,programId,routineId,programName,routineName,startedAt,offsetSeconds,state,warmupElapsedMs,warmupAnchorMs,restAnchorMs,restDurationMs"
            old.execSQL("INSERT INTO workout_sessions SELECT $sessionColumns FROM seeded.workout_sessions")
            val exerciseColumns = "id,sessionId,sourceOccurrenceId,ordinal,snapshot_id,snapshot_origin,snapshot_catalogueVersion,snapshot_name,snapshot_description,snapshot_muscleGroups,snapshot_equipment,snapshot_movementPatterns,snapshot_exerciseType,snapshot_measurementKind,snapshot_loadConvention,snapshot_archived"
            old.execSQL("INSERT INTO session_exercises SELECT $exerciseColumns FROM seeded.session_exercises")
            listOf("active_workout", "session_prescriptions", "recorded_sets").forEach { old.execSQL("INSERT INTO $it SELECT * FROM seeded.$it") }
            old.execSQL("DETACH DATABASE seeded")
        }
        migration.runMigrationsAndValidate(oldName, 4, true, GymDatabase.MIGRATION_3_4).close()
        val upgraded = GymDatabase.build(context, oldName)
        try {
            val restored = WorkoutRepository(upgraded).active()!!
            assertEquals(before.copy(session = before.session.copy(sourceMembershipId = null)), restored)
            assertEquals(catalogue, upgraded.exerciseDao().all())
            assertEquals(plans, upgraded.programDao().programs())
            assertEquals(cursor, upgraded.programDao().settings())
            upgraded.openHelper.readableDatabase.query("PRAGMA foreign_key_check").use { assertFalse(it.moveToFirst()) }
            upgraded.openHelper.readableDatabase.query("PRAGMA integrity_check").use {
                assertTrue(it.moveToFirst())
                assertEquals("ok", it.getString(0))
            }
            // Existing B-04 sessions can still finalise and advance using their preserved lineage.
            WorkoutRepository(upgraded).warmdown(id, true)
            WorkoutRepository(upgraded).finish(id)
            assertNotEquals(cursor, upgraded.programDao().settings())
        } finally {
            upgraded.close()
            context.deleteDatabase(oldName)
        }
    }
}
