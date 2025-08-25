package com.primecut.theprimecut.ui.screen

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.primecut.theprimecut.ui.component.FoodItemCard
import com.primecut.theprimecut.ui.viewmodels.FoodItemViewModel

@Composable
fun FoodListScreen(
    viewModel: FoodItemViewModel = hiltViewModel()
) {
    val foodItems = viewModel.foodItems

    LazyColumn {
        items(foodItems.value) { item ->
            FoodItemCard(
                foodItem = item
            )
        }
    }
}
