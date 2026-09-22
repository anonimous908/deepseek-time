package com.protas.time_deepseek.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val TimelineBorderStroke = Stroke(width = 2f)
private val TimelineCornerRadius = CornerRadius(12f, 12f)
private val ColorTrackBackground = Color(0xFFE2E8F0)
private val ColorTrackBorder = Color(0xFFCBD5E1)
private val ColorPeakSegment = Color(0xFFFECDD3)
private val ColorOffPeakSegment = Color(0xFFD1FAE5)

private val TimelineHourLabels = listOf("00h", "04h", "06h", "10h", "14h", "18h", "22h", "24h")

private data class ScheduleSegment(val startFraction: Float, val widthFraction: Float, val color: Color)
private val TimelineSegments = listOf(
    ScheduleSegment(0f / 24f, 1f / 24f, ColorOffPeakSegment),
    ScheduleSegment(1f / 24f, 3f / 24f, ColorPeakSegment),
    ScheduleSegment(4f / 24f, 2f / 24f, ColorOffPeakSegment),
    ScheduleSegment(6f / 24f, 4f / 24f, ColorPeakSegment),
    ScheduleSegment(10f / 24f, 14f / 24f, ColorOffPeakSegment)
)

@Composable
internal fun TimelineCard(
    progressProvider: () -> Float,
    modifier: Modifier = Modifier
) {
    AppCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = "📅", fontSize = 16.sp)
            Text(
                text = "Línea de Tiempo UTC (24 Horas)",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = ColorSlateDark
            )
        }

        // Leyenda
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TimelineLegendItem(ColorPeakCrimson, "Tarifa Peak (100%)")
            TimelineLegendItem(ColorOffPeakEmerald, "Off-Peak (50% Desc.)")
        }

        // Scrubber visualizador 24h directo sobre Column (sin Box intermedio superfluo)
        Column(
            modifier = InsetModifier,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
            ) {
                drawRoundRect(ColorTrackBackground, cornerRadius = TimelineCornerRadius)
                for (seg in TimelineSegments) {
                    drawRect(
                        color = seg.color,
                        topLeft = Offset(size.width * seg.startFraction, 0f),
                        size = Size(size.width * seg.widthFraction, size.height)
                    )
                }
                drawRoundRect(ColorTrackBorder, cornerRadius = TimelineCornerRadius, style = TimelineBorderStroke)

                // Lectura diferida en fase de dibujo: redibuja aguja directamente en GPU sin recomponer
                val needleX = progressProvider().coerceIn(0f, 1f) * size.width
                drawLine(ColorSkyPrimary, Offset(needleX, 0f), Offset(needleX, size.height), strokeWidth = 4f)
                drawCircle(ColorSkyPrimary, radius = 5f, center = Offset(needleX, 4f))
                drawCircle(ColorSkyPrimary, radius = 5f, center = Offset(needleX, size.height - 4f))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TimelineHourLabels.forEach { hour ->
                    Text(
                        text = hour,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = ColorSlateMuted
                    )
                }
            }
        }

        Text(
            text = "ℹ Las franjas pico corresponden a las horas de mayor demanda global de inferencia en DeepSeek.",
            fontSize = 11.sp,
            color = ColorSlateMuted,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun TimelineLegendItem(color: Color, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Spacer(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Text(text = text, fontSize = 11.sp, color = ColorSlateMuted)
    }
}
