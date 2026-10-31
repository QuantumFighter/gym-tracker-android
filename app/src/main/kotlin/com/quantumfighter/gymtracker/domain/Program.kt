package com.quantumfighter.gymtracker.domain

import java.math.BigDecimal

/** Repetition-only working-set target. Mass is exact integer grams; null means unknown. */
internal data class SetTarget(
    val repMin: Int,
    val repMax: Int,
    val loadGrams: Long?,
    val restSeconds: Int,
) {
    init {
        require(repMin > 0 && repMax >= repMin) { "Repetitions must be positive and minimum must not exceed maximum." }
        require(loadGrams == null || loadGrams >= 0) { "Starting weight must be nonnegative or blank." }
        require(restSeconds >= 0) { "Rest must be a nonnegative whole number of seconds." }
    }

    fun validateFor(exercise: Exercise) {
        require(exercise.canRecord) { "Exercise is archived or its measurement is unsupported." }
        require(exercise.loadConvention != LoadConvention.BODYWEIGHT || loadGrams == null) {
            "Bodyweight exercises do not take an external load."
        }
    }
}

/** Central exact kg conversion. Blank stays unset; no rounding or floating-point storage. */
internal object LoadInput {
    fun parseKg(text: String): Long? {
        if (text.isBlank()) return null
        require(Regex("[0-9]+(\\.[0-9]{1,3})?").matches(text.trim())) {
            "Weight must be nonnegative kg with at most three decimal places, or blank."
        }
        return try {
            BigDecimal(text.trim()).movePointRight(3).longValueExact()
        } catch (_: ArithmeticException) {
            throw IllegalArgumentException("Weight is too large.")
        }
    }

    fun formatKg(grams: Long): String = BigDecimal.valueOf(grams, 3).stripTrailingZeros().toPlainString()
}

/** A repeated catalogue exercise has its own occurrence ID and ordered set targets. */
internal data class RoutineExerciseDraft(val id: String, val exerciseId: String, val sets: List<SetTarget>)

internal data class PreparedExercise(val id: String, val exercise: Exercise, val sets: List<SetTarget>)

/** Membership identity is distinct from the reusable routine definition identity. */
internal data class PreparedRoutine(
    val membershipId: String,
    val routineId: String,
    val name: String,
    val exercises: List<PreparedExercise>,
)

internal data class ProgramPlan(val id: String, val name: String, val routines: List<PreparedRoutine>)

internal data class ProgramLibrary(val programs: List<ProgramPlan> = emptyList(), val nextMembershipId: String? = null) {
    val nextRoutine: PreparedRoutine?
        get() = programs.flatMap { it.routines }.find { it.membershipId == nextMembershipId }
}
