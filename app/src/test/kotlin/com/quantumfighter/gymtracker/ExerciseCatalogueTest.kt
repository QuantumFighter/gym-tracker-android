package com.quantumfighter.gymtracker

import com.quantumfighter.gymtracker.data.exercise.BuiltInCatalogue
import com.quantumfighter.gymtracker.domain.ExerciseOrigin
import com.quantumfighter.gymtracker.domain.LoadConvention
import com.quantumfighter.gymtracker.domain.MeasurementKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/** B-02 / T-01, T-02: review fixture locks identities, required metadata and load meanings. */
class ExerciseCatalogueTest {
    private val exercises = BuiltInCatalogue.exercises

    @Test
    fun catalogueV1HasSixFixedDistinctIdentitiesAndExplicitLoadConventions() {
        assertEquals(
            (1..6).map { "6ae90adc-270d-4af1-ad4e-451410de100$it" },
            exercises.map { it.id },
        )
        assertEquals(
            listOf(
                LoadConvention.TOTAL,
                LoadConvention.TOTAL,
                LoadConvention.TOTAL,
                LoadConvention.PER_HAND,
                LoadConvention.BODYWEIGHT,
                LoadConvention.ASSISTANCE,
            ),
            exercises.map { it.loadConvention },
        )
        exercises.forEach {
            assertEquals(ExerciseOrigin.BUILT_IN, it.origin)
            assertEquals(1, it.catalogueVersion)
            assertEquals(MeasurementKind.REPETITIONS, it.measurementKind)
            assertEquals("strength", it.exerciseType)
            assertTrue(it.name.isNotBlank() && it.description.isNotBlank())
            assertTrue(it.muscleGroups.isNotEmpty() && it.equipment.isNotEmpty() && it.movementPatterns.isNotEmpty())
            assertTrue(it.canRecord)
        }
    }

    @Test
    fun archivedAndEveryUnsupportedMeasurementKindAreNotRecordable() {
        assertFalse(exercises.first().copy(archived = true).canRecord)
        MeasurementKind.entries.filter { it != MeasurementKind.REPETITIONS }.forEach {
            assertFalse(exercises.first().copy(measurementKind = it).canRecord)
        }
    }

    @Test
    fun invalidMetadataAndOriginVersionPairsAreRejectedBeforePersistence() {
        val exercise = exercises.first()
        listOf<() -> Any>(
            { exercise.copy(id = "squat") },
            { exercise.copy(name = " ") },
            { exercise.copy(description = "") },
            { exercise.copy(muscleGroups = emptyList()) },
            { exercise.copy(equipment = listOf("bar|bell")) },
            { exercise.copy(movementPatterns = listOf("squat", "squat")) },
            { exercise.copy(exerciseType = "") },
            { exercise.copy(catalogueVersion = 0) },
            { exercise.copy(catalogueVersion = null) },
            { exercise.copy(origin = ExerciseOrigin.CUSTOM) },
        ).forEach { invalid -> assertThrows(IllegalArgumentException::class.java) { invalid() } }
        assertEquals(null, exercise.copy(origin = ExerciseOrigin.CUSTOM, catalogueVersion = null).catalogueVersion)
    }
}
