package com.quantumfighter.gymtracker.data.exercise

import androidx.room.withTransaction
import com.quantumfighter.gymtracker.domain.Exercise
import com.quantumfighter.gymtracker.domain.ExerciseOrigin

/**
 * Offline catalogue access for ViewModels and subsequent manual-routine selection.
 * Every read waits for atomic seeding; failures propagate without deleting or resetting storage.
 *
 * @param database Application-owned Room instance, supplied by the manual composition root.
 */
class ExerciseRepository internal constructor(private val database: GymDatabase) {
    /**
     * Inserts missing built-ins atomically. Existing metadata (including archival) is preserved.
     * A conflicting custom identity fails the whole transaction instead of replacing user data.
     */
    suspend fun seedCatalogue() {
        database.withTransaction {
            val dao = database.exerciseDao()
            BuiltInCatalogue.exercises.forEach { exercise ->
                val existing = dao.find(exercise.id)
                if (existing == null) {
                    dao.insert(exercise.toEntity())
                } else {
                    check(existing.toExercise().origin == ExerciseOrigin.BUILT_IN) { "Built-in identity conflict" }
                }
            }
        }
    }

    /** All supported, unarchived selections; no duration/distance options are exposed before B-19. */
    suspend fun recordableExercises(): List<Exercise> {
        seedCatalogue()
        return database.exerciseDao().recordable().map(ExerciseEntity::toExercise)
    }

    /**
     * Revalidates an ID at selection time, including stale/deep-linked selections.
     * @throws IllegalArgumentException if absent, archived or unsupported.
     */
    suspend fun exerciseForRecording(id: String): Exercise {
        seedCatalogue()
        val exercise = requireNotNull(database.exerciseDao().find(id)) { "Exercise is unavailable" }.toExercise()
        require(exercise.canRecord) { "Exercise cannot be recorded yet" }
        return exercise
    }
}
