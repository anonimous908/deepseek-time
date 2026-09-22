package com.protas.time_deepseek.domain

import java.time.Duration
import java.time.Instant

// Tipos de dominio puros (capa central). No dependen de ningún framework de Android ni
// de capas externas del sistema (únicamente de java.time).

enum class TariffState {
    PEAK,
    OFF_PEAK
}

data class PriceSet(
    val cacheHit: Double,
    val cacheMiss: Double,
    val output: Double
) {
    /**
     * Calcula las tarifas reducidas de horario valle (off-peak), equivalentes al 50% de la tarifa base (peak).
     *
     * Para garantizar coherencia y evitar desincronizaciones accidentales en la fuente de datos,
     * el catálogo almacena únicamente los precios base y deriva las tarifas con descuento en tiempo de ejecución.
     * La división entre 2.0 en punto flotante IEEE 754 es exacta (solo altera el exponente de base 2),
     * garantizando precisión aritmética sin error de redondeo.
     */
    fun halved(): PriceSet = PriceSet(
        cacheHit = cacheHit / 2.0,
        cacheMiss = cacheMiss / 2.0,
        output = output / 2.0
    )
}

enum class ModelStatus {
    ACTIVE,
    RETIRED
}

data class ModelPricing(
    val id: String,
    val displayName: String,
    val status: ModelStatus,
    val peak: PriceSet
) {

    fun pricesFor(state: TariffState): PriceSet = when (state) {
        TariffState.PEAK -> peak
        TariffState.OFF_PEAK -> peak.halved()
    }
}

data class RateWindow(
    val start: Instant,
    val end: Instant,
    val state: TariffState
) {
    /**
     * Determina si el instante dado pertenece a esta ventana según un intervalo semiabierto `[start, end)`.
     *
     * El límite inicial es inclusivo y el límite final es exclusivo. Esto asegura que cualquier instante
     * en una frontera pertenezca a exactamente una franja tarifaria, evitando solapamientos o discontinuidades.
     *
     * Utiliza [Instant] (tiempo UTC absoluto) en lugar de horas locales para garantizar comparaciones
     * unívocas e independientes de husos horarios locales (incluyendo zonas con desfases no enteros o cambios de hora).
     */
    fun isCurrent(instant: Instant): Boolean =
        !instant.isBefore(start) && instant.isBefore(end)
}

data class PricingCatalog(
    val schemaVersion: Int,
    val catalogVersion: String,
    val updatedAt: Instant,
    val staleAfterDays: Int,
    val currency: String,
    val unit: String,
    val models: List<ModelPricing>,
    val retiredModelIds: List<String>
) {
    /**
     * Modelos activos disponibles para visualización en la interfaz y el widget.
     *
     * Centraliza la regla de filtrado en el modelo de dominio para garantizar una única fuente
     * de verdad y evitar discrepancias entre la pantalla principal y el widget del sistema.
     */
    val activeModels: List<ModelPricing>
        get() = models.filter { it.status == ModelStatus.ACTIVE }

    /**
     * Evalúa si el catálogo empaquetado ha superado el umbral de vigencia configurado ([staleAfterDays]).
     *
     * Determina la caducidad temporal de la copia local empaquetada en el cliente respecto a su fecha
     * de compilación ([updatedAt]), permitiendo alertar al usuario de posible obsolescencia sin acoplar
     * la política de expiración al código fuente compilado.
     */
    fun isStale(now: Instant): Boolean {
        val deadline = updatedAt.plus(Duration.ofDays(staleAfterDays.toLong()))
        return !now.isBefore(deadline)
    }
}

enum class CatalogStatus {
    OK,
    STALE,
    UNAVAILABLE
}
