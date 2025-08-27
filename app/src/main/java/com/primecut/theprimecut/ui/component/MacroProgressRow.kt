package com.primecut.theprimecut.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.abs

@Composable
fun MacroProgressRow(
    name: String,
    current: Float,
    goal: Float,
    format: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = String.format("%.1f%s / %.1f%s", current, format, goal, format),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        val safeGoal = if (goal > 0f) goal else 1f
        val progress = (current / safeGoal).coerceIn(0f, 1f)

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .height(8.dp)
        )

        Text(
            text = getStatusText(current, goal, format),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

private fun getStatusText(current: Float, goal: Float, format: String): String {
    val remaining = goal - current
    return when {
        remaining < 0 -> "Over Limit by %.1f%s".format(abs(remaining), format)
        current / (if (goal > 0) goal else 1f) >= 0.85f -> "Getting Close"
        else -> "On Track"
    }
}
