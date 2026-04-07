package com.primecut.theprimecut.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.primecut.theprimecut.PrimeCutApplication
import com.primecut.theprimecut.ui.viewmodels.ViewModelFactory
import com.primecut.theprimecut.ui.component.DateSelector
import com.primecut.theprimecut.ui.component.ResponsiveInputRow
import com.primecut.theprimecut.ui.viewmodels.WeightLogViewModel
import com.primecut.theprimecut.util.AppSession

import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.PathEffect
import kotlin.math.max
import kotlin.math.min

@Composable
fun WeightTrendGraph(
    logs: List<com.primecut.theprimecut.data.model.WeightLog>,
    modifier: Modifier = Modifier
) {
    val sortedLogs = logs.sortedBy { it.date }
    val weights = sortedLogs.map { it.weightLbs }
    val minWeight = weights.minOrNull() ?: 0f
    val maxWeight = weights.maxOrNull() ?: 100f
    
    // Calculate padding for the graph so it doesn't hit the absolute edges
    val range = maxWeight - minWeight
    val displayMin = minWeight - (range * 0.1f)
    val displayMax = maxWeight + (range * 0.1f)
    val displayRange = (displayMax - displayMin).let { if (it <= 0f) 1f else it }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val spacing = width / (weights.size - 1).coerceAtLeast(1)

        val points = weights.mapIndexed { index, weight ->
            val x = index * spacing
            val normalizedWeight = (weight - displayMin) / displayRange
            // Inverse Y since 0 is top
            val y = height - (normalizedWeight * height)
            androidx.compose.ui.geometry.Offset(x, y)
        }

        val path = Path().apply {
            if (points.isNotEmpty()) {
                moveTo(points[0].x, points[0].y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
            }
        }

        drawPath(
            path = path,
            color = Color(0xFF2E7D32),
            style = Stroke(
                width = 3.dp.toPx(),
                pathEffect = PathEffect.cornerPathEffect(20f)
            )
        )
    }
}

@Composable
fun ProfileScreen(
    viewModel: WeightLogViewModel = viewModel(
        factory = ViewModelFactory((LocalContext.current.applicationContext as PrimeCutApplication).container)
    )
) {
    val context = LocalContext.current
    val logs by viewModel.logs.collectAsState()

    var dateInput by remember { mutableStateOf("") }
    var weightInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Text(
                text = "The Prime Identity",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Managing the elite engine that is you",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Profile Header Card
            item {
                Card(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                            
                            Column {
                                Text(
                                    text = AppSession.userName,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Elite Cut Connoisseur",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (logs.size >= 2) {
                            WeightTrendGraph(
                                logs = logs,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .padding(horizontal = 24.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Weight Input Section
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Recalibrate the Machine",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Card(
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            ResponsiveInputRow(
                                content1 = { modifier ->
                                    DateSelector(
                                        selectedDate = dateInput,
                                        onDateSelected = { dateInput = it },
                                        modifier = modifier
                                    )
                                },
                                content2 = { modifier ->
                                    OutlinedTextField(
                                        value = weightInput,
                                        onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() || c == '.' }) weightInput = it },
                                        label = { Text("Weight (lbs)") },
                                        modifier = modifier,
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            )

                            Button(
                                onClick = {
                                    val weight = weightInput.toFloatOrNull()
                                    if (dateInput.isBlank() || weight == null) {
                                        Toast.makeText(context, "Precision requires data. Enter date and weight.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.addOrUpdateLog(AppSession.userName, dateInput, weight)
                                        dateInput = ""
                                        weightInput = ""
                                        Toast.makeText(context, "Metrics updated!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Commit to the Log", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // History Header
            item {
                Text(
                    text = "Historical Performance",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            if (logs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "The annals are empty. Start recording your legend.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                items(logs) { log ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = log.date,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "${log.weightLbs} lbs",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
