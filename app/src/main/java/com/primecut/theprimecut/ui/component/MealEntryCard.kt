package com.primecut.theprimecut.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.primecut.theprimecut.data.model.MealEntry
import com.primecut.theprimecut.ui.theme.OffWhite
import com.primecut.theprimecut.ui.theme.SlateGray
import com.primecut.theprimecut.ui.theme.VividBlue

@Composable
fun MealEntryCard(
    mealEntryItem: MealEntry,
    onDelete: (MealEntry) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = SlateGray
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Name, Calories, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = mealEntryItem.mealName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = VividBlue,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    val subtitle = listOfNotNull(
                        mealEntryItem.mealType,
                        mealEntryItem.groupName?.takeIf { it.isNotEmpty() }
                    ).joinToString(" • ")

                    if (subtitle.isNotEmpty()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelMedium,
                            color = OffWhite.copy(alpha = 0.7f)
                        )
                    }
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = VividBlue.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${mealEntryItem.calories.toInt()} kcal",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = VividBlue
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    
                    IconButton(
                        onClick = { onDelete(mealEntryItem) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = OffWhite.copy(alpha = 0.1f)
            )

            // Macros Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MacroItem(label = "Protein", value = "${mealEntryItem.protein.toInt()}g")
                MacroItem(label = "Carbs", value = "${mealEntryItem.carbs.toInt()}g")
                MacroItem(label = "Fats", value = "${mealEntryItem.fats.toInt()}g")
                MacroItem(label = "Fiber", value = "${mealEntryItem.fiber.toInt()}g")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Serving Info footer
            Text(
                text = "${mealEntryItem.portionEaten} portion(s) • ${mealEntryItem.measurementServings} ${mealEntryItem.measurementType}",
                style = MaterialTheme.typography.bodySmall,
                color = OffWhite.copy(alpha = 0.5f),
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
private fun MacroItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = OffWhite
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = OffWhite.copy(alpha = 0.7f)
        )
    }
}
