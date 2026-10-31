package com.quantumfighter.gymtracker.data.program

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
internal interface ProgramDao {
    @Insert suspend fun insertProgram(program: ProgramEntity)

    @Insert suspend fun insertRoutine(routine: RoutineEntity)

    @Insert suspend fun insertMembership(membership: ProgramRoutineEntity)

    @Insert suspend fun insertOccurrence(occurrence: RoutineExerciseEntity)

    @Insert suspend fun insertSet(set: SetPrescriptionEntity)

    @Upsert suspend fun saveSettings(settings: AppSettingsEntity)

    @Query("SELECT * FROM programs WHERE id = :id")
    suspend fun program(id: String): ProgramEntity?

    @Transaction
    @Query("SELECT * FROM programs WHERE archived = 0 ORDER BY rowid")
    fun observePrograms(): Flow<List<ProgramGraph>>

    @Transaction
    @Query("SELECT * FROM programs WHERE archived = 0 ORDER BY rowid")
    suspend fun programs(): List<ProgramGraph>

    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun observeSettings(): Flow<AppSettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = 1")
    suspend fun settings(): AppSettingsEntity?

    @Query("SELECT COALESCE(MAX(ordinal) + 1, 0) FROM program_routines WHERE programId = :programId")
    suspend fun nextOrdinal(programId: String): Int
}
