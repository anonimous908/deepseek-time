package com.protas.time_deepseek.domain

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Verifica la coherencia entre el límite final de la ventana activa ([RateWindow.end])
 * y el contador de transición ([TariffSchedule.nextTransition]), garantizando que ambos
 * apunten exactamente al mismo instante en fines de semana y transiciones interdiarias.
 */
class WindowEndCoherenceTest {

    @Test
    fun saturdayWindowEndsAtTheNextPeakStart() {
        val window = TariffSchedule.currentWindow(Instant.parse("2026-09-19T12:00:00Z"))
        assertEquals(Instant.parse("2026-09-21T01:00:00Z"), window.end)
    }

    @Test
    fun countdownAndWindowEndPointToTheSameInstant() {
        val now = Instant.parse("2026-09-19T12:00:00Z")
        assertEquals(TariffSchedule.currentWindow(now).end, TariffSchedule.nextTransition(now))
        // Intervalo de 37 horas: desde el sábado 12:00 UTC hasta el inicio del ciclo laboral el lunes 01:00 UTC.
        assertEquals(37 * 3600L, TariffSchedule.countdownSeconds(now))
    }
}
