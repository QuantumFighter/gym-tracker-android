package com.quantumfighter.gymtracker.data.exercise

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/** First real schema. No destructive fallback: unsupported versions preserve the original database. */
@Database(entities = [ExerciseEntity::class], version = 1, exportSchema = true)
@TypeConverters(ExerciseConverters::class)
internal abstract class GymDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao

    companion object {
        const val NAME = "gym-tracker.db"

        fun build(context: Context, name: String = NAME): GymDatabase = Room.databaseBuilder(context.applicationContext, GymDatabase::class.java, name).build()
    }
}
