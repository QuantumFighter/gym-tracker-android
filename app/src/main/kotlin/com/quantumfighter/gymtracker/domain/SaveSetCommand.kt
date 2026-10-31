package com.quantumfighter.gymtracker.domain

import java.util.UUID

/** Immutable retry payload. A changed input is a new command, never a reuse of an old UUID. */
internal data class SaveSetCommand(
    val id: String,
    val exerciseId: String,
    val loadGrams: Long?,
    val repetitions: Int,
    val setType: String,
    val unit: String = "kg",
) {
    fun validate(exercise: Exercise) {
        require(UUID.fromString(id).toString() == id) { "Invalid save command." }
        require(UUID.fromString(exerciseId).toString() == exerciseId) { "Invalid session exercise." }
        require(exercise.measurementKind == MeasurementKind.REPETITIONS) { "Unsupported measurement." }
        require(unit == "kg") { "Unsupported weight unit." }
        require(repetitions > 0) { "Repetitions must be positive." }
        require(loadGrams == null || loadGrams >= 0) { "Weight must be nonnegative or unset." }
        require(exercise.loadConvention != LoadConvention.BODYWEIGHT || loadGrams == null) { "Bodyweight takes no external load." }
        require(setType == "WORKING" || setType == "WARM_UP") { "Invalid set type." }
    }
}
