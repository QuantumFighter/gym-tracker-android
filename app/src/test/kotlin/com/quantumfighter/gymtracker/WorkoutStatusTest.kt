package com.quantumfighter.gymtracker

import com.quantumfighter.gymtracker.domain.exerciseStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class WorkoutStatusTest {
    @Test fun unfinishedWorkIsPartialOrSkippedAndBacktrackingRestoresPending() {
        assertEquals("PENDING", exerciseStatus(false, false, false))
        assertEquals("SKIPPED", exerciseStatus(false, false, true))
        assertEquals("PARTIAL", exerciseStatus(false, true, false))
        assertEquals("PARTIAL", exerciseStatus(false, true, true))
        listOf(false, true).forEach { ending -> assertEquals("COMPLETE", exerciseStatus(true, true, ending)) }
    }
}
