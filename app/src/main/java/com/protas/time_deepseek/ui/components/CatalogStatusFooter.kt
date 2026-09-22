package com.protas.time_deepseek.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.protas.time_deepseek.R
import com.protas.time_deepseek.domain.CatalogStatus

@Composable
internal fun CatalogStatusFooter(
    catalogStatus: CatalogStatus,
    catalogVersion: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "USD por 1M de tokens",
            fontSize = 11.sp,
            color = ColorSlateMuted
        )
        if (catalogVersion != null) {
            Text(
                text = "Catálogo vigente: $catalogVersion",
                fontSize = 11.sp,
                color = ColorSlateMuted
            )
        }
        if (catalogStatus == CatalogStatus.UNAVAILABLE) {
            Text(
                text = stringResource(R.string.degraded_message),
                fontSize = 11.sp,
                color = ColorPeakCrimson,
                textAlign = TextAlign.Center
            )
        }
    }
}
