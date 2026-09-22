package com.protas.time_deepseek.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ============================================================================
// Colores Semánticos del Sistema de Diseño (Material 3 Dark/Light Contrast)
// ============================================================================

internal val ColorPeakCrimson = Color(0xFFDC2626)
internal val ColorPeakContainer = Color(0xFFFEE2E2)
internal val ColorPeakText = Color(0xFF991B1B)
internal val ColorPeakBorder = Color(0xFFFECDD3)
internal val ColorPeakLabel = Color(0xFFB45309)

internal val ColorOffPeakEmerald = Color(0xFF10B981)
internal val ColorOffPeakContainer = Color(0xFFD1FAE5)
internal val ColorOffPeakText = Color(0xFF065F46)
internal val ColorOffPeakBorder = Color(0xFFA7F3D0)
internal val ColorOffPeakLabel = Color(0xFF047857)

internal val ColorSkyPrimary = Color(0xFF0284C7)
internal val ColorSkyContainer = Color(0xFFE0F2FE)
internal val ColorSkyBorder = Color(0xFFBAE6FD)
internal val ColorSkyText = Color(0xFF0369A1)

internal val ColorAmberContainer = Color(0xFFFEF3C7)
internal val ColorAmberText = Color(0xFF92400E)

internal val ColorSlateDark = Color(0xFF0F172A)
internal val ColorSlateMuted = Color(0xFF64748B)
internal val ColorSlateLight = Color(0xFFF8FAFC)
internal val ColorBorder = Color(0xFFE2E8F0)

// ============================================================================
// Shapes Estáticos Cacheados
// ============================================================================

internal val CardShape = RoundedCornerShape(24.dp)
internal val ContainerShape = RoundedCornerShape(16.dp)
internal val BadgeShape = RoundedCornerShape(12.dp)
internal val ChipShape = RoundedCornerShape(20.dp)
internal val TagShape = RoundedCornerShape(6.dp)

// ============================================================================
// Bordes Cacheados (Cero alocaciones en runtime de Compose)
// ============================================================================

internal val DefaultCardBorder = BorderStroke(1.dp, ColorBorder)
internal val PeakCardBorder = BorderStroke(1.dp, ColorPeakBorder)
internal val OffPeakCardBorder = BorderStroke(1.dp, ColorOffPeakBorder)
internal val PeakBadgeBorder = BorderStroke(1.dp, ColorPeakCrimson.copy(alpha = 0.3f))
internal val OffPeakBadgeBorder = BorderStroke(1.dp, ColorOffPeakEmerald.copy(alpha = 0.3f))
internal val SkyIconBorder = BorderStroke(1.dp, ColorSkyBorder)

// ============================================================================
// Modificador de Inset Cacheado (Elimina Boxes intermedios en el árbol de Compose)
// ============================================================================

internal val InsetModifier = Modifier
    .fillMaxWidth()
    .background(ColorSlateLight, ContainerShape)
    .border(DefaultCardBorder, ContainerShape)
    .padding(12.dp)

// ============================================================================
// Primitiva de Contenedor AppCard
// ============================================================================

@Composable
internal fun AppCard(
    modifier: Modifier = Modifier,
    border: BorderStroke = DefaultCardBorder,
    padding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = border
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}
