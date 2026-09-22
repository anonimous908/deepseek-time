package com.protas.time_deepseek.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.protas.time_deepseek.domain.TariffState
import com.protas.time_deepseek.util.CountdownFormatter

private data class HeroCardStyle(
    val cardBorder: BorderStroke,
    val labelColor: Color,
    val badgeBg: Color,
    val badgeBorder: BorderStroke,
    val badgeIcon: String,
    val badgeText: String,
    val badgeTextColor: Color,
    val countdownColor: Color,
    val description: String
)

private val PeakHeroStyle = HeroCardStyle(
    cardBorder = PeakCardBorder,
    labelColor = ColorPeakLabel,
    badgeBg = ColorPeakContainer,
    badgeBorder = PeakBadgeBorder,
    badgeIcon = "⚡",
    badgeText = "TARIFA 100% (PEAK)",
    badgeTextColor = ColorPeakText,
    countdownColor = ColorPeakCrimson,
    description = "Franja de alta demanda en curso. Al finalizar comenzará el descuento del 50% en todos los tokens."
)

private val OffPeakHeroStyle = HeroCardStyle(
    cardBorder = OffPeakCardBorder,
    labelColor = ColorOffPeakLabel,
    badgeBg = ColorOffPeakContainer,
    badgeBorder = OffPeakBadgeBorder,
    badgeIcon = "🏷",
    badgeText = "50% DESCUENTO (OFF-PEAK)",
    badgeTextColor = ColorOffPeakText,
    countdownColor = ColorOffPeakEmerald,
    description = "Aprovecha el 50% OFF actual para inferencia masiva y procesamiento antes de la próxima franja pico."
)

@Composable
internal fun HeroStatusCard(
    tariffState: TariffState,
    countdownSecondsProvider: () -> Long,
    modifier: Modifier = Modifier
) {
    val style = if (tariffState == TariffState.PEAK) PeakHeroStyle else OffPeakHeroStyle

    AppCard(modifier = modifier, border = style.cardBorder, padding = 20.dp) {
        // Cabecera estática (se omite en los ticks de 1 Hz del temporizador)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ESTADO DE TARIFA",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = style.labelColor,
                letterSpacing = 1.sp
            )

            Surface(
                shape = ContainerShape,
                color = style.badgeBg,
                border = style.badgeBorder
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = style.badgeIcon, fontSize = 12.sp)
                    Text(
                        text = style.badgeText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = style.badgeTextColor
                    )
                }
            }
        }

        // Único nodo hoja que recompone a 1 Hz con jerarquía aplanada (buildAnnotatedString)
        CountdownRow(
            countdownSeconds = countdownSecondsProvider(),
            color = style.countdownColor
        )

        // Texto multilínea estático (evita re-layout cada segundo)
        Text(
            text = style.description,
            fontSize = 13.sp,
            color = ColorSlateMuted,
            lineHeight = 18.sp
        )
    }
}

@Composable
internal fun CountdownRow(
    countdownSeconds: Long,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "⏳", fontSize = 24.sp)
        Text(
            text = buildAnnotatedString {
                append("Inicia en ")
                withStyle(SpanStyle(color = color, fontFamily = FontFamily.Monospace)) {
                    append(CountdownFormatter.format(countdownSeconds))
                }
            },
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = ColorSlateDark
        )
    }
}
