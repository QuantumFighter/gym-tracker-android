package com.quantumfighter.gymtracker.domain

import java.util.UUID

/** Stable persisted tokens; duration and distance remain unavailable until B-19. */
enum class MeasurementKind {
    /** Counted repetitions; the only currently recordable format. */
    REPETITIONS,

    /** Reserved token; recording is deferred to B-19. */
    DURATION,

    /** Reserved token; recording is deferred to B-19. */
    DISTANCE,
}

/** Meaning of an entered nonnegative load; future recorded mass uses integer grams. */
enum class LoadConvention {
    /** Total external mass, including the bar where applicable. */
    TOTAL,

    /** Mass of one dumbbell, never the sum of both hands. */
    PER_HAND,

    /** Unloaded bodyweight movement; no profile body mass is collected. */
    BODYWEIGHT,

    /** Positive indicated assistance; increasing this value makes the exercise easier. */
    ASSISTANCE,
}

/** Distinguishes bundled definitions from later user-owned exercises. */
enum class ExerciseOrigin {
    /** Project-owned, versioned catalogue definition with a fixed UUID. */
    BUILT_IN,

    /** User-owned definition; creation is deferred to B-12. */
    CUSTOM,
}

/**
 * Durable library definition. Names are labels, never identifiers or history keys.
 *
 * @property id Canonical UUID retained across catalogue releases and database reopen.
 * @property origin Ownership; seeding must never replace custom definitions.
 * @property catalogueVersion Positive bundle version for built-ins, null for custom definitions.
 * @property name Nonblank display name; duplicate names are allowed.
 * @property description Original descriptive metadata, not training advice.
 * @property muscleGroups Nonempty unique lowercase underscore tags.
 * @property equipment Nonempty unique lowercase underscore tags, including bodyweight equipment.
 * @property movementPatterns Nonempty unique lowercase underscore tags.
 * @property exerciseType Lowercase underscore classification tag.
 * @property measurementKind Measurement format, independent of load convention.
 * @property loadConvention Meaning of load; no negative-load encoding for assistance.
 * @property archived Excludes this definition from new recording selection without deleting identity.
 */
data class Exercise(
    val id: String,
    val origin: ExerciseOrigin,
    val catalogueVersion: Int?,
    val name: String,
    val description: String,
    val muscleGroups: List<String>,
    val equipment: List<String>,
    val movementPatterns: List<String>,
    val exerciseType: String,
    val measurementKind: MeasurementKind,
    val loadConvention: LoadConvention,
    val archived: Boolean = false,
) {
    init {
        require(UUID.fromString(id).toString() == id) { "Exercise ID must be a canonical UUID" }
        require(name.isNotBlank() && description.isNotBlank()) { "Exercise text is required" }
        require(
            when (origin) {
                ExerciseOrigin.BUILT_IN -> catalogueVersion != null && catalogueVersion > 0
                ExerciseOrigin.CUSTOM -> catalogueVersion == null
            },
        ) { "Catalogue version must match exercise origin" }
        val tag = Regex("[a-z][a-z_]*")
        require(tag.matches(exerciseType)) { "Exercise type must be a tag" }
        listOf(muscleGroups, equipment, movementPatterns).forEach { tags ->
            require(tags.isNotEmpty() && tags.distinct().size == tags.size && tags.all(tag::matches)) {
                "Metadata must contain unique nonempty tags"
            }
        }
    }

    /** Fail closed for archived or unsupported measurement definitions at the selection boundary. */
    val canRecord: Boolean
        get() = !archived && measurementKind == MeasurementKind.REPETITIONS
}
