package com.protas.time_deepseek.util

import java.util.Locale

/**
 * Formateador de cuenta regresiva de segundos a formato `HH:mm:ss`.
 *
 * Se ubica en `util` junto a [PriceFormatter] como función pura desacoplada del framework
 * de UI, garantizando idéntico comportamiento en cualquier componente y permitiendo pruebas
 * unitarias sin emulador.
 */
object CountdownFormatter {

    fun format(seconds: Long): String {
        val totalSeconds = seconds.coerceAtLeast(0L)
        val h = totalSeconds / 3600
        val m = (totalSeconds % 3600) / 60
        val s = totalSeconds % 60
        return String.format(Locale.US, "%02d:%02d:%02d", h, m, s)
    }
}

fun formatCountdown(seconds: Long): String = CountdownFormatter.format(seconds)
