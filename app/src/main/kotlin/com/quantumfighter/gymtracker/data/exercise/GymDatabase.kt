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

/** Explicit additive migrations preserve catalogue rows; unsupported versions never reset storage. */
@Database(
    entities = [ExerciseEntity::class, ProgramEntity::class, RoutineEntity::class, ProgramRoutineEntity::class, RoutineExerciseEntity::class, SetPrescriptionEntity::class, AppSettingsEntity::class],
    version = 2,
    exportSchema = true,
)
@TypeConverters(ExerciseConverters::class)
internal abstract class GymDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun programDao(): ProgramDao

    companion object {
        const val NAME = "gym-tracker.db"

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
            .addMigrations(MIGRATION_1_2)
            .build()
    }
}
