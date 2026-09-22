package com.protas.time_deepseek.domain

import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifica la cobertura completa del inicio del día (00:00 UTC) sin huecos temporales,
 * asegurando que siempre exista una ventana activa definida para cualquier instante.
 */
class TimelineGapTest {

    private val monday = LocalDate.of(2026, 9, 21)

    @Test
    fun theDayStartsAtMidnight() {
        val first = TariffSchedule.timeline(monday).first()
        assertEquals(Instant.parse("2026-09-21T00:00:00Z"), first.start)
        assertEquals(TariffState.OFF_PEAK, first.state)
    }

    @Test
    fun halfPastMidnightHasAWindow() {
        val window = TariffSchedule.currentWindow(Instant.parse("2026-09-21T00:30:00Z"))
        assertEquals(TariffState.OFF_PEAK, window.state)
    }

    @Test
    fun everyHourOfTheDayFallsInExactlyOneRow() {
        // Invariante de partición temporal completa: ni huecos ni solapes a lo largo de las 24 horas.
        val rows = TariffSchedule.timeline(monday)
        for (hour in 0 until 24) {
            val at = Instant.parse("2026-09-21T%02d:00:00Z".format(hour))
            val matches = rows.count { it.isCurrent(at) }
            assertTrue("la hora $hour cae en $matches filas, debería caer en 1", matches == 1)
        }
    }
}
