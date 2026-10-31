package com.quantumfighter.gymtracker.data.exercise

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/** Internal persistence access; callers validate domain data and seed inside a Room transaction. */
@Dao
internal interface ExerciseDao {
    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun find(id: String): ExerciseEntity?

    @Query("SELECT * FROM exercises ORDER BY name COLLATE NOCASE, id")
    suspend fun all(): List<ExerciseEntity>

    @Query("SELECT * FROM exercises WHERE archived = 0 AND measurementKind = 'REPETITIONS' ORDER BY name COLLATE NOCASE, id")
    suspend fun recordable(): List<ExerciseEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(exercise: ExerciseEntity)
}
