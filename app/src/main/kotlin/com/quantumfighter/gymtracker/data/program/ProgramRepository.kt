package com.quantumfighter.gymtracker.data.program

import androidx.room.withTransaction
import com.quantumfighter.gymtracker.data.exercise.GymDatabase
import com.quantumfighter.gymtracker.domain.PreparedExercise
import com.quantumfighter.gymtracker.domain.PreparedRoutine
import com.quantumfighter.gymtracker.domain.ProgramLibrary
import com.quantumfighter.gymtracker.domain.ProgramPlan
import com.quantumfighter.gymtracker.domain.RoutineExerciseDraft
import com.quantumfighter.gymtracker.domain.SetTarget
import kotlinx.coroutines.flow.combine
import java.util.UUID

/** Owns atomic manual-plan creation and the Home cursor. No session/history writes occur here. */
internal class ProgramRepository(private val database: GymDatabase) {
    private val dao = database.programDao()

    fun observeLibrary() = combine(dao.observePrograms(), dao.observeSettings()) { programs, settings ->
        library(programs, settings)
    }

    suspend fun readLibrary(): ProgramLibrary = database.withTransaction { library(dao.programs(), dao.settings()) }

    suspend fun createProgram(name: String): String {
        require(name.isNotBlank()) { "Program name is required." }
        val id = UUID.randomUUID().toString()
        dao.insertProgram(ProgramEntity(id, name.trim()))
        return id
    }

    /** Validates current catalogue eligibility and writes the entire ordered graph or nothing. */
    suspend fun createRoutine(programId: String, name: String, exercises: List<RoutineExerciseDraft>): String = database.withTransaction {
        require(name.isNotBlank()) { "Routine name is required." }
        require(exercises.isNotEmpty()) { "Add at least one exercise." }
        require(exercises.map { it.id }.distinct().size == exercises.size) { "Exercise occurrences must have distinct IDs." }
        require(dao.program(programId)?.archived == false) { "Program is unavailable." }
        exercises.forEach { occurrence ->
            require(UUID.fromString(occurrence.id).toString() == occurrence.id) { "Invalid occurrence ID." }
            val exercise = requireNotNull(database.exerciseDao().find(occurrence.exerciseId)) { "Exercise is unavailable." }.toExercise()
            require(occurrence.sets.isNotEmpty()) { "Each exercise needs at least one set." }
            occurrence.sets.forEach { it.validateFor(exercise) }
        }
        val routineId = UUID.randomUUID().toString()
        val membershipId = UUID.randomUUID().toString()
        dao.insertRoutine(RoutineEntity(routineId, name.trim()))
        dao.insertMembership(ProgramRoutineEntity(membershipId, programId, routineId, dao.nextOrdinal(programId)))
        exercises.forEachIndexed { ordinal, occurrence ->
            dao.insertOccurrence(RoutineExerciseEntity(occurrence.id, routineId, occurrence.exerciseId, ordinal))
            occurrence.sets.forEachIndexed { setOrdinal, target ->
                dao.insertSet(SetPrescriptionEntity(UUID.randomUUID().toString(), occurrence.id, setOrdinal, target.repMin, target.repMax, target.loadGrams, target.restSeconds))
            }
        }
        if (dao.settings()?.nextProgramRoutineId == null) dao.saveSettings(AppSettingsEntity(nextProgramRoutineId = membershipId))
        membershipId
    }

    /** A stale selection cannot select an archived or unsupported exercise through its routine. */
    suspend fun selectNextRoutine(membershipId: String) = database.withTransaction {
        val routine = library(dao.programs(), dao.settings()).programs.flatMap { it.routines }.find { it.membershipId == membershipId }
        requireNotNull(routine) { "Routine is unavailable." }
        require(routine.exercises.isNotEmpty()) { "Routine is empty." }
        routine.exercises.forEach { occurrence ->
            require(occurrence.sets.isNotEmpty()) { "Exercise has no targets." }
            occurrence.sets.forEach { it.validateFor(occurrence.exercise) }
        }
        dao.saveSettings(AppSettingsEntity(nextProgramRoutineId = membershipId))
    }

    private fun library(programs: List<ProgramGraph>, settings: AppSettingsEntity?): ProgramLibrary = ProgramLibrary(
        programs.map { graph ->
            ProgramPlan(
                graph.program.id,
                graph.program.name,
                graph.memberships.sortedBy { it.membership.ordinal }.map { link ->
                    PreparedRoutine(
                        link.membership.id,
                        link.routine.routine.id,
                        link.routine.routine.name,
                        link.routine.exercises.sortedBy { it.occurrence.ordinal }.map { occurrence ->
                            PreparedExercise(
                                occurrence.occurrence.id,
                                occurrence.exercise.toExercise(),
                                occurrence.sets.sortedBy { it.ordinal }.map {
                                    check(it.setType == "WORKING") { "Unsupported set type." }
                                    SetTarget(it.repMin, it.repMax, it.loadGrams, it.restSeconds)
                                },
                            )
                        },
                    )
                },
            )
        },
        settings?.nextProgramRoutineId,
    )
}
