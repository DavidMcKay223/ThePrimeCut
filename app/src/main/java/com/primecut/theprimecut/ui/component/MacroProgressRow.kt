package com.primecut.theprimecut.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.primecut.theprimecut.ui.theme.VividBlue
import kotlin.math.abs

@Composable
fun MacroProgressRow(
    name: String,
    current: Float,
    goal: Float,
    format: String,
) {
    val safeGoal = if (goal > 0f) goal else 1f
    val progressValue = (current / safeGoal).coerceIn(0f, 1f)
    val isOverLimit = current > goal
    
    // Animate progress
    val animatedProgress by animateFloatAsState(
        targetValue = progressValue,
        animationSpec = tween(durationMillis = 1000),
        label = "ProgressAnimation"
    )

    // Determine colors
    val progressColor = when {
        isOverLimit -> MaterialTheme.colorScheme.error
        progressValue >= 0.9f -> Color(0xFFFFA726) // Warning/Close color
        else -> VividBlue
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Formatted text: "Current / Goal"
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(
                        fontWeight = FontWeight.Bold, 
                        color = if (isOverLimit) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )) {
                        append(String.format("%.1f%s", current, format))
                    }
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                        append(" / ${String.format("%.0f%s", goal, format)}")
                    }
                },
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(50)), // Fully rounded
            color = progressColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Status / Info Text
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val status = getStatusText(current, goal, format)
            Text(
                text = status,
                style = MaterialTheme.typography.labelSmall,
                color = if (isOverLimit) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Percentage
            val percentage = (current / safeGoal * 100).toInt()
             Text(
                text = "$percentage%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun getStatusText(current: Float, goal: Float, format: String): String {
    val remaining = goal - current
    return when {
        remaining < 0 -> "Over by %.1f%s".format(abs(remaining), format)
        current / (if (goal > 0) goal else 1f) >= 0.9f -> "Almost there!"
        else -> "${String.format("%.1f", remaining)}$format left"
    }
}
