package com.quantumfighter.gymtracker.data.exercise

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.quantumfighter.gymtracker.data.program.AppSettingsEntity
import com.quantumfighter.gymtracker.data.program.ProgramDao
import com.quantumfighter.gymtracker.data.program.ProgramEntity
import com.quantumfighter.gymtracker.data.program.ProgramRoutineEntity
import com.quantumfighter.gymtracker.data.program.RoutineEntity
import com.quantumfighter.gymtracker.data.program.RoutineExerciseEntity
import com.quantumfighter.gymtracker.data.program.SetPrescriptionEntity
import com.quantumfighter.gymtracker.data.workout.ActiveWorkoutEntity
import com.quantumfighter.gymtracker.data.workout.RecordedSetEntity
import com.quantumfighter.gymtracker.data.workout.SessionExerciseEntity
import com.quantumfighter.gymtracker.data.workout.SessionPrescriptionEntity
import com.quantumfighter.gymtracker.data.workout.WorkoutDao
import com.quantumfighter.gymtracker.data.workout.WorkoutSessionEntity

/** Explicit additive migrations preserve catalogue rows; unsupported versions never reset storage. */
@Database(
    entities = [ExerciseEntity::class, ProgramEntity::class, RoutineEntity::class, ProgramRoutineEntity::class, RoutineExerciseEntity::class, SetPrescriptionEntity::class, AppSettingsEntity::class, WorkoutSessionEntity::class, ActiveWorkoutEntity::class, SessionExerciseEntity::class, SessionPrescriptionEntity::class, RecordedSetEntity::class],
    version = 4,
    exportSchema = true,
)
@TypeConverters(ExerciseConverters::class)
internal abstract class GymDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun programDao(): ProgramDao
    abstract fun workoutDao(): WorkoutDao

    companion object {
        const val NAME = "gym-tracker.db"

        /** Adds lifecycle fields without rebuilding or deleting session-owned records. */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE workout_sessions ADD COLUMN sourceMembershipId TEXT")
                db.execSQL("ALTER TABLE workout_sessions ADD COLUMN finishedAt INTEGER")
                db.execSQL("ALTER TABLE workout_sessions ADD COLUMN finishedOffsetSeconds INTEGER")
                db.execSQL("ALTER TABLE workout_sessions ADD COLUMN warmdownElapsedMs INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE workout_sessions ADD COLUMN warmdownAnchorMs INTEGER")
                db.execSQL("ALTER TABLE session_exercises ADD COLUMN status TEXT NOT NULL DEFAULT 'PENDING'")
                db.execSQL("UPDATE session_exercises SET status = 'PARTIAL' WHERE EXISTS (SELECT 1 FROM recorded_sets WHERE exerciseId = session_exercises.id)")
            }
        }

        /** Additive session-owned tables; existing catalogue and plan rows are never rewritten. */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS workout_sessions (id TEXT NOT NULL PRIMARY KEY, programId TEXT, routineId TEXT, programName TEXT NOT NULL, routineName TEXT NOT NULL, startedAt INTEGER NOT NULL, offsetSeconds INTEGER NOT NULL, state TEXT NOT NULL, warmupElapsedMs INTEGER NOT NULL, warmupAnchorMs INTEGER, restAnchorMs INTEGER, restDurationMs INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS active_workout (id INTEGER NOT NULL PRIMARY KEY, sessionId TEXT NOT NULL, FOREIGN KEY(sessionId) REFERENCES workout_sessions(id) ON UPDATE NO ACTION ON DELETE RESTRICT)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_active_workout_sessionId ON active_workout(sessionId)")
                db.execSQL("CREATE TABLE IF NOT EXISTS session_exercises (id TEXT NOT NULL PRIMARY KEY, sessionId TEXT NOT NULL, sourceOccurrenceId TEXT, ordinal INTEGER NOT NULL, snapshot_id TEXT NOT NULL, snapshot_origin TEXT NOT NULL, snapshot_catalogueVersion INTEGER, snapshot_name TEXT NOT NULL, snapshot_description TEXT NOT NULL, snapshot_muscleGroups TEXT NOT NULL, snapshot_equipment TEXT NOT NULL, snapshot_movementPatterns TEXT NOT NULL, snapshot_exerciseType TEXT NOT NULL, snapshot_measurementKind TEXT NOT NULL, snapshot_loadConvention TEXT NOT NULL, snapshot_archived INTEGER NOT NULL, FOREIGN KEY(sessionId) REFERENCES workout_sessions(id) ON UPDATE NO ACTION ON DELETE RESTRICT)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_session_exercises_sessionId_ordinal ON session_exercises(sessionId, ordinal)")
                db.execSQL("CREATE TABLE IF NOT EXISTS session_prescriptions (id TEXT NOT NULL PRIMARY KEY, exerciseId TEXT NOT NULL, sourcePrescriptionId TEXT, ordinal INTEGER NOT NULL, repMin INTEGER NOT NULL, repMax INTEGER NOT NULL, loadGrams INTEGER, restSeconds INTEGER NOT NULL, setType TEXT NOT NULL, unit TEXT NOT NULL, FOREIGN KEY(exerciseId) REFERENCES session_exercises(id) ON UPDATE NO ACTION ON DELETE RESTRICT)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_session_prescriptions_exerciseId_ordinal ON session_prescriptions(exerciseId, ordinal)")
                db.execSQL("CREATE TABLE IF NOT EXISTS recorded_sets (id TEXT NOT NULL PRIMARY KEY, commandId TEXT NOT NULL, exerciseId TEXT NOT NULL, sequence INTEGER NOT NULL, recordingOrder INTEGER NOT NULL, loadGrams INTEGER, repetitions INTEGER NOT NULL, setType TEXT NOT NULL, unit TEXT NOT NULL, completedAt INTEGER NOT NULL, offsetSeconds INTEGER NOT NULL, FOREIGN KEY(exerciseId) REFERENCES session_exercises(id) ON UPDATE NO ACTION ON DELETE RESTRICT)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_recorded_sets_exerciseId_sequence ON recorded_sets(exerciseId, sequence)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_recorded_sets_commandId ON recorded_sets(commandId)")
            }
        }

        /** B-03 only adds plan/cursor tables. The v1 exercises table and its contents are untouched. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS programs (id TEXT NOT NULL PRIMARY KEY, name TEXT NOT NULL, notes TEXT NOT NULL, archived INTEGER NOT NULL, origin TEXT NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS routines (id TEXT NOT NULL PRIMARY KEY, name TEXT NOT NULL, notes TEXT NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS program_routines (id TEXT NOT NULL PRIMARY KEY, programId TEXT NOT NULL, routineId TEXT NOT NULL, ordinal INTEGER NOT NULL, FOREIGN KEY(programId) REFERENCES programs(id) ON UPDATE NO ACTION ON DELETE RESTRICT, FOREIGN KEY(routineId) REFERENCES routines(id) ON UPDATE NO ACTION ON DELETE RESTRICT)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_program_routines_programId_ordinal ON program_routines(programId, ordinal)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_program_routines_routineId ON program_routines(routineId)")
                db.execSQL("CREATE TABLE IF NOT EXISTS routine_exercises (id TEXT NOT NULL PRIMARY KEY, routineId TEXT NOT NULL, exerciseId TEXT NOT NULL, ordinal INTEGER NOT NULL, FOREIGN KEY(routineId) REFERENCES routines(id) ON UPDATE NO ACTION ON DELETE RESTRICT, FOREIGN KEY(exerciseId) REFERENCES exercises(id) ON UPDATE NO ACTION ON DELETE RESTRICT)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_routine_exercises_routineId_ordinal ON routine_exercises(routineId, ordinal)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_routine_exercises_exerciseId ON routine_exercises(exerciseId)")
                db.execSQL("CREATE TABLE IF NOT EXISTS set_prescriptions (id TEXT NOT NULL PRIMARY KEY, routineExerciseId TEXT NOT NULL, ordinal INTEGER NOT NULL, repMin INTEGER NOT NULL, repMax INTEGER NOT NULL, loadGrams INTEGER, restSeconds INTEGER NOT NULL, setType TEXT NOT NULL, FOREIGN KEY(routineExerciseId) REFERENCES routine_exercises(id) ON UPDATE NO ACTION ON DELETE RESTRICT)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_set_prescriptions_routineExerciseId_ordinal ON set_prescriptions(routineExerciseId, ordinal)")
                db.execSQL("CREATE TABLE IF NOT EXISTS app_settings (id INTEGER NOT NULL PRIMARY KEY, nextProgramRoutineId TEXT, FOREIGN KEY(nextProgramRoutineId) REFERENCES program_routines(id) ON UPDATE NO ACTION ON DELETE RESTRICT)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_app_settings_nextProgramRoutineId ON app_settings(nextProgramRoutineId)")
            }
        }

        fun build(context: Context, name: String = NAME): GymDatabase = Room.databaseBuilder(context.applicationContext, GymDatabase::class.java, name)
            .openHelperFactory(PreservingOpenHelperFactory())
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            .build()
    }
}
