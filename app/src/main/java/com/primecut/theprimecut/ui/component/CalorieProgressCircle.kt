package com.primecut.theprimecut.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

@Composable
fun CalorieProgressCircle(
    current: Float,
    goal: Float,
    hasMacroOverflow: Boolean = false,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    strokeWidth: Dp = 16.dp
) {
    val safeGoal = if (goal > 0f) goal else 1f
    val progressValue = (current / safeGoal).coerceIn(0f, 1f)
    val remaining = goal - current
    val isOverLimit = current > goal

    val animatedProgress by animateFloatAsState(
        targetValue = progressValue,
        animationSpec = tween(durationMillis = 1000),
        label = "CalorieProgressAnimation"
    )

    val progressColor = when {
        isOverLimit -> MaterialTheme.colorScheme.error
        hasMacroOverflow -> Color(0xFFFFA726) // Warning Orange
        else -> MaterialTheme.colorScheme.tertiary
    }
    val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Background Track
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )

            // Progress Arc
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }

        // Center Text Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (remaining >= 0) remaining.toInt().toString() else abs(remaining).toInt().toString(),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 42.sp
                ),
                color = if (isOverLimit) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
            )
            Text(
                text = if (isOverLimit) "Prime Overflow" else "Remaining Fuel",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "kcal",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
