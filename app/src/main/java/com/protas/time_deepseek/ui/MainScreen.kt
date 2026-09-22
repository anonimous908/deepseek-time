package com.protas.time_deepseek.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.protas.time_deepseek.domain.CatalogStatus
import com.protas.time_deepseek.domain.TariffState
import com.protas.time_deepseek.ui.components.CatalogStatusFooter
import com.protas.time_deepseek.ui.components.ColorSlateLight
import com.protas.time_deepseek.ui.components.HeroStatusCard
import com.protas.time_deepseek.ui.components.PricingSection
import com.protas.time_deepseek.ui.components.TimelineCard
import com.protas.time_deepseek.ui.components.TopBarSection
import java.time.Instant
import java.time.ZoneOffset

/**
 * Pantalla principal interactiva en Jetpack Compose.
 *
 * Arquitectura modular:
 * 1. Orquestador reactivo desacoplado que coordina componentes especializados en [ui.components].
 * 2. Aislamiento de fases de Compose: el ticker de 1 Hz solo invalida el nodo de texto en [HeroStatusCard].
 * 3. Renderizado de aguja horaria en GPU pura (Draw phase) en [TimelineCard].
 */
@Composable
fun MainScreen(state: MainUiState, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = ColorSlateLight
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TopBarSection()

            HeroStatusCard(
                tariffState = state.tariffState,
                countdownSecondsProvider = { state.countdownSeconds }
            )

            TimelineCard(
                progressProvider = {
                    val zdt = Instant.now().atZone(ZoneOffset.UTC)
                    (zdt.toLocalTime().toSecondOfDay()) / 86400f
                }
            )

            PricingSection(
                priceLines = state.priceLines,
                tariffState = state.tariffState
            )

            CatalogStatusFooter(
                catalogStatus = state.catalogStatus,
                catalogVersion = state.catalogVersion
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ============================================================================
// Previews Compactadas con PreviewParameter
// ============================================================================

private class MainUiStatePreviewProvider : PreviewParameterProvider<MainUiState> {
    override val values = sequenceOf(
        MainUiState(
            tariffState = TariffState.PEAK,
            countdownSeconds = 10_800,
            priceLines = listOf(
                ModelPriceLine("flash", "DeepSeek-V4.1-Flash", "In: $0.30  •  Out: $1.20"),
                ModelPriceLine("pro", "DeepSeek-V4-Pro-0813", "In: $1.32  •  Out: $3.96")
            ),
            catalogStatus = CatalogStatus.OK,
            catalogVersion = "2026.09.18"
        ),
        MainUiState(
            tariffState = TariffState.OFF_PEAK,
            countdownSeconds = 133_200,
            priceLines = listOf(
                ModelPriceLine("flash", "DeepSeek-V4.1-Flash", "In: $0.15  •  Out: $0.60")
            ),
            catalogStatus = CatalogStatus.STALE,
            catalogVersion = "2026.06.01"
        ),
        MainUiState(
            tariffState = TariffState.OFF_PEAK,
            countdownSeconds = 133_200,
            priceLines = emptyList(),
            catalogStatus = CatalogStatus.UNAVAILABLE,
            catalogVersion = null
        )
    )
}

@Preview(showBackground = true, name = "DeepSeek Time Screens")
@Composable
private fun MainScreenPreview(
    @PreviewParameter(MainUiStatePreviewProvider::class) state: MainUiState
) {
    MaterialTheme {
        MainScreen(state = state)
    }
}
