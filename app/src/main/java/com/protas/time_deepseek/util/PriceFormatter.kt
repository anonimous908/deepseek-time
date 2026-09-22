package com.protas.time_deepseek.util

import com.protas.time_deepseek.domain.PriceSet
import java.util.Locale

/**
 * Utilidad de formato para representaciones de precios en formato compacto (`In: $0.30  •  Out: $1.20`).
 *
 * Centraliza el formateo para asegurar coherencia visual entre la interfaz de usuario y el widget del sistema.
 * Aplica separación de responsabilidades: opera sobre un [PriceSet] ya resuelto por la lógica de dominio,
 * limitando su función a la transformación numérica y tipográfica.
 */
object PriceFormatter {

    fun format(prices: PriceSet, currency: String): String {
        val symbol = symbolFor(currency)

        // Espaciado canónico para preservar legibilidad en interfaces densas y widgets de pantalla de inicio.
        return "In: $symbol${amount(prices.cacheMiss)}  •  Out: $symbol${amount(prices.output)}"
    }

    // Retorna el símbolo monetario estándar para USD o el código ISO con espacio para divisas no mapeadas.
    private fun symbolFor(currency: String): String =
        if (currency == "USD") "$" else "$currency "

    /**
     * Formatea valores monetarios utilizando [Locale.US] para garantizar el punto decimal como separador estándar,
     * preservando consistencia con las convenciones financieras de cotización de APIs en USD independientemente del locale del dispositivo.
     */
    private fun amount(value: Double): String = String.format(Locale.US, "%.2f", value)
}
