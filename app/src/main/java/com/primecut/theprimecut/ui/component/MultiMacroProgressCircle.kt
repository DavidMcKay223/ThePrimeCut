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

import com.primecut.theprimecut.ui.theme.*

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
        MacroData("Calories", caloriesCurrent, caloriesGoal, macroPurple), 
        MacroData("Protein", proteinCurrent, proteinGoal, macroPink), 
        MacroData("Carbs", carbsCurrent, carbsGoal, macroBlue), 
        MacroData("Fat", fatCurrent, fatGoal, macroAmber), 
        MacroData("Fiber", fiberCurrent, fiberGoal, macroGreen)
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

        // Center text is now handled by the caller to allow better customization
    }
}

data class MacroData(
    val name: String,
    val current: Float,
    val goal: Float,
    val color: Color
)
