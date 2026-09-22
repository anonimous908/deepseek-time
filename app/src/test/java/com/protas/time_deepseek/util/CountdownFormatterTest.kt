package com.protas.time_deepseek.util

import org.junit.Assert.assertEquals
import org.junit.Test

class CountdownFormatterTest {

    @Test
    fun formatReturnsZeroWhenZero() {
        assertEquals("00:00:00", CountdownFormatter.format(0))
    }

    @Test
    fun formatHandlesNegativeSecondsSafely() {
        assertEquals("00:00:00", CountdownFormatter.format(-10))
    }

    @Test
    fun formatHandlesMinutesAndSecondsCorrectly() {
        assertEquals("00:01:05", CountdownFormatter.format(65))
    }

    @Test
    fun formatHandlesHoursCorrectly() {
        assertEquals("03:00:00", CountdownFormatter.format(10_800))
    }

    @Test
    fun formatHandlesHoursExceeding24Hours() {
        assertEquals("37:00:00", CountdownFormatter.format(133_200))
    }
}
