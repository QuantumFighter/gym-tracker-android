package com.quantumfighter.gymtracker

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.quantumfighter.gymtracker.app.GymTrackerApplication
import com.quantumfighter.gymtracker.data.exercise.BuiltInCatalogue
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** T-01: can be run in seed/verify phases around an external force-stop and launcher restart. */
@RunWith(AndroidJUnit4::class)
class CatalogueRelaunchTest {
    @Test
    fun t01ApplicationCatalogueSurvivesProcessRelaunchOffline() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val application = context.applicationContext as GymTrackerApplication
        val exercises = application.exercises.recordableExercises()
        assertEquals(BuiltInCatalogue.exercises.sortedBy { it.name.lowercase() }, exercises)
        val preferences = context.getSharedPreferences("b02-relaunch-test", 0)
        when (InstrumentationRegistry.getArguments().getString("cataloguePhase")) {
            "seed" -> assertTrue(preferences.edit().putString("snapshot", exercises.toString()).commit())
            "verify" -> {
                val snapshot = preferences.getString("snapshot", null)
                assertNotNull("Run seed phase before force-stop and relaunch", snapshot)
                assertEquals(snapshot, exercises.toString())
                assertTrue(preferences.edit().clear().commit())
            }
        }
    }
}
