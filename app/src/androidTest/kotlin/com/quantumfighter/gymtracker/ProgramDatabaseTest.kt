package com.quantumfighter.gymtracker

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.quantumfighter.gymtracker.data.exercise.BuiltInCatalogue
import com.quantumfighter.gymtracker.data.exercise.ExerciseRepository
import com.quantumfighter.gymtracker.data.exercise.GymDatabase
import com.quantumfighter.gymtracker.data.exercise.PreservingOpenHelperFactory
import com.quantumfighter.gymtracker.data.program.AppSettingsEntity
import com.quantumfighter.gymtracker.data.program.ProgramRepository
import com.quantumfighter.gymtracker.data.program.ProgramRoutineEntity
import com.quantumfighter.gymtracker.data.program.RoutineExerciseEntity
import com.quantumfighter.gymtracker.data.program.SetPrescriptionEntity
import com.quantumfighter.gymtracker.domain.MeasurementKind
import com.quantumfighter.gymtracker.domain.RoutineExerciseDraft
import com.quantumfighter.gymtracker.domain.SetTarget
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

/** B-03 / T-01, T-02, T-11. Only isolated synthetic databases are created/deleted. */
@RunWith(AndroidJUnit4::class)
class ProgramDatabaseTest {
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val name = "b03-program-test.db"
    private lateinit var database: GymDatabase
    private val target = SetTarget(8, 12, null, 90)
    private val exercise = BuiltInCatalogue.exercises.first()
    private val second = BuiltInCatalogue.exercises[1]
    private val repository get() = ProgramRepository(database)

