package com.primecut.theprimecut.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.primecut.theprimecut.data.model.FoodItem
import com.primecut.theprimecut.ui.theme.DarkCharcoal
import com.primecut.theprimecut.ui.theme.OffWhite
import com.primecut.theprimecut.ui.theme.VividBlue

@Composable
fun FoodItemCard(
    foodItem: FoodItem
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
                Text(
                    text = foodItem.recipeName,
                    style = MaterialTheme.typography.titleMedium,
                    color = VividBlue
                )
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = "${foodItem.servings} Servings - (${foodItem.measurementServings} x ${foodItem.measurementType})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OffWhite
                    )
                    Text(
                        text = "${foodItem.caloriesPerServing} cal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OffWhite
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween){
                    Text(
                        text = "Protein:\n${foodItem.protein} g",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OffWhite
                    )
                    Text(
                        text = "Carbs:\n${foodItem.carbs} g",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OffWhite
                    )
                    Text(
                        text = "Fats:\n${foodItem.fats} g",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OffWhite
                    )
                    Text(
                        text = "Fiber:\n${foodItem.fiber} g",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OffWhite
                    )
                }
            }
        }
    }
}
