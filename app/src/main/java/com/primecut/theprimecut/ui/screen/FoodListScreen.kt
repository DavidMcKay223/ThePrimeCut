package com.primecut.theprimecut.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.primecut.theprimecut.data.model.FoodItem
import com.primecut.theprimecut.ui.component.FoodItemCard
import com.primecut.theprimecut.ui.viewmodels.FoodItemViewModel

@Composable
fun FoodListScreen(
    viewModel: FoodItemViewModel = hiltViewModel()
) {
    val foodItems by viewModel.foodItems.collectAsState()
    val nameQuery by viewModel.nameQuery.collectAsState()
    val brandQuery by viewModel.brandQuery.collectAsState()
    val groupQuery by viewModel.groupQuery.collectAsState()
    val selectedFilters by viewModel.selectedFilters.collectAsState()

    val mealFilters = listOf(
        "Breakfast", "Lunch", "Dinner", "Snack"
    )
    
    val macroFilters = listOf(
        "High Protein", "Low Carb", "Keto", "Bulk", 
        "Low Fiber", "Balanced", "High Fat"
    )

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Search Fields Column
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Recipe Name Search
            OutlinedTextField(
                value = nameQuery,
                onValueChange = { viewModel.onNameQueryChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search by Recipe Name...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Name") },
                singleLine = true,
                shape = MaterialTheme.shapes.small,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            // Row for Brand and Group Search
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = brandQuery,
                    onValueChange = { viewModel.onBrandQueryChanged(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Brand...") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.small,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                OutlinedTextField(
                    value = groupQuery,
                    onValueChange = { viewModel.onGroupQueryChanged(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Group...") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.small,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        }

        // Meal Filters
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(mealFilters) { filter ->
                FilterChip(
                    selected = selectedFilters.contains(filter),
                    onClick = { viewModel.toggleFilter(filter) },
                    label = { Text(filter) }
                )
            }
        }

        // Macro/Type Filters
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(macroFilters) { filter ->
                FilterChip(
                    selected = selectedFilters.contains(filter),
                    onClick = { viewModel.toggleFilter(filter) },
                    label = { Text(filter) }
                )
            }
        }

        // Food List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(foodItems) { item ->
                FoodItemCard(
                    foodItem = item
                )
            }
            
            if (foodItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Text(
                            text = "No items found matching your search.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
