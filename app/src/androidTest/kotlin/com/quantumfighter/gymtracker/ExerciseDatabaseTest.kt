package com.quantumfighter.gymtracker

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.quantumfighter.gymtracker.data.exercise.BuiltInCatalogue
import com.quantumfighter.gymtracker.data.exercise.ExerciseRepository
import com.quantumfighter.gymtracker.data.exercise.GymDatabase
import com.quantumfighter.gymtracker.data.exercise.toEntity
import com.quantumfighter.gymtracker.domain.ExerciseOrigin
import com.quantumfighter.gymtracker.domain.MeasurementKind
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** B-02 / T-01, T-11: actual Android SQLite, using only isolated synthetic test databases. */
@RunWith(AndroidJUnit4::class)
class ExerciseDatabaseTest {
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val name = "b02-catalogue-test.db"
    private lateinit var database: GymDatabase
    private val expected = BuiltInCatalogue.exercises.sortedBy { it.name.lowercase() }

    @get:Rule
    val migration = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        GymDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory(),
    )

    @Before
    fun openIsolatedDatabase() {
        context.deleteDatabase(name)
        database = GymDatabase.build(context, name)
    }

    @After
    fun closeAndDeleteOnlyTestDatabase() {
        database.close()
        context.deleteDatabase(name)
    }

    @Test
    fun t01InitialSeedPersistsCompleteMetadata() = runBlocking {
        assertTrue(database.exerciseDao().all().isEmpty())
        assertEquals(expected, ExerciseRepository(database).recordableExercises())
        assertEquals(expected, database.exerciseDao().all().map { it.toExercise() })
    }

    @Test
    fun t01RepeatedAndConcurrentSeedingDoesNotDuplicateOrReplaceRows() = runBlocking {
        val repository = ExerciseRepository(database)
        repeat(3) { repository.seedCatalogue() }
        coroutineScope {
            val first = async { ExerciseRepository(database).seedCatalogue() }
            val second = async { ExerciseRepository(database).seedCatalogue() }
            first.await()
            second.await()
        }
        assertEquals(expected, database.exerciseDao().all().map { it.toExercise() })
    }

    @Test
    fun t11ReopeningAFilePreservesEveryIdentityAndMetadataField() = runBlocking {
        ExerciseRepository(database).seedCatalogue()
        val before = database.exerciseDao().all()
        database.close()
        database = GymDatabase.build(context, name)
        ExerciseRepository(database).seedCatalogue()
        assertEquals(before, database.exerciseDao().all())
    }

    @Test
    fun t01ReseedingPreservesExistingMetadataArchivalAndCustomRows() = runBlocking {
        val repository = ExerciseRepository(database)
        repository.seedCatalogue()
        val first = expected.first()
        database.openHelper.writableDatabase.execSQL(
            "UPDATE exercises SET name = ?, archived = 1 WHERE id = ?",
            arrayOf("Retained label", first.id),
        )
        val custom = first.copy(
            id = "18c91d44-379c-42b3-8ae7-152ccdf88001",
            origin = ExerciseOrigin.CUSTOM,
            catalogueVersion = null,
        )
        database.exerciseDao().insert(custom.toEntity())
        val before = database.exerciseDao().all()
        repository.seedCatalogue()
        assertEquals(before, database.exerciseDao().all())
        assertFalse(repository.recordableExercises().any { it.id == first.id })
        assertEquals(custom, repository.exerciseForRecording(custom.id))
    }

    @Test
    fun t01RecordingBoundaryRejectsMissingArchivedAndUnsupportedMeasurements() = runBlocking {
        val repository = ExerciseRepository(database)
        repository.seedCatalogue()
        val first = expected.first()
        expectFailure<IllegalArgumentException> { repository.exerciseForRecording("missing") }
        MeasurementKind.entries.filter { it != MeasurementKind.REPETITIONS }.forEachIndexed { index, kind ->
            val unsupported = first.copy(
                id = "18c91d44-379c-42b3-8ae7-152ccdf8800${index + 2}",
                origin = ExerciseOrigin.CUSTOM,
                catalogueVersion = null,
                measurementKind = kind,
            )
            database.exerciseDao().insert(unsupported.toEntity())
            assertFalse(repository.recordableExercises().any { it.id == unsupported.id })
            expectFailure<IllegalArgumentException> { repository.exerciseForRecording(unsupported.id) }
        }
        database.openHelper.writableDatabase.execSQL("UPDATE exercises SET archived = 1 WHERE id = ?", arrayOf(first.id))
        expectFailure<IllegalArgumentException> { repository.exerciseForRecording(first.id) }
    }

    @Test
    fun t11PrimaryKeyAndRequiredColumnConstraintsRejectInvalidWrites() = runBlocking {
        ExerciseRepository(database).seedCatalogue()
        val before = database.exerciseDao().all()
        expectFailure<SQLiteConstraintException> { database.exerciseDao().insert(expected.first().toEntity()) }
        expectFailure<SQLiteConstraintException> {
            database.openHelper.writableDatabase.execSQL("UPDATE exercises SET name = NULL")
        }
        assertEquals(before, database.exerciseDao().all())
    }

    @Test
    fun t11IdentityCollisionRollsBackWholeSeedAndPreservesOriginal() = runBlocking {
        val collision = BuiltInCatalogue.exercises.last().copy(origin = ExerciseOrigin.CUSTOM, catalogueVersion = null)
        database.exerciseDao().insert(collision.toEntity())
        expectFailure<IllegalStateException> { ExerciseRepository(database).seedCatalogue() }
        assertEquals(listOf(collision.toEntity()), database.exerciseDao().all())
    }

    @Test
    fun t11UnknownStoredMeasurementFailsClosedWithoutResettingData() = runBlocking {
        ExerciseRepository(database).seedCatalogue()
        database.openHelper.writableDatabase.execSQL(
            "UPDATE exercises SET measurementKind = 'FUTURE_KIND' WHERE id = ?",
            arrayOf(expected.first().id),
        )
        expectFailure<IllegalArgumentException> { ExerciseRepository(database).seedCatalogue() }
        database.openHelper.readableDatabase.query("SELECT COUNT(*) FROM exercises").use {
            assertTrue(it.moveToFirst())
            assertEquals(6, it.getInt(0))
        }
    }

    @Test
    fun t11ExportedV1CreatesAndValidatesTheSameSchemaAsFreshRoom() = runBlocking {
        database.close()
        context.deleteDatabase(name)
        migration.createDatabase(name, 1).close()
        migration.runMigrationsAndValidate(name, 1, true).close()
        database = GymDatabase.build(context, name)
        assertEquals(expected, ExerciseRepository(database).recordableExercises())
        database.openHelper.readableDatabase.query("PRAGMA integrity_check").use {
            assertTrue(it.moveToFirst())
            assertEquals("ok", it.getString(0))
        }
    }

    @Test
    fun t11UnsupportedDatabaseVersionFailsWithoutDestructiveFallback() = runBlocking {
        ExerciseRepository(database).seedCatalogue()
        database.close()
        SQLiteDatabase.openDatabase(context.getDatabasePath(name).path, null, SQLiteDatabase.OPEN_READWRITE).use {
            it.version = 3
        }
        database = GymDatabase.build(context, name)
        expectFailure<IllegalStateException> { database.exerciseDao().all() }
        database.close()
        SQLiteDatabase.openDatabase(context.getDatabasePath(name).path, null, SQLiteDatabase.OPEN_READONLY).use { original ->
            assertEquals(3, original.version)
            original.rawQuery("SELECT COUNT(*) FROM exercises", null).use {
                assertTrue(it.moveToFirst())
                assertEquals(6, it.getInt(0))
            }
        }
    }

    private suspend inline fun <reified T : Throwable> expectFailure(crossinline action: suspend () -> Unit) {
        var failure: Throwable? = null
        try {
            action()
        } catch (caught: Throwable) {
            failure = caught
        }
        assertNotNull("Expected ${T::class.java.simpleName}", failure)
        assertTrue("Unexpected exception: ${failure?.javaClass?.simpleName}", failure is T)
    }
}
