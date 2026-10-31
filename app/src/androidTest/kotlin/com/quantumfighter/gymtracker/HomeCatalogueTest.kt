package com.quantumfighter.gymtracker

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.quantumfighter.gymtracker.data.exercise.BuiltInCatalogue
import com.quantumfighter.gymtracker.data.exercise.ExerciseRepository
import com.quantumfighter.gymtracker.data.exercise.GymDatabase
import com.quantumfighter.gymtracker.data.exercise.toEntity
import com.quantumfighter.gymtracker.domain.ExerciseOrigin
import com.quantumfighter.gymtracker.feature.home.HomeViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** T-01 failure subset: real repository failure reaches Home; retry retains committed records. */
@RunWith(AndroidJUnit4::class)
class HomeCatalogueTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context: Context = instrumentation.targetContext
    private val name = "b02-home-test.db"
    private val store = ViewModelStore()
    private lateinit var database: GymDatabase

    @Before
    fun openIsolatedDatabase() {
        context.deleteDatabase(name)
        database = GymDatabase.build(context, name)
    }

    @After
    fun releaseStateAndTestDatabase() {
        instrumentation.runOnMainSync { store.clear() }
        database.close()
        context.deleteDatabase(name)
    }

    @Test
    fun failedSeedShowsErrorAndRetryCanRecoverWithoutResettingData() = runBlocking {
        val collision = BuiltInCatalogue.exercises.last().copy(origin = ExerciseOrigin.CUSTOM, catalogueVersion = null)
        database.exerciseDao().insert(collision.toEntity())
        lateinit var home: HomeViewModel
        instrumentation.runOnMainSync {
            home = ViewModelProvider.create(
                store,
                viewModelFactory { initializer { HomeViewModel(ExerciseRepository(database)) } },
            )[HomeViewModel::class]
        }
        val failed = withTimeout(10_000) { home.uiState.first { !it.catalogueLoading } }
        assertTrue(failed.catalogueFailed)
        assertEquals(listOf(collision.toEntity()), database.exerciseDao().all())

        // Repair only the isolated synthetic fixture; production never deletes data on retry.
        database.openHelper.writableDatabase.execSQL("DELETE FROM exercises WHERE id = ?", arrayOf(collision.id))
        instrumentation.runOnMainSync { home.retryCatalogue() }
        val recovered = withTimeout(10_000) { home.uiState.first { !it.catalogueLoading } }
        assertFalse(recovered.catalogueFailed)
        assertEquals(6, database.exerciseDao().all().size)
    }
}
