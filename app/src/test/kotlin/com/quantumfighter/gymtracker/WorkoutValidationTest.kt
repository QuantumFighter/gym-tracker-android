package com.quantumfighter.gymtracker

import com.quantumfighter.gymtracker.data.exercise.BuiltInCatalogue
import com.quantumfighter.gymtracker.domain.LoadConvention
import com.quantumfighter.gymtracker.domain.MeasurementKind
import com.quantumfighter.gymtracker.domain.SaveSetCommand
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.util.UUID

class WorkoutValidationTest {
    private val exercise = BuiltInCatalogue.exercises.first()
    private val command = SaveSetCommand(UUID.randomUUID().toString(), UUID.randomUUID().toString(), null, 8, "WORKING")

    @Test fun t02UnsetZeroAndExactPositiveLoadsRemainDistinct() {
        listOf(null, 0L, 12345L, Long.MAX_VALUE).forEach { command.copy(loadGrams = it).validate(exercise) }
        assertNotEquals(command, command.copy(loadGrams = 0))
        command.copy(setType = "WARM_UP").validate(exercise)
    }

    @Test fun t02InvalidMeasurementIdentityTypeAndValuesAreRejected() {
        listOf(command.copy(id = "bad"), command.copy(exerciseId = "bad"), command.copy(repetitions = 0), command.copy(repetitions = -1), command.copy(loadGrams = -1), command.copy(unit = "lb"), command.copy(setType = "bad")).forEach {
            assertThrows(IllegalArgumentException::class.java) { it.validate(exercise) }
        }
        assertThrows(IllegalArgumentException::class.java) { command.validate(exercise.copy(measurementKind = MeasurementKind.DURATION)) }
        assertThrows(IllegalArgumentException::class.java) { command.copy(loadGrams = 0).validate(exercise.copy(loadConvention = LoadConvention.BODYWEIGHT)) }
        command.validate(exercise.copy(loadConvention = LoadConvention.BODYWEIGHT))
        command.copy(loadGrams = 1000).validate(exercise.copy(loadConvention = LoadConvention.ASSISTANCE))
    }
}
