package com.primecut.theprimecut.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.primecut.theprimecut.ui.view.MacroProgressView

@Composable
fun HomeScreen() {
    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text("Today Macro's:")
        MacroProgressView()
    }
}
