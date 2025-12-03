package com.primecut.theprimecut.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

@Composable
fun ResponsiveInputRow(
    modifier: Modifier = Modifier,
    content1: @Composable (Modifier) -> Unit,
    content2: @Composable (Modifier) -> Unit
) {
    val configuration = LocalConfiguration.current
    // Breakpoint for switching from Row to Column. 
    // 480dp covers most phones in portrait mode.
    val isCompact = configuration.screenWidthDp < 480

    if (isCompact) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            content1(Modifier.fillMaxWidth())
            content2(Modifier.fillMaxWidth())
        }
    } else {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            content1(Modifier.weight(1f))
            content2(Modifier.weight(1f))
        }
    }
}
