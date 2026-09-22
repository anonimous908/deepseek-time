package com.protas.time_deepseek.domain

import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Garantiza que los fines de semana (sábado y domingo) permanezcan en tarifa reducida (OFF-PEAK)
 * durante las 24 horas completas, independientemente de las horas solapadas con franjas peak semanales.
 */
class WeekendGuardTest {

    @Test
    fun weekendIsOffPeakEvenInsidePeakHours() {
        // Sábado 07:00 UTC: horario coincidente con franja peak en días laborables.
        assertEquals(
            TariffState.OFF_PEAK,
            TariffSchedule.currentState(Instant.parse("2026-09-19T07:00:00Z"))
        )
        // Domingo 02:00 UTC: lo mismo, en la primera franja.
        assertEquals(
            TariffState.OFF_PEAK,
            TariffSchedule.currentState(Instant.parse("2026-09-20T02:00:00Z"))
        )
    }

    @Test
    fun theSameHourOnAWeekdayIsPeak() {
        // Control: verifica que la misma hora en día laborable sí clasifique como PEAK.
        assertEquals(
            TariffState.PEAK,
            TariffSchedule.currentState(Instant.parse("2026-09-21T07:00:00Z"))
        )
    }

    @Test
    fun weekendHasNoPeakWindowsAtAll() {
        val saturday = TariffSchedule.timeline(LocalDate.of(2026, 9, 19))
        assertEquals(0, saturday.count { it.state == TariffState.PEAK })
    }
}
