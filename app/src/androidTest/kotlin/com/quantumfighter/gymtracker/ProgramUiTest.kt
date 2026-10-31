package com.quantumfighter.gymtracker

import androidx.activity.ComponentActivity
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.quantumfighter.gymtracker.app.GymTrackerApp
import com.quantumfighter.gymtracker.data.exercise.BuiltInCatalogue
import com.quantumfighter.gymtracker.data.exercise.ExerciseRepository
import com.quantumfighter.gymtracker.data.exercise.GymDatabase
import com.quantumfighter.gymtracker.data.program.ProgramRepository
import com.quantumfighter.gymtracker.domain.RoutineExerciseDraft
import com.quantumfighter.gymtracker.domain.SetTarget
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Real Compose → ViewModel → Room workflow, isolated from all installed app data. */
@RunWith(AndroidJUnit4::class)
class ProgramUiTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val name = "b03-ui-test.db"
    private lateinit var database: GymDatabase
    private var dismissInput: () -> Unit = {}

    @Before
    fun start() {
        context.deleteDatabase(name)
        database = GymDatabase.build(context, name)
        compose.setContent {
            val focus = LocalFocusManager.current
            val keyboard = LocalSoftwareKeyboardController.current
            SideEffect {
                dismissInput = {
                    focus.clearFocus()
                    keyboard?.hide()
                }
            }
            GymTrackerApp(ExerciseRepository(database), ProgramRepository(database))
        }
        compose.waitUntil(10_000) { compose.onAllNodes(hasText("Programs")).fetchSemanticsNodes().isNotEmpty() }
    }

    @After
    fun close() {
        compose.activityRule.scenario.close()
        database.close()
        context.deleteDatabase(name)
    }

    @Test
    fun t01CreateConfigureReorderSaveRetrieveAndSelectFromHome() {
        click("Programs")
        click("Create program")
        click("Save program")
        compose.onNodeWithTag("programError").performScrollTo().assertIsDisplayed()
        input("Program name", "Synthetic UI program")
        click("Save program")
        waitFor("Add routine to Synthetic UI program")
        click("Add routine to Synthetic UI program")
        input("Routine name", "Synthetic UI routine")
        click("Add exercise")
        click(BuiltInCatalogue.exercises.first().name)
        // Invalid entered data remains a draft, then can be corrected.
        field(0, "Minimum reps").performScrollTo().performTextReplacement("14")
        click("Save routine")
        compose.onNodeWithTag("programError").performScrollTo().assertIsDisplayed()
        field(0, "Minimum reps").performScrollTo().performTextReplacement("8")
        field(0, "Starting weight kg (optional)").performScrollTo().performTextReplacement("0")
        scoped(0, "+ Add set").performScrollTo().performClick()
        scoped(0, "Remove set 2").performScrollTo().performClick()
        click("Add exercise")
        click(BuiltInCatalogue.exercises[1].name)
        scoped(1, "Move up").performScrollTo().performClick()
        click("Save routine")
        waitFor("Saved routine")
        compose.onNodeWithText("1. ${BuiltInCatalogue.exercises[1].name}").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Set 1: 8–12 reps · Weight unset · 90 s rest").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Set 1: 8–12 reps · 0 kg · 90 s rest").performScrollTo().assertIsDisplayed()
        // Start Home on a different persisted choice so this proves selection changes the cursor.
        val alternativeId = runBlocking {
            val repository = ProgramRepository(database)
            val program = repository.readLibrary().programs.single()
            val id = repository.createRoutine(
                program.id,
                "Alternative UI routine",
                listOf(RoutineExerciseDraft("35bf2486-1052-4aa7-b571-025d9da867c8", BuiltInCatalogue.exercises.first().id, listOf(SetTarget(5, 5, null, 90)))),
            )
            repository.selectNextRoutine(id)
            id
        }
        click("Home")
        waitFor("Alternative UI routine")
        waitFor("Choose next routine")
        click("Choose next routine")
        click("Synthetic UI program · Synthetic UI routine")
        compose.onNodeWithText("Synthetic UI routine").performScrollTo().assertIsDisplayed()
        val saved = runBlocking { ProgramRepository(database).readLibrary() }
        assertNotEquals(alternativeId, saved.nextMembershipId)
        assertEquals(listOf(BuiltInCatalogue.exercises[1].id, BuiltInCatalogue.exercises.first().id), saved.nextRoutine!!.exercises.map { it.exercise.id })
        assertEquals(listOf(null, 0L), saved.nextRoutine!!.exercises.map { it.sets.single().loadGrams })
        click("Programs")
        click("Synthetic UI routine")
        waitFor("Saved routine")
        compose.onNodeWithText("2. ${BuiltInCatalogue.exercises.first().name}").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun t02EmptyRoutineAndZeroSetsCannotSaveAndDraftSurvivesFailure() {
        click("Programs")
        click("Create program")
        input("Program name", "Validation program")
        click("Save program")
        waitFor("Add routine to Validation program")
        click("Add routine to Validation program")
        input("Routine name", "Unsaved routine")
        click("Save routine")
        compose.onNodeWithText("Add at least one exercise.").performScrollTo().assertIsDisplayed()
        click("Add exercise")
        click(BuiltInCatalogue.exercises.first().name)
        scoped(0, "Remove set 1").performScrollTo().performClick()
        click("Save routine")
        compose.onNodeWithText("Each exercise needs at least one set.").performScrollTo().assertIsDisplayed()
        assertEquals(0, runBlocking { ProgramRepository(database).readLibrary().programs.single().routines.size })
        scoped(0, "+ Add set").performScrollTo().performClick()
        click("Save routine")
        waitFor("Saved routine")
        assertEquals(1, runBlocking { ProgramRepository(database).readLibrary().programs.single().routines.size })
    }

    private fun field(index: Int, label: String) = scoped(index, label)
    private fun scoped(index: Int, text: String) = compose.onNode(hasText(text) and hasAnyAncestor(hasTestTag("draftExercise$index")))
    private fun click(text: String) {
        // Prevent IME/focused-field scrolling from obscuring validation on the smaller CI phone.
        compose.runOnIdle { dismissInput() }
        compose.onNodeWithText(text).performScrollTo().performClick()
    }
    private fun input(label: String, value: String) {
        compose.onNodeWithText(label).performScrollTo().performTextReplacement(value)
    }
    private fun waitFor(text: String) {
        compose.waitUntil(10_000) { compose.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty() }
    }
}
