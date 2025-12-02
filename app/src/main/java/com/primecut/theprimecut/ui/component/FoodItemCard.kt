package com.primecut.theprimecut.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.primecut.theprimecut.data.model.FoodItem
import com.primecut.theprimecut.ui.theme.OffWhite
import com.primecut.theprimecut.ui.theme.SlateGray
import com.primecut.theprimecut.ui.theme.VividBlue

@Composable
fun FoodItemCard(
    foodItem: FoodItem
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = SlateGray
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Name and Calorie Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = foodItem.recipeName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = VividBlue,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    val subtitle = listOfNotNull(
                        foodItem.brandType.takeIf { it.isNotEmpty() },
                        foodItem.groupName?.takeIf { it.isNotEmpty() }
                    ).joinToString(" • ")

                    if (subtitle.isNotEmpty()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelMedium,
                            color = OffWhite.copy(alpha = 0.7f)
                        )
                    }
                }
                
                Surface(
                    color = VividBlue.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${foodItem.caloriesPerServing.toInt()} kcal",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = VividBlue
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Badges/Tags
            val tags = remember(foodItem) {
                buildList {
                    if (foodItem.isHighProtein) add("High Protein")
                    if (foodItem.isKeto) add("Keto")
                    if (foodItem.isLowCarb) add("Low Carb")
                    if (foodItem.isBalancedMeal) add("Balanced")
                    if (foodItem.isBulkMeal) add("Bulk")
                    if (foodItem.isLowFiber) add("Low Fiber")
                }
            }

            if (tags.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(tags) { tag ->
                        Surface(
                            color = OffWhite.copy(alpha = 0.1f),
                            shape = CircleShape
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall,
                                color = OffWhite,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            HorizontalDivider(color = OffWhite.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(12.dp))

            // Macros Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MacroItem(label = "Protein", value = "${foodItem.protein}g")
                MacroItem(label = "Carbs", value = "${foodItem.carbs}g")
                MacroItem(label = "Fats", value = "${foodItem.fats}g")
                MacroItem(label = "Fiber", value = "${foodItem.fiber}g")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Serving Info footer
            Text(
                text = "${foodItem.servings} servings • ${foodItem.measurementServings} ${foodItem.measurementType}",
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
