package com.primecut.theprimecut.ui.screen

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.primecut.theprimecut.ui.component.FoodItemCard
import com.primecut.theprimecut.ui.viewmodels.FoodItemViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.primecut.theprimecut.data.model.FoodItem

@Composable
fun FoodListScreen(
    viewModel: FoodItemViewModel = hiltViewModel()
) {
    val foodItems: List<FoodItem> by viewModel.foodItems.collectAsState()

    LazyColumn {
        items(foodItems) { item ->
            FoodItemCard(
                foodItem = item
            )
        }
    }
}
