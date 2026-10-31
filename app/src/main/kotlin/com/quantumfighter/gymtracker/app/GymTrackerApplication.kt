package com.quantumfighter.gymtracker.app

import android.app.Application
import com.quantumfighter.gymtracker.data.exercise.ExerciseRepository
import com.quantumfighter.gymtracker.data.exercise.GymDatabase

/** Owns one lazy database/repository per process; opening and seeding run through suspend APIs. */
class GymTrackerApplication : Application() {
    /** Manually injected catalogue dependency; uses application context, never an activity. */
    val exercises: ExerciseRepository by lazy { ExerciseRepository(GymDatabase.build(this)) }
}
