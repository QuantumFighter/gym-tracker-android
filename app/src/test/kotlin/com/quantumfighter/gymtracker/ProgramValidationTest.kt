package com.quantumfighter.gymtracker

import com.quantumfighter.gymtracker.data.exercise.BuiltInCatalogue
import com.quantumfighter.gymtracker.domain.LoadConvention
import com.quantumfighter.gymtracker.domain.LoadInput
import com.quantumfighter.gymtracker.domain.MeasurementKind
import com.quantumfighter.gymtracker.domain.SetTarget
import com.quantumfighter.gymtracker.feature.programs.SetDraft
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

/** B-03 / T-02: real boundary cases independent of Android/SQLite. */
class ProgramValidationTest {
    @Test
    fun validRangesRestAndExactMetricConversions() {
        SetTarget(1, Int.MAX_VALUE, Long.MAX_VALUE, Int.MAX_VALUE)
        assertEquals(12_345L, LoadInput.parseKg("12.345"))
        assertEquals(1L, LoadInput.parseKg("0.001"))
        assertEquals(12_345L, LoadInput.parseKg(LoadInput.formatKg(12_345)))
        assertEquals(Long.MAX_VALUE, LoadInput.parseKg(LoadInput.formatKg(Long.MAX_VALUE)))
        assertEquals(SetTarget(8, 12, null, 90), SetDraft().target())
    }

    @Test
    fun unsetAndZeroAreDifferentAndNeverInferred() {
        assertNull(LoadInput.parseKg(""))
        assertNull(LoadInput.parseKg("  "))
        assertEquals(0L, LoadInput.parseKg("0"))
        assertEquals(0L, LoadInput.parseKg("0.000"))
    }

    @Test
    fun invalidRangesNegativeValuesAndOverflowAreRejected() {
        listOf(
            { SetTarget(0, 12, null, 90) },
            { SetTarget(-1, 12, null, 90) },
            { SetTarget(12, 8, null, 90) },
            { SetTarget(8, 12, -1, 90) },
            { SetTarget(8, 12, null, -1) },
            { SetDraft(min = "2147483648").target() },
            { SetDraft(rest = "1.5").target() },
            { SetDraft(rest = "").target() },
            { SetDraft(max = "not reps").target() },
        ).forEach { action -> assertThrows(IllegalArgumentException::class.java) { action() } }
        listOf("-1", "NaN", "Infinity", "1e3", "1.0001", "1,5", "9223372036854775.808").forEach { text ->
            assertThrows(text, IllegalArgumentException::class.java) { LoadInput.parseKg(text) }
        }
    }

    @Test
    fun eligibilityAndLoadMeaningAreRechecked() {
        val exercise = BuiltInCatalogue.exercises.first()
        val target = SetTarget(8, 12, 0, 0)
        listOf(LoadConvention.TOTAL, LoadConvention.PER_HAND, LoadConvention.ASSISTANCE).forEach {
            target.validateFor(exercise.copy(loadConvention = it))
        }
        SetTarget(8, 12, null, 90).validateFor(exercise.copy(loadConvention = LoadConvention.BODYWEIGHT))
        assertThrows(IllegalArgumentException::class.java) { target.validateFor(exercise.copy(loadConvention = LoadConvention.BODYWEIGHT)) }
        assertThrows(IllegalArgumentException::class.java) { target.validateFor(exercise.copy(archived = true)) }
        MeasurementKind.entries.filter { it != MeasurementKind.REPETITIONS }.forEach {
            assertThrows(IllegalArgumentException::class.java) { target.validateFor(exercise.copy(measurementKind = it)) }
        }
    }
}
