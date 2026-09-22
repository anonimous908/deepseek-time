package com.protas.time_deepseek.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.protas.time_deepseek.domain.TariffState
import com.protas.time_deepseek.ui.ModelPriceLine

private data class ModelPresentation(
    val badgeTitle: String,
    val badgeBg: Color,
    val badgeColor: Color,
    val subtitle: String
)

private val FlashPresentation = ModelPresentation(
    badgeTitle = "⚡ RÁPIDO & LIGERO",
    badgeBg = ColorAmberContainer,
    badgeColor = ColorAmberText,
    subtitle = "Optimizado para baja latencia y alta concurrencia"
)

private val ProPresentation = ModelPresentation(
    badgeTitle = "🧠 RAZONAMIENTO PROFUNDO",
    badgeBg = ColorSkyContainer,
    badgeColor = ColorSkyText,
    subtitle = "Capacidad analítica avanzada y lógica matemática"
)

private fun resolveModelPresentation(displayName: String): ModelPresentation =
    if (displayName.contains("Flash", ignoreCase = true)) FlashPresentation else ProPresentation

@Composable
internal fun PricingSection(
    priceLines: List<ModelPriceLine>,
    tariffState: TariffState,
    modifier: Modifier = Modifier
) {
    if (priceLines.isEmpty()) return
    val isOffPeak = tariffState == TariffState.OFF_PEAK

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = "💳", fontSize = 16.sp)
            Text(
                text = "Tarifas de Modelos Activos",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = ColorSlateDark
            )
        }

        priceLines.forEach { line ->
            ModelCard(line = line, isOffPeak = isOffPeak)
        }
    }
}

@Composable
private fun ModelCard(
    line: ModelPriceLine,
    isOffPeak: Boolean,
    modifier: Modifier = Modifier
) {
    val meta = resolveModelPresentation(line.displayName)

    AppCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                    text = line.displayName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorSlateDark
                )
                Text(
                    text = meta.subtitle,
                    fontSize = 12.sp,
                    color = ColorSlateMuted
                )
            }

            Text(
                text = meta.badgeTitle,
                modifier = Modifier
                    .background(meta.badgeBg, BadgeShape)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = meta.badgeColor
            )
        }

        Row(
            modifier = InsetModifier,
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "PRECIOS VIGENTES",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorSlateMuted,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = line.priceText,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = ColorSlateDark
                )
            }

            if (isOffPeak) {
                Text(
                    text = "-50% OFF",
                    modifier = Modifier
                        .background(ColorOffPeakContainer, TagShape)
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorOffPeakText
                )
            }
        }
    }
}
