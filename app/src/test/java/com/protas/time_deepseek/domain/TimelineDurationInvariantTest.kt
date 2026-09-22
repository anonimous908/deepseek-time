package com.protas.time_deepseek.domain

import java.time.Duration
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Valida las invariantes de duración para las ventanas finales antes y durante el fin de semana:
 * 63 h (viernes post-peak), 49 h (sábado completo) y 25 h (domingo completo hasta el inicio de peak el lunes).
 *
 * La última ventana de un día no se trunca a medianoche, sino que se extiende hasta el inicio del próximo
 * bloque peak en día hábil.
 */
class TimelineDurationInvariantTest {

    private fun lastRowHours(date: LocalDate): Long {
        val last = TariffSchedule.timeline(date).last()
        return Duration.between(last.start, last.end).toHours()
    }

    @Test
    fun fridayLastRowLasts63Hours() {
        // Viernes 10:00 (fin del último peak) hasta lunes 01:00.
        assertEquals(63L, lastRowHours(LocalDate.of(2026, 9, 18)))
    }

    @Test
    fun saturdayIsOneSingleRowOf49Hours() {
        val saturday = LocalDate.of(2026, 9, 19)
        assertEquals(1, TariffSchedule.timeline(saturday).size)
        assertEquals(49L, lastRowHours(saturday))
    }

    @Test
    fun sundayIsOneSingleRowOf25Hours() {
        val sunday = LocalDate.of(2026, 9, 20)
        assertEquals(1, TariffSchedule.timeline(sunday).size)
        assertEquals(25L, lastRowHours(sunday))
    }

    @Test
    fun aWeekdayHasFiveRows() {
        // 00:00-01:00 · 01:00-04:00 · 04:00-06:00 · 06:00-10:00 · 10:00-...
        assertEquals(5, TariffSchedule.timeline(LocalDate.of(2026, 9, 21)).size)
    }
}
