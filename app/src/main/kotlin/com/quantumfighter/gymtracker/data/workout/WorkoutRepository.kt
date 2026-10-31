package com.quantumfighter.gymtracker.data.workout

import android.os.SystemClock
import androidx.room.withTransaction
import com.quantumfighter.gymtracker.data.exercise.GymDatabase
import com.quantumfighter.gymtracker.domain.SaveSetCommand
import com.quantumfighter.gymtracker.domain.SetTarget
import java.time.OffsetDateTime
import java.util.UUID

/** All commands serialize in Room transactions; return values acknowledge committed writes only. */
internal class WorkoutRepository(private val database: GymDatabase, private val monotonic: () -> Long = SystemClock::elapsedRealtime) {
    private val dao = database.workoutDao()
    fun observeActive() = dao.observeActive()
    suspend fun active() = dao.active()

    suspend fun startRest(exerciseId: String) = database.withTransaction {
        val active = requireNotNull(dao.active())
        require(active.session.state == "RECORDING")
        val exercise = requireNotNull(active.exercises.find { it.exercise.id == exerciseId })
        val seconds = exercise.prescriptions.minByOrNull { it.ordinal }?.restSeconds ?: 90
        dao.updateSession(active.session.copy(restAnchorMs = monotonic(), restDurationMs = seconds * 1000L))
    }

    suspend fun start(membershipId: String): String = database.withTransaction {
        dao.active()?.let { return@withTransaction it.session.id }
        val program = database.programDao().programs().firstOrNull { p -> p.memberships.any { it.membership.id == membershipId } }
        requireNotNull(program) { "Saved routine is unavailable." }
        val routine = program.memberships.single { it.membership.id == membershipId }.routine
        require(routine.exercises.isNotEmpty()) { "Routine is empty." }
        val now = OffsetDateTime.now()
        val session = WorkoutSessionEntity(uuid(), program.program.id, routine.routine.id, program.program.name, routine.routine.name, now.toInstant().toEpochMilli(), now.offset.totalSeconds)
        dao.insertSession(session)
        routine.exercises.sortedBy { it.occurrence.ordinal }.forEach { occurrence ->
            require(occurrence.sets.isNotEmpty()) { "Exercise has no prescriptions." }
            val exercise = SessionExerciseEntity(uuid(), session.id, occurrence.occurrence.id, occurrence.occurrence.ordinal, occurrence.exercise.toExercise())
            dao.insertExercise(exercise)
            occurrence.sets.forEach { target ->
                SetTarget(target.repMin, target.repMax, target.loadGrams, target.restSeconds).validateFor(occurrence.exercise.toExercise())
                require(target.setType == "WORKING" || target.setType == "WARM_UP")
                dao.insertPrescription(SessionPrescriptionEntity(uuid(), exercise.id, target.id, target.ordinal, target.repMin, target.repMax, target.loadGrams, target.restSeconds, target.setType))
            }
        }
        dao.insertActive(ActiveWorkoutEntity(sessionId = session.id))
        session.id
    }

    suspend fun warmup(start: Boolean) = database.withTransaction {
        val session = requireNotNull(dao.active()).session
        require(session.state == "WARM_UP")
        if (start) {
            if (session.warmupAnchorMs == null) dao.updateSession(session.copy(warmupAnchorMs = monotonic()))
        } else {
            val elapsed = session.warmupAnchorMs?.let { (monotonic() - it).coerceAtLeast(0) } ?: 0
            dao.updateSession(session.copy(state = "RECORDING", warmupElapsedMs = session.warmupElapsedMs + elapsed, warmupAnchorMs = null))
        }
    }

    /** Identical retries return the original row; reusing a UUID with different content is rejected. */
    suspend fun save(command: SaveSetCommand): RecordedSetEntity = database.withTransaction {
        dao.command(command.id)?.let { old ->
            require(old.exerciseId == command.exerciseId && old.loadGrams == command.loadGrams && old.repetitions == command.repetitions && old.setType == command.setType && old.unit == command.unit) { "Save command already belongs to different values." }
            return@withTransaction old
        }
        val active = requireNotNull(dao.active()) { "No active workout." }
        require(active.session.state == "RECORDING") { "Stop warm-up before recording." }
        val exercise = requireNotNull(active.exercises.find { it.exercise.id == command.exerciseId }) { "Exercise does not belong to this workout." }
        command.validate(exercise.exercise.definition)
        val now = OffsetDateTime.now()
        val row = RecordedSetEntity(uuid(), command.id, command.exerciseId, (exercise.sets.maxOfOrNull { it.sequence } ?: 0) + 1, dao.nextRecordingOrder(), command.loadGrams, command.repetitions, command.setType, command.unit, now.toInstant().toEpochMilli(), now.offset.totalSeconds)
        dao.insertSet(row)
        row
    }

    private fun uuid() = UUID.randomUUID().toString()
}
