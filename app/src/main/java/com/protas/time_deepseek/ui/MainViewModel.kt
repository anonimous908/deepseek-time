package com.protas.time_deepseek.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.protas.time_deepseek.domain.CatalogStatus
import com.protas.time_deepseek.domain.PricingCatalog
import com.protas.time_deepseek.domain.TariffSchedule
import com.protas.time_deepseek.domain.TariffState
import com.protas.time_deepseek.util.PriceFormatter
import java.time.Clock
import java.time.Instant
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** Línea de precio de un modelo formateada para representación en UI. */
data class ModelPriceLine(
    val modelId: String,
    val displayName: String,
    val priceText: String
)

/**
 * Estado inmutable de la interfaz de usuario.
 *
 * Modela el estado visual desacoplado de los recursos de plataforma, utilizando tipos de dominio
 * ([TariffState]) en lugar de cadenas localizadas para preservar la independencia de la capa de presentación.
 */
data class MainUiState(
    val tariffState: TariffState,
    val countdownSeconds: Long,
    val priceLines: List<ModelPriceLine>,
    val catalogStatus: CatalogStatus,
    val catalogVersion: String?
)

/**
 * ViewModel que expone el estado reactivo unificado de tarifas y catálogo de precios.
 *
 * [cargarCatalogo] actúa como puerto funcional desacoplado de la capa de datos.
 * La inyección de [clock] permite el control determinista del tiempo en pruebas automatizadas.
 */
class MainViewModel(
    cargarCatalogo: () -> Result<PricingCatalog>,
    private val clock: Clock
) : ViewModel() {

    // Carga estática inicial: el catálogo empaquetado es inmutable durante el ciclo de vida del proceso
    // y su tamaño reducido permite una lectura síncrona en inicialización sin impacto en la fluidez de la UI.
    private val catalog: PricingCatalog? = cargarCatalogo().getOrNull()

    private val _state = MutableStateFlow(buildState())
    val state: StateFlow<MainUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            while (isActive) {
                delay(TICK_MILLIS)
                _state.value = buildState()
            }
        }
    }

    private fun buildState(): MainUiState {
        val now = clock.instant()
        val tariff = TariffSchedule.currentState(now)
        return MainUiState(
            tariffState = tariff,
            countdownSeconds = TariffSchedule.countdownSeconds(now),
            priceLines = catalog?.let { priceLinesOf(it, tariff) }.orEmpty(),
            catalogStatus = catalogStatusOf(catalog, now),
            catalogVersion = catalog?.catalogVersion
        )
    }

    private fun priceLinesOf(catalog: PricingCatalog, tariff: TariffState): List<ModelPriceLine> =
        catalog.activeModels.map { model ->
            ModelPriceLine(
                modelId = model.id,
                displayName = model.displayName,
                priceText = PriceFormatter.format(model.pricesFor(tariff), catalog.currency)
            )
        }

    /**
     * Determina el estado de disponibilidad del catálogo con precedencia estricta:
     * la ausencia de datos ([CatalogStatus.UNAVAILABLE]) prima sobre la expiración temporal ([CatalogStatus.STALE]).
     */
    private fun catalogStatusOf(catalog: PricingCatalog?, now: Instant): CatalogStatus = when {
        catalog == null -> CatalogStatus.UNAVAILABLE
        catalog.isStale(now) -> CatalogStatus.STALE
        else -> CatalogStatus.OK
    }

    private companion object {
        const val TICK_MILLIS = 1_000L
    }
}
