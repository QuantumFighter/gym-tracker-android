package com.quantumfighter.gymtracker

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.quantumfighter.gymtracker.data.exercise.ExerciseRepository
import com.quantumfighter.gymtracker.data.exercise.GymDatabase
import com.quantumfighter.gymtracker.data.program.ProgramRepository
import com.quantumfighter.gymtracker.feature.programs.ProgramsRoute
import com.quantumfighter.gymtracker.feature.programs.ProgramsViewModel
import com.quantumfighter.gymtracker.feature.programs.SetDraft
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger

/** B-03 recovery: inject observer failures around real Room reads without damaging/resetting storage. */
@RunWith(AndroidJUnit4::class)
class ProgramsRecoveryTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val name = "b03-observer-recovery-test.db"
    private lateinit var database: GymDatabase
    private lateinit var repository: ProgramRepository
    private lateinit var viewModel: ProgramsViewModel
    private val failReads = MutableStateFlow(false)
    private val subscriptions = AtomicInteger()
    private val active = AtomicInteger()
    private val peakActive = AtomicInteger()

    @Before
    fun open() {
        context.deleteDatabase(name)
        database = GymDatabase.build(context, name)
        repository = ProgramRepository(database)
    }

    @After
    fun close() {
        compose.activityRule.scenario.close()
        database.close()
        context.deleteDatabase(name)
    }

    private fun start() {
        val library = flow {
            subscriptions.incrementAndGet()
            val currentActive = active.incrementAndGet()
            peakActive.updateAndGet { maxOf(it, currentActive) }
            try {
                emitAll(
                    combine(repository.observeLibrary(), failReads) { value, failed ->
                        if (failed) throw IOException("Synthetic observer read failure")
                        value
                    },
                )
            } finally {
                active.decrementAndGet()
            }
        }
        compose.runOnUiThread {
            viewModel = ViewModelProvider.create(
                compose.activity,
                viewModelFactory { initializer { ProgramsViewModel(repository, ExerciseRepository(database), library) } },
            )[ProgramsViewModel::class]
        }
        compose.setContent { ProgramsRoute(viewModel, onHome = {}) }
        compose.waitUntil(10_000) { !viewModel.state.value.loading }
    }

    @Test
    fun retryAfterLoadedObserverFailureRetainsDraftAndResumesOneLiveObserver() {
        val programId = runBlocking { repository.createProgram("Existing program") }
        start()
        assertTrue(viewModel.state.value.catalogue.isNotEmpty())
        val catalogue = viewModel.state.value.catalogue
        compose.runOnUiThread {
            viewModel.newRoutine(programId)
            viewModel.routineName("Retained routine draft")
            viewModel.addExercise(catalogue.first())
            viewModel.set(viewModel.state.value.draft.single().id, 0, SetDraft("3", "5", "12.345", "45"))
        }
        val draft = viewModel.state.value.draft
        val savedBeforeFailure = runBlocking { repository.readLibrary() }
        failReads.value = true
        compose.waitUntil(10_000) { viewModel.state.value.loadFailed && active.get() == 0 }
        compose.onNodeWithTag("programLoadError").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Retry").performScrollTo().assertIsDisplayed()
        assertEquals(catalogue, viewModel.state.value.catalogue)
        assertEquals(savedBeforeFailure, runBlocking { repository.readLibrary() })
        // This committed write is invisible to the terminated observer until the user retries.
        runBlocking { repository.createProgram("Saved during outage") }
        assertFalse(viewModel.state.value.library.programs.any { it.name == "Saved during outage" })
        failReads.value = false
        compose.onNodeWithText("Retry").performClick()
        compose.waitUntil(10_000) { !viewModel.state.value.loading && viewModel.state.value.library.programs.any { it.name == "Saved during outage" } }
        assertFalse(viewModel.state.value.loadFailed)
        assertEquals("Retained routine draft", viewModel.state.value.routineName)
        assertEquals(draft, viewModel.state.value.draft)
        assertEquals(catalogue, viewModel.state.value.catalogue)
        compose.onNodeWithText("Retry").assertDoesNotExist()
        compose.runOnUiThread { repeat(5) { viewModel.load() } }
        assertEquals(2, subscriptions.get())
        assertEquals(1, active.get())
        assertEquals(1, peakActive.get())
        runBlocking { repository.createProgram("Saved after recovery") }
        compose.waitUntil(10_000) { viewModel.state.value.library.programs.any { it.name == "Saved after recovery" } }
        compose.runOnUiThread { viewModel.back() }
        compose.onNodeWithText("Saved during outage").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Saved after recovery").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun initialLoadFailureCanRetryAndValidationErrorsDoNotOfferStorageRetry() {
        failReads.value = true
        start()
        assertTrue(viewModel.state.value.loadFailed)
        assertTrue(viewModel.state.value.catalogue.isEmpty())
        failReads.value = false
        compose.onNodeWithText("Retry").performScrollTo().performClick()
        compose.waitUntil(10_000) { !viewModel.state.value.loading && viewModel.state.value.catalogue.isNotEmpty() }
        compose.onNodeWithText("Create program").performScrollTo().performClick()
        compose.onNodeWithText("Save program").performScrollTo().performClick()
        compose.onNodeWithText("Program name is required.").performScrollTo().assertIsDisplayed()
        assertFalse(viewModel.state.value.loadFailed)
        compose.onNodeWithText("Retry").assertDoesNotExist()
        assertEquals(2, subscriptions.get())
        assertEquals(1, active.get())
        assertEquals(1, peakActive.get())
    }
}
