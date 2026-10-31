package com.quantumfighter.gymtracker.app

import android.app.Application
import com.quantumfighter.gymtracker.data.exercise.ExerciseRepository
import com.quantumfighter.gymtracker.data.exercise.GymDatabase
import com.quantumfighter.gymtracker.data.program.ProgramRepository

/** Owns one lazy database/repository per process; opening and seeding run through suspend APIs. */
class GymTrackerApplication : Application() {
    private val database by lazy { GymDatabase.build(this) }

    /** Manually injected catalogue dependency; uses application context, never an activity. */
    val exercises: ExerciseRepository by lazy { ExerciseRepository(database) }

    /** Plans and catalogue share the same transaction-capable database. */
    internal val programs: ProgramRepository by lazy { ProgramRepository(database) }
}