    @get:Rule
    val migration = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), GymDatabase::class.java, emptyList(), FrameworkSQLiteOpenHelperFactory())

    @Before
    fun open() {
        context.deleteDatabase(name)
        database = GymDatabase.build(context, name)
    }

    @After
    fun close() {
        database.close()
        context.deleteDatabase(name)
    }

    private fun occurrence(exerciseId: String = exercise.id, sets: List<SetTarget> = listOf(target)) = RoutineExerciseDraft(UUID.randomUUID().toString(), exerciseId, sets)

    @Test
    fun t01CreateOrderDuplicateOccurrencesAndReopenPreserveTheWholeGraph() = runBlocking {
        ExerciseRepository(database).seedCatalogue()
        val program = repository.createProgram("  Synthetic program  ")
        val drafts = listOf(occurrence(second.id), occurrence(sets = listOf(target, target.copy(loadGrams = 0, restSeconds = 0), target.copy(loadGrams = 12345))), occurrence())
        val firstId = repository.createRoutine(program, "Routine one", drafts)
        val otherId = repository.createRoutine(program, "Routine two", listOf(occurrence()))
        assertEquals(firstId, repository.readLibrary().nextMembershipId)
        repository.selectNextRoutine(otherId)
        val before = repository.readLibrary()
        val first = before.programs.single().routines.first()
        assertEquals("Synthetic program", before.programs.single().name)
        assertEquals(listOf("Routine one", "Routine two"), before.programs.single().routines.map { it.name })
        assertEquals(drafts.map { it.id }, first.exercises.map { it.id })
        assertEquals(drafts.map { it.exerciseId }, first.exercises.map { it.exercise.id })
        assertEquals(drafts.map { it.sets }, first.exercises.map { it.sets })
        assertNotEquals(first.exercises[1].id, first.exercises[2].id)
        val rawBefore = database.programDao().programs()
        database.close()
        database = GymDatabase.build(context, name)
        assertEquals(before, repository.readLibrary())
        assertEquals(rawBefore, database.programDao().programs())
        assertEquals(otherId, repository.readLibrary().nextRoutine?.membershipId)
    }

    @Test
    fun t02InvalidNamesEmptyTargetsMissingAndIneligibleExercisesCannotSave() = runBlocking {
        ExerciseRepository(database).seedCatalogue()
        expectFailure<IllegalArgumentException> { repository.createProgram(" ") }
        val program = repository.createProgram("P")
        expectFailure<IllegalArgumentException> { repository.createRoutine(program, " ", listOf(occurrence())) }
        expectFailure<IllegalArgumentException> { repository.createRoutine(program, "R", emptyList()) }
        expectFailure<IllegalArgumentException> { repository.createRoutine(program, "R", listOf(occurrence(sets = emptyList()))) }
        expectFailure<IllegalArgumentException> { repository.createRoutine("missing", "R", listOf(occurrence())) }
        expectFailure<IllegalArgumentException> { repository.createRoutine(program, "R", listOf(occurrence("missing"))) }
        val duplicate = occurrence()
        expectFailure<IllegalArgumentException> { repository.createRoutine(program, "R", listOf(duplicate, duplicate)) }
        for (kind in MeasurementKind.entries.filter { it != MeasurementKind.REPETITIONS }) {
            database.openHelper.writableDatabase.execSQL("UPDATE exercises SET measurementKind = ? WHERE id = ?", arrayOf(kind.name, exercise.id))
            expectFailure<IllegalArgumentException> { repository.createRoutine(program, "R", listOf(occurrence())) }
        }
        database.openHelper.writableDatabase.execSQL("UPDATE exercises SET measurementKind = 'REPETITIONS', archived = 1 WHERE id = ?", arrayOf(exercise.id))
        expectFailure<IllegalArgumentException> { repository.createRoutine(program, "R", listOf(occurrence())) }
        assertTrue(repository.readLibrary().programs.single().routines.isEmpty())
        database.openHelper.readableDatabase.query("SELECT COUNT(*) FROM routines").use {
            it.moveToFirst()
            assertEquals(0, it.getInt(0))
        }
    }

    @Test
    fun t11FailureHalfwayRollsBackRoutineMembershipAndEarlierPrescriptions() = runBlocking {
        ExerciseRepository(database).seedCatalogue()
        val program = repository.createProgram("P")
        val existing = occurrence()
        repository.createRoutine(program, "Existing", listOf(existing))
        val before = repository.readLibrary()
        expectFailure<SQLiteConstraintException> { repository.createRoutine(program, "Must roll back", listOf(occurrence(second.id), existing)) }
        assertEquals(before, repository.readLibrary())
        database.openHelper.readableDatabase.query("SELECT COUNT(*) FROM routines").use {
            it.moveToFirst()
            assertEquals(1, it.getInt(0))
        }
    }

    @Test
    fun t11ForeignKeysOrdinalUniquenessAndRestrictProtectRelationships() = runBlocking {
        ExerciseRepository(database).seedCatalogue()
        val program = repository.createProgram("P")
        val member = repository.createRoutine(program, "R", listOf(occurrence()))
        val routine = repository.readLibrary().nextRoutine!!
        val dao = database.programDao()
        expectFailure<SQLiteConstraintException> { dao.insertMembership(ProgramRoutineEntity(UUID.randomUUID().toString(), program, routine.routineId, 0)) }
        expectFailure<SQLiteConstraintException> { dao.insertMembership(ProgramRoutineEntity(UUID.randomUUID().toString(), "missing", routine.routineId, 1)) }
        expectFailure<SQLiteConstraintException> { dao.insertOccurrence(RoutineExerciseEntity(UUID.randomUUID().toString(), routine.routineId, second.id, 0)) }
        expectFailure<SQLiteConstraintException> { dao.insertOccurrence(RoutineExerciseEntity(UUID.randomUUID().toString(), routine.routineId, "missing", 1)) }
        expectFailure<SQLiteConstraintException> { dao.insertSet(SetPrescriptionEntity(UUID.randomUUID().toString(), routine.exercises.single().id, 0, 8, 12, null, 90)) }
        expectFailure<SQLiteConstraintException> { dao.insertSet(SetPrescriptionEntity(UUID.randomUUID().toString(), "missing", 1, 8, 12, null, 90)) }
        expectFailure<SQLiteConstraintException> { dao.saveSettings(AppSettingsEntity(nextProgramRoutineId = "missing")) }
        listOf("DELETE FROM exercises WHERE id = '${exercise.id}'", "DELETE FROM routines", "DELETE FROM programs", "DELETE FROM program_routines").forEach { sql ->
            expectFailure<SQLiteConstraintException> { database.openHelper.writableDatabase.execSQL(sql) }
        }
        assertEquals(member, repository.readLibrary().nextMembershipId)
        database.openHelper.readableDatabase.query("PRAGMA foreign_key_check").use { assertFalse(it.moveToFirst()) }
    }

    @Test
    fun t01ConcurrentRoutineCreationAllocatesDistinctOrderedMemberships() = runBlocking {
        ExerciseRepository(database).seedCatalogue()
        val program = repository.createProgram("P")
        coroutineScope {
            val a = async { repository.createRoutine(program, "A", listOf(occurrence())) }
            val b = async { repository.createRoutine(program, "B", listOf(occurrence())) }
            a.await()
            b.await()
        }
        assertEquals(listOf(0, 1), database.programDao().programs().single().memberships.map { it.membership.ordinal }.sorted())
    }

    @Test
    fun t02StaleSelectionRechecksCatalogueAndRetainsPreviousCursorOnFailure() = runBlocking {
        ExerciseRepository(database).seedCatalogue()
        val program = repository.createProgram("P")
        val first = repository.createRoutine(program, "A", listOf(occurrence()))
        val other = repository.createRoutine(program, "B", listOf(occurrence(second.id)))
        database.openHelper.writableDatabase.execSQL("UPDATE exercises SET archived = 1 WHERE id = ?", arrayOf(second.id))
        expectFailure<IllegalArgumentException> { repository.selectNextRoutine(other) }
        expectFailure<IllegalArgumentException> { repository.selectNextRoutine("missing") }
        assertEquals(first, repository.readLibrary().nextMembershipId)
    }

    @Test
    fun t11MigrationPreservesEveryCatalogueFieldIncludingCustomAndModifiedRows() = runBlocking {
        val expected = createV1Fixture()
        migration.runMigrationsAndValidate(name, 2, true, GymDatabase.MIGRATION_1_2).close()
        database = GymDatabase.build(context, name)
        assertEquals(expected, database.exerciseDao().all().toString())
        ExerciseRepository(database).seedCatalogue()
        assertEquals(expected, database.exerciseDao().all().toString())
        assertTrue(repository.readLibrary().programs.isEmpty())
        val program = repository.createProgram("Migrated program")
        repository.createRoutine(program, "Migrated routine", listOf(occurrence(second.id)))
        database.openHelper.readableDatabase.query("PRAGMA integrity_check").use {
            it.moveToFirst()
            assertEquals("ok", it.getString(0))
        }
        database.openHelper.readableDatabase.query("PRAGMA foreign_key_check").use { assertFalse(it.moveToFirst()) }
    }

    @Test
    fun t11InterruptedMigrationRollsBackThenProductionMigrationSucceeds() = runBlocking {
        val expected = createV1Fixture()
        database = Room.databaseBuilder(context, GymDatabase::class.java, name)
            .openHelperFactory(PreservingOpenHelperFactory())
            .addMigrations(
                object : Migration(1, 2) {
                    override fun migrate(db: SupportSQLiteDatabase) {
                        GymDatabase.MIGRATION_1_2.migrate(db)
                        error("Synthetic interruption")
                    }
                },
                GymDatabase.MIGRATION_2_3,
            ).build()
        expectFailure<IllegalStateException> { database.exerciseDao().all() }
        database.close()
        SQLiteDatabase.openDatabase(context.getDatabasePath(name).path, null, SQLiteDatabase.OPEN_READONLY).use { original ->
            assertEquals(1, original.version)
            original.rawQuery("SELECT COUNT(*) FROM exercises", null).use {
                it.moveToFirst()
                assertEquals(7, it.getInt(0))
            }
            original.rawQuery("SELECT name FROM sqlite_master WHERE name = 'programs'", null).use { assertFalse(it.moveToFirst()) }
        }
        database = GymDatabase.build(context, name)
        assertEquals(expected, database.exerciseDao().all().toString())
    }

    private fun createV1Fixture(): String {
        database.close()
        context.deleteDatabase(name)
        val definitions = BuiltInCatalogue.exercises.mapIndexed { index, value ->
            if (index == 0) value.copy(name = "Modified built-in", description = "Preserve modified metadata", archived = true) else value
        } + exercise.copy(id = "dbbb5804-5780-412d-9ffc-be595692829b", origin = com.quantumfighter.gymtracker.domain.ExerciseOrigin.CUSTOM, catalogueVersion = null, name = "Custom sentinel")
        migration.createDatabase(name, 1).use { old ->
            definitions.forEach { value ->
                old.execSQL(
                    "INSERT INTO exercises VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    arrayOf<Any?>(value.id, value.origin.name, value.catalogueVersion, value.name, value.description, value.muscleGroups.joinToString("|"), value.equipment.joinToString("|"), value.movementPatterns.joinToString("|"), value.exerciseType, value.measurementKind.name, value.loadConvention.name, if (value.archived) 1 else 0),
                )
            }
        }
        return definitions.sortedBy { it.name.lowercase() }.map { com.quantumfighter.gymtracker.data.exercise.ExerciseEntity(it.id, it.origin, it.catalogueVersion, it.name, it.description, it.muscleGroups, it.equipment, it.movementPatterns, it.exerciseType, it.measurementKind, it.loadConvention, it.archived) }.toString()
    }

    private suspend inline fun <reified T : Throwable> expectFailure(crossinline action: suspend () -> Unit) {
        val failure = try {
            action()
            null
        } catch (caught: Throwable) {
            caught
        }
        assertTrue("Expected ${T::class.java.simpleName}, got $failure", failure is T)
    }
}
