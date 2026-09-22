package com.protas.time_deepseek.domain

import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

/**
 * Reglas de programación horaria y cálculo de tarifas de DeepSeek.
 *
 * Se modela como un `object` singleton sin estado al tratarse de funciones puras que transforman
 * instantes temporales ([Instant]) en ventanas tarifarias ([RateWindow]), asegurando una única
 * definición canónica de los horarios en toda la aplicación.
 *
 * Todos los cálculos operan estrictamente sobre el estándar UTC, coincidiendo con la especificación
 * oficial de la API de DeepSeek. Las capas de presentación son responsables de la conversión a hora local.
 */
object TariffSchedule {

    // Franjas peak de DeepSeek, en UTC.
    private val PEAK_1_START = LocalTime.of(1, 0)
    private val PEAK_1_END = LocalTime.of(4, 0)
    private val PEAK_2_START = LocalTime.of(6, 0)
    private val PEAK_2_END = LocalTime.of(10, 0)

    /**
     * Retorna la secuencia ordenada y exhaustiva de ventanas tarifarias ([RateWindow]) para una fecha dada en UTC.
     * Cubre desde el inicio del día (00:00 UTC) sin huecos ni solapes. La ventana final se extiende hasta el próximo
     * inicio de tarifa pico, garantizando continuidad temporal más allá de la medianoche.
     */
    fun timeline(date: LocalDate): List<RateWindow> {
        val rows = mutableListOf<RateWindow>()
        var cursor = date.atStartOfDay(ZoneOffset.UTC).toInstant()

        for (peak in peakWindowsOn(date)) {
            // Si existe un intervalo entre el cursor actual y el inicio de la ventana pico,
            // se emite una ventana valle (off-peak). Esto cubre de manera continua el periodo
            // inicial de la jornada (por ejemplo, [00:00, 01:00 UTC] los días laborables).
            if (cursor.isBefore(peak.start)) {
                rows += RateWindow(cursor, peak.start, TariffState.OFF_PEAK)
            }
            rows += peak
            cursor = peak.end
        }

        // La última ventana valle del día no se trunca a medianoche, sino que se extiende
        // hasta el inicio del siguiente ciclo pico. Esto alinea de forma determinista el final
        // de la ventana activa con el contador de cuenta regresiva para el próximo cambio de tarifa.
        val nextPeakStart =
            nextPeakStartOnOrAfter(date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant())
        rows += RateWindow(cursor, nextPeakStart, TariffState.OFF_PEAK)

        return rows
    }

    /**
     * Obtiene la ventana tarifaria activa para el instante especificado.
     *
     * El itinerario diario forma un recubrimiento continuo y exhaustivo de la línea temporal,
     * por lo que la búsqueda de la ventana contenedora es determinista e invariante (nunca nula).
     * Utiliza `first` para fallar rápidamente (fail-fast) ante cualquier incoherencia en la definición de franjas.
     */
    fun currentWindow(instant: Instant): RateWindow =
        timeline(instant.atZone(ZoneOffset.UTC).toLocalDate()).first { it.isCurrent(instant) }

    fun currentState(instant: Instant): TariffState = currentWindow(instant).state

    /** El instante exacto en que cambia la tarifa. */
    fun nextTransition(instant: Instant): Instant = currentWindow(instant).end

    /**
     * Retorna los segundos restantes hasta la próxima transición de tarifa.
     *
     * Se calcula directamente a partir del límite superior de la ventana activa ([currentWindow]),
     * garantizando consistencia absoluta entre la cuenta regresiva y la representación visual de las franjas.
     */
    fun countdownSeconds(instant: Instant): Long =
        Duration.between(instant, nextTransition(instant)).seconds

    // Los fines de semana (sábado y domingo completos) mantienen tarifa valle (off-peak) continua sin franjas pico.
    private fun peakWindowsOn(date: LocalDate): List<RateWindow> =
        if (!isWeekday(date)) {
            emptyList()
        } else {
            listOf(
                window(date, PEAK_1_START, PEAK_1_END),
                window(date, PEAK_2_START, PEAK_2_END)
            )
        }

    private fun isWeekday(date: LocalDate): Boolean =
        date.dayOfWeek != DayOfWeek.SATURDAY && date.dayOfWeek != DayOfWeek.SUNDAY

    private fun window(date: LocalDate, start: LocalTime, end: LocalTime): RateWindow =
        RateWindow(
            start = date.atTime(start).toInstant(ZoneOffset.UTC),
            end = date.atTime(end).toInstant(ZoneOffset.UTC),
            state = TariffState.PEAK
        )

    /**
     * Localiza el inicio del próximo intervalo pico a partir de [from].
     *
     * Explora un horizonte de hasta 4 días para contemplar transiciones que atraviesan fines de semana completos
     * (por ejemplo, desde el viernes posterior a las 10:00 UTC hasta el lunes a las 01:00 UTC).
     */
    private fun nextPeakStartOnOrAfter(from: Instant): Instant {
        var date = from.atZone(ZoneOffset.UTC).toLocalDate()
        repeat(4) {
            peakWindowsOn(date).forEach { peak ->
                if (!peak.start.isBefore(from)) return peak.start
            }
            date = date.plusDays(1)
        }
        error("No hay ninguna ventana peak en los próximos 4 días")
    }
}
