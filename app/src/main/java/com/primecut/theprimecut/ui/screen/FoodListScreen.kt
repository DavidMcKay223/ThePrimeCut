package com.primecut.theprimecut.ui.screen

import androidx.compose.foundation.BorderStroke
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
import com.primecut.theprimecut.ui.component.FoodItemCard
import com.primecut.theprimecut.ui.component.ResponsiveInputRow
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

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Search Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Search Library",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Recipe Name Search
                        OutlinedTextField(
                            value = nameQuery,
                            onValueChange = { viewModel.onNameQueryChanged(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Search by Recipe Name...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Name") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )

                        // Row for Brand and Group Search
                        ResponsiveInputRow(
                            content1 = { modifier ->
                                OutlinedTextField(
                                    value = brandQuery,
                                    onValueChange = { viewModel.onBrandQueryChanged(it) },
                                    modifier = modifier,
                                    placeholder = { Text("Brand...") },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                    )
                                )
                            },
                            content2 = { modifier ->
                                OutlinedTextField(
                                    value = groupQuery,
                                    onValueChange = { viewModel.onGroupQueryChanged(it) },
                                    modifier = modifier,
                                    placeholder = { Text("Group...") },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Filters
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
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

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
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
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        item {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
        
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Food List
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
