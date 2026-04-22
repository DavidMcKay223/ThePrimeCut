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
fun MultiMacroProgressCircle(
    caloriesCurrent: Float,
    caloriesGoal: Float,
    proteinCurrent: Float,
    proteinGoal: Float,
    carbsCurrent: Float,
    carbsGoal: Float,
    fatCurrent: Float,
    fatGoal: Float,
    fiberCurrent: Float,
    fiberGoal: Float,
    modifier: Modifier = Modifier,
    size: Dp = 220.dp,
    strokeWidth: Dp = 10.dp,
    spacing: Dp = 6.dp
) {
    val macros = listOf(
        MacroData("Calories", caloriesCurrent, caloriesGoal, Color(0xFF6750A4)), // Primary
        MacroData("Protein", proteinCurrent, proteinGoal, Color(0xFFE91E63)), // Pink
        MacroData("Carbs", carbsCurrent, carbsGoal, Color(0xFF03A9F4)), // Blue
        MacroData("Fat", fatCurrent, fatGoal, Color(0xFFFFC107)), // Amber
        MacroData("Fiber", fiberCurrent, fiberGoal, Color(0xFF4CAF50)) // Green
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            macros.forEachIndexed { index, macro ->
                val radius = (size.toPx() / 2) - (index * (strokeWidth.toPx() + spacing.toPx())) - (strokeWidth.toPx() / 2)
                if (radius > 0) {
                    val progress = if (macro.goal > 0) (macro.current / macro.goal).coerceIn(0f, 1f) else 0f
                    
                    // Track
                    drawCircle(
                        color = macro.color.copy(alpha = 0.15f),
                        radius = radius,
                        style = Stroke(width = strokeWidth.toPx())
                    )

                    // Progress Arc
                    drawArc(
                        color = macro.color,
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        topLeft = center.copy(x = center.x - radius, y = center.y - radius),
                        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
        }

        // Center Text (Mainly for Calories)
        val remaining = caloriesGoal - caloriesCurrent
        val isOverLimit = caloriesCurrent > caloriesGoal
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = abs(remaining).toInt().toString(),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 36.sp
                ),
                color = if (isOverLimit) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (isOverLimit) "Over kcal" else "Left kcal",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

data class MacroData(
    val name: String,
    val current: Float,
    val goal: Float,
    val color: Color
)
