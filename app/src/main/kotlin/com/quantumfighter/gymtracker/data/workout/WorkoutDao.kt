package com.quantumfighter.gymtracker.data.workout

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
internal interface WorkoutDao {
    @Insert suspend fun insertSession(value: WorkoutSessionEntity)

    @Insert suspend fun insertActive(value: ActiveWorkoutEntity)

    @Insert suspend fun insertExercise(value: SessionExerciseEntity)

    @Insert suspend fun insertPrescription(value: SessionPrescriptionEntity)

    @Insert suspend fun insertSet(value: RecordedSetEntity)

    @Update suspend fun updateSession(value: WorkoutSessionEntity)

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE id = (SELECT sessionId FROM active_workout WHERE id = 1)")
    suspend fun active(): WorkoutGraph?

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE id = (SELECT sessionId FROM active_workout WHERE id = 1)")
    fun observeActive(): Flow<WorkoutGraph?>

    @Query("SELECT * FROM recorded_sets WHERE commandId = :id")
    suspend fun command(id: String): RecordedSetEntity?

    @Query("SELECT COALESCE(MAX(recordingOrder), 0) + 1 FROM recorded_sets")
    suspend fun nextRecordingOrder(): Long
}
