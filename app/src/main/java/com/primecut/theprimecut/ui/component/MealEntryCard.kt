package com.primecut.theprimecut.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.primecut.theprimecut.data.model.MealEntry
import com.primecut.theprimecut.ui.theme.DarkCharcoal
import com.primecut.theprimecut.ui.theme.OffWhite
import com.primecut.theprimecut.ui.theme.VividBlue

@Composable
fun MealEntryCard(
    mealEntryItem: MealEntry,
    onDelete: (MealEntry) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkCharcoal
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = mealEntryItem.mealName,
                        style = MaterialTheme.typography.titleMedium,
                        color = VividBlue
                    )
                    IconButton(
                        onClick = { onDelete(mealEntryItem) },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${mealEntryItem.portionEaten} Servings - (${mealEntryItem.measurementServings} x ${mealEntryItem.measurementType})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OffWhite
                    )
                    Text(
                        text = "${mealEntryItem.calories} cal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OffWhite
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Protein:\n${mealEntryItem.protein} g",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OffWhite
                    )
                    Text(
                        text = "Carbs:\n${mealEntryItem.carbs} g",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OffWhite
                    )
                    Text(
                        text = "Fats:\n${mealEntryItem.fats} g",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OffWhite
                    )
                    Text(
                        text = "Fiber:\n${mealEntryItem.fiber} g",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OffWhite
                    )
                }
            }
        }
    }
}
