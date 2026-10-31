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

    @Update suspend fun updateExercise(value: SessionExerciseEntity)

    @Update suspend fun updateSet(value: RecordedSetEntity)

    @Query("DELETE FROM active_workout WHERE sessionId = :sessionId")
    suspend fun clearActive(sessionId: String)

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    suspend fun session(id: String): WorkoutGraph?

    @Query("SELECT * FROM workout_sessions WHERE state = 'FINISHED' ORDER BY finishedAt DESC, id")
    fun observeHistory(): Flow<List<WorkoutSessionEntity>>

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE id = :id AND state = 'FINISHED'")
    fun observeFinished(id: String): Flow<WorkoutGraph?>

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
