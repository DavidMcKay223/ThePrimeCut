package com.primecut.theprimecut.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.primecut.theprimecut.data.model.FoodItem
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun FoodItemCard(
    foodItem: FoodItem,
    remainingCalories: Float? = null,
    onLogClick: (FoodItem, Float) -> Unit = { _, _ -> }
) {
    var expanded by remember { mutableStateOf(false) }
    var servingMultiplier by remember { mutableFloatStateOf(1f) }
    
    val calories = foodItem.caloriesPerServing * servingMultiplier
    val protein = foodItem.protein * servingMultiplier
    val carbs = foodItem.carbs * servingMultiplier
    val fats = foodItem.fats * servingMultiplier

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Visual Header Section (Styled Box with Icon and Color)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    )
            ) {
                // Large Background Icon for "Picture Book" feel without internet
                Icon(
                    imageVector = getIconForGroup(foodItem.groupName),
                    contentDescription = null,
                    modifier = Modifier
                        .size(120.dp)
                        .align(Alignment.CenterEnd)
                        .offset(x = 20.dp, y = 10.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
                )

                // Calorie Badge
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Text(
                        text = "${calories.roundToInt()} kcal",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Food Name on Header
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                ) {
                    Text(
                        text = foodItem.recipeName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Info & Small Macros Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${foodItem.brandType} • ${foodItem.measurementServings.roundToInt()} ${foodItem.measurementType}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                         MacroSmallLabel("P", "${protein.roundToInt()}g")
                         MacroSmallLabel("C", "${carbs.roundToInt()}g")
                         MacroSmallLabel("F", "${fats.roundToInt()}g")
                    }
                }

                AnimatedVisibility(
                    visible = expanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        HorizontalDivider(
                            modifier = Modifier.padding(bottom = 12.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Adjust Serving",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = String.format(Locale.US, "%.2f x", servingMultiplier),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Slider(
                            value = servingMultiplier,
                            onValueChange = { servingMultiplier = it },
                            valueRange = 0.25f..4f,
                            steps = 14,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        Button(
                            onClick = { 
                                onLogClick(foodItem, servingMultiplier) 
                                expanded = false
                            },
                            modifier = Modifier.fillMaxWidth().height(40.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Add to Diary", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MacroSmallLabel(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun getIconForGroup(group: String?): ImageVector {
    return when (group?.lowercase()) {
        "fruits & vegetables" -> Icons.Default.Restaurant
        "meat", "poultry", "beef" -> Icons.Default.Restaurant
        "bakery", "sweets" -> Icons.Default.Fastfood
        "nutrition", "beverages" -> Icons.Default.Fastfood
        else -> Icons.Default.Restaurant
    }
}
