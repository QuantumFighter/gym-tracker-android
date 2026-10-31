package com.quantumfighter.gymtracker

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.quantumfighter.gymtracker.app.GymTrackerApplication
import com.quantumfighter.gymtracker.data.exercise.BuiltInCatalogue
import com.quantumfighter.gymtracker.data.exercise.ExerciseRepository
import com.quantumfighter.gymtracker.data.exercise.GymDatabase
import com.quantumfighter.gymtracker.data.exercise.toEntity
import com.quantumfighter.gymtracker.domain.ExerciseOrigin
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
        val phase = InstrumentationRegistry.getArguments().getString("cataloguePhase")
        if (phase == null) {
            val application = context.applicationContext as GymTrackerApplication
            assertEquals(BuiltInCatalogue.exercises.sortedBy { it.name.lowercase() }, application.exercises.recordableExercises())
            return@runBlocking
        }
        val name = "b02-relaunch-test.db"
        val preferences = context.getSharedPreferences("b02-relaunch-test", 0)
        if (phase == "seed") context.deleteDatabase(name)
        val database = GymDatabase.build(context, name)
        try {
            when (phase) {
                "seed" -> assertTrue(preferences.edit().putString("snapshot", seedSentinelFixture(database)).commit())
                "verify" -> {
                    verifyBeforeSeeding(database, preferences.getString("snapshot", null))
                    assertTrue(preferences.edit().clear().commit())
                }
                else -> error("Unknown relaunch test phase")
            }
        } finally {
            database.close()
        }
        if (phase == "verify") context.deleteDatabase(name)
    }

    @Test
    fun t11RelaunchVerificationFailsIfOnlyDatabaseIsLost() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "b02-relaunch-loss-test.db"
        val preferences = context.getSharedPreferences("b02-relaunch-loss-test", 0)
        context.deleteDatabase(name)
        var database = GymDatabase.build(context, name)
        try {
            val snapshot = seedSentinelFixture(database)
            assertTrue(preferences.edit().putString("snapshot", snapshot).commit())
            database.close()
            assertTrue(context.deleteDatabase(name))
            assertEquals(snapshot, preferences.getString("snapshot", null))
            database = GymDatabase.build(context, name)
            val failure = runCatching { verifyBeforeSeeding(database, preferences.getString("snapshot", null)) }.exceptionOrNull()
            assertTrue("Database loss must fail verification", failure is AssertionError)
            assertTrue("Verification must not reseed", database.exerciseDao().all().isEmpty())
        } finally {
            database.close()
            context.deleteDatabase(name)
            assertTrue(preferences.edit().clear().commit())
        }
    }

    private suspend fun seedSentinelFixture(database: GymDatabase): String {
        ExerciseRepository(database).seedCatalogue()
        val first = BuiltInCatalogue.exercises.first()
        database.openHelper.writableDatabase.execSQL(
            "UPDATE exercises SET description = ?, archived = 1 WHERE id = ?",
            arrayOf("Metadata retained across process death", first.id),
        )
        database.exerciseDao().insert(
            first.copy(
                id = "82ea033e-6fe9-45d5-a7ca-596f958da193",
                origin = ExerciseOrigin.CUSTOM,
                catalogueVersion = null,
                name = "Relaunch sentinel",
                description = "Not reconstructible by built-in seeding",
            ).toEntity(),
        )
        return database.exerciseDao().all().toString()
    }

    private suspend fun verifyBeforeSeeding(database: GymDatabase, snapshot: String?) {
        assertNotNull("Run seed phase before force-stop and relaunch", snapshot)
        assertEquals("Persisted rows must survive before any seeding", snapshot, database.exerciseDao().all().toString())
        ExerciseRepository(database).seedCatalogue()
        assertEquals("Reseeding must preserve sentinel and modified metadata", snapshot, database.exerciseDao().all().toString())
    }
}
