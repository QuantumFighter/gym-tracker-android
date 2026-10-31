package com.quantumfighter.gymtracker.data.workout

import android.os.SystemClock
import androidx.room.withTransaction
import com.quantumfighter.gymtracker.data.exercise.GymDatabase
import com.quantumfighter.gymtracker.data.program.AppSettingsEntity
import com.quantumfighter.gymtracker.domain.SaveSetCommand
import com.quantumfighter.gymtracker.domain.SetTarget
import com.quantumfighter.gymtracker.domain.exerciseStatus
import java.time.OffsetDateTime
import java.util.UUID

/** All commands serialize in Room transactions; return values acknowledge committed writes only. */
internal class WorkoutRepository(private val database: GymDatabase, private val monotonic: () -> Long = SystemClock::elapsedRealtime) {
    private val dao = database.workoutDao()
    fun observeActive() = dao.observeActive()
    suspend fun active() = dao.active()
    fun observeHistory() = dao.observeHistory()
    fun observeFinished(id: String) = dao.observeFinished(id)
    suspend fun finished(id: String) = dao.session(id)?.takeIf { it.session.state == "FINISHED" }

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
        val session = WorkoutSessionEntity(uuid(), program.program.id, routine.routine.id, program.program.name, routine.routine.name, now.toInstant().toEpochMilli(), now.offset.totalSeconds, sourceMembershipId = membershipId)
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
        require(active.session.state == "RECORDING") { "Return to recording before saving a new set." }
        val exercise = requireNotNull(active.exercises.find { it.exercise.id == command.exerciseId }) { "Exercise does not belong to this workout." }
        command.validate(exercise.exercise.definition)
        val now = OffsetDateTime.now()
        val row = RecordedSetEntity(uuid(), command.id, command.exerciseId, (exercise.sets.maxOfOrNull { it.sequence } ?: 0) + 1, dao.nextRecordingOrder(), command.loadGrams, command.repetitions, command.setType, command.unit, now.toInstant().toEpochMilli(), now.offset.totalSeconds)
        dao.insertSet(row)
        dao.updateExercise(exercise.exercise.copy(status = "PARTIAL"))
        row
    }

    suspend fun complete(sessionId: String, exerciseId: String) = database.withTransaction {
        val active = requireActive(sessionId)
        require(active.session.state == "RECORDING")
        val exercise = requireNotNull(active.exercises.find { it.exercise.id == exerciseId })
        require(exercise.sets.isNotEmpty()) { "Save a set before completing this exercise." }
        dao.updateExercise(exercise.exercise.copy(status = "COMPLETE"))
    }

    /** Rechecks pending work inside the transaction, even if the confirmation UI was stale. */
    suspend fun warmdown(sessionId: String, confirmed: Boolean) = database.withTransaction {
        val active = requireActive(sessionId)
        if (active.session.state == "WARM_DOWN") return@withTransaction
        require(confirmed || active.exercises.all { it.exercise.status == "COMPLETE" }) { "Confirm unfinished exercises before warm-down." }
        classify(active, true)
        val session = active.session
        dao.updateSession(session.copy(state = "WARM_DOWN", warmupElapsedMs = session.warmupElapsedMs + elapsed(session.warmupAnchorMs), warmupAnchorMs = null, restAnchorMs = null, restDurationMs = 0))
    }

    suspend fun returnToRecording(sessionId: String) = database.withTransaction {
        val active = requireActive(sessionId)
        require(active.session.state == "WARM_DOWN")
        classify(active, false)
        dao.updateSession(active.session.copy(state = "RECORDING", warmdownElapsedMs = active.session.warmdownElapsedMs + elapsed(active.session.warmdownAnchorMs), warmdownAnchorMs = null))
    }

    suspend fun warmdownTimer(sessionId: String, start: Boolean) = database.withTransaction {
        val session = requireActive(sessionId).session
        require(session.state == "WARM_DOWN")
        dao.updateSession(
            if (start) {
                session.copy(warmdownAnchorMs = session.warmdownAnchorMs ?: monotonic())
            } else {
                session.copy(warmdownElapsedMs = session.warmdownElapsedMs + elapsed(session.warmdownAnchorMs), warmdownAnchorMs = null)
            },
        )
    }

    /** Optimistic compare-and-update avoids overwriting a newer correction; no inserts or rest changes. */
    suspend fun correct(expected: RecordedSetEntity, loadGrams: Long?, repetitions: Int, setType: String): RecordedSetEntity = database.withTransaction {
        val active = requireNotNull(dao.active()) { "Completed workouts are read-only." }
        require(active.session.state == "RECORDING" || active.session.state == "WARM_DOWN")
        val exercise = requireNotNull(active.exercises.find { it.exercise.id == expected.exerciseId })
        val current = requireNotNull(exercise.sets.find { it.id == expected.id })
        SaveSetCommand(uuid(), expected.exerciseId, loadGrams, repetitions, setType, expected.unit).validate(exercise.exercise.definition)
        val updated = expected.copy(loadGrams = loadGrams, repetitions = repetitions, setType = setType)
        require(current == expected || current == updated) { "This set changed. Reopen it before correcting." }
        dao.updateSet(updated)
        updated
    }

    /** Session, cursor and active reference commit together. Session identity makes retries safe. */
    suspend fun finish(sessionId: String): String = database.withTransaction {
        val existing = requireNotNull(dao.session(sessionId))
        if (existing.session.state == "FINISHED") return@withTransaction sessionId
        val active = requireActive(sessionId)
        require(active.session.state == "WARM_DOWN") { "Enter warm-down before finishing." }
        classify(active, true)
        val now = OffsetDateTime.now()
        dao.updateSession(active.session.copy(state = "FINISHED", finishedAt = now.toInstant().toEpochMilli(), finishedOffsetSeconds = now.offset.totalSeconds, warmdownElapsedMs = active.session.warmdownElapsedMs + elapsed(active.session.warmdownAnchorMs), warmdownAnchorMs = null))
        val plans = database.programDao()
        val selectedMembership = plans.settings()?.nextProgramRoutineId
        val members = plans.programs().find { it.program.id == active.session.programId }?.memberships?.sortedBy { it.membership.ordinal }.orEmpty()
        // v3 sessions predate membership snapshots. Prefer their selected matching membership.
        val index = members.indexOfFirst { it.membership.id == active.session.sourceMembershipId }.takeIf { it >= 0 }
            ?: members.indexOfFirst { it.membership.id == selectedMembership && it.membership.routineId == active.session.routineId }.takeIf { it >= 0 }
            ?: members.indexOfFirst { it.membership.routineId == active.session.routineId }.takeIf { it >= 0 }
        if (index != null) plans.saveSettings(AppSettingsEntity(nextProgramRoutineId = members[(index + 1) % members.size].membership.id))
        dao.clearActive(sessionId)
        sessionId
    }

    suspend fun abandon(sessionId: String) = database.withTransaction {
        val active = requireActive(sessionId)
        classify(active, true)
        val now = OffsetDateTime.now()
        dao.updateSession(active.session.copy(state = "ABANDONED", finishedAt = now.toInstant().toEpochMilli(), finishedOffsetSeconds = now.offset.totalSeconds, warmupElapsedMs = active.session.warmupElapsedMs + elapsed(active.session.warmupAnchorMs), warmupAnchorMs = null, warmdownElapsedMs = active.session.warmdownElapsedMs + elapsed(active.session.warmdownAnchorMs), warmdownAnchorMs = null, restAnchorMs = null, restDurationMs = 0))
        dao.clearActive(sessionId)
    }

    private suspend fun requireActive(id: String): WorkoutGraph {
        val active = requireNotNull(dao.active()) { "No active workout. Completed workouts are read-only." }
        require(active.session.id == id) { "This workout is no longer active." }
        require(active.session.state in listOf("WARM_UP", "RECORDING", "WARM_DOWN"))
        return active
    }

    private suspend fun classify(graph: WorkoutGraph, ending: Boolean) {
        graph.exercises.forEach {
            dao.updateExercise(it.exercise.copy(status = exerciseStatus(it.exercise.status == "COMPLETE", it.sets.isNotEmpty(), ending)))
        }
    }

    private fun elapsed(anchor: Long?) = anchor?.let { (monotonic() - it).coerceAtLeast(0) } ?: 0L

    private fun uuid() = UUID.randomUUID().toString()
}
