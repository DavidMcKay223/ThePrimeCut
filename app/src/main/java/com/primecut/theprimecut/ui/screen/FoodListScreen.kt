package com.primecut.theprimecut.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.primecut.theprimecut.PrimeCutApplication
import com.primecut.theprimecut.data.model.MealEntry
import com.primecut.theprimecut.ui.component.DropdownSelector
import com.primecut.theprimecut.ui.component.FoodItemCard
import com.primecut.theprimecut.ui.component.ResponsiveInputRow
import com.primecut.theprimecut.ui.viewmodels.FoodItemViewModel
import com.primecut.theprimecut.ui.viewmodels.MacroViewModel
import com.primecut.theprimecut.ui.viewmodels.MealEntryViewModel
import com.primecut.theprimecut.ui.viewmodels.UserProfileViewModel
import com.primecut.theprimecut.ui.viewmodels.ViewModelFactory
import com.primecut.theprimecut.util.AppSession
import java.time.LocalDate
import java.util.Calendar

@Composable
fun FoodListScreen(
    viewModel: FoodItemViewModel = viewModel(
        factory = ViewModelFactory((LocalContext.current.applicationContext as PrimeCutApplication).container)
    ),
    macroViewModel: MacroViewModel = viewModel(
        factory = ViewModelFactory((LocalContext.current.applicationContext as PrimeCutApplication).container)
    ),
    userProfileViewModel: UserProfileViewModel = viewModel(
        factory = ViewModelFactory((LocalContext.current.applicationContext as PrimeCutApplication).container)
    ),
    mealEntryViewModel: MealEntryViewModel = viewModel(
        factory = ViewModelFactory((LocalContext.current.applicationContext as PrimeCutApplication).container)
    )
) {
    val foodItems by viewModel.foodItems.collectAsState()
    val nameQuery by viewModel.nameQuery.collectAsState()
    val brandQuery by viewModel.brandQuery.collectAsState()
    val groupQuery by viewModel.groupQuery.collectAsState()
    val selectedFilters by viewModel.selectedFilters.collectAsState()
    val brands by viewModel.brands.collectAsState()
    val groups by viewModel.groups.collectAsState()
    
    val profile by userProfileViewModel.userProfile.collectAsState()
    val summary by macroViewModel.summary.collectAsState()
    
    var showFilters by remember { mutableStateOf(false) }

    val remainingCalories = if (profile != null) {
        profile!!.calorieGoal - summary.calories
    } else null

    val mealFilters = listOf("Breakfast", "Lunch", "Dinner", "Snack")
    val macroFilters = listOf("High Protein", "Low Carb", "Keto", "Bulk", "Low Fiber", "Balanced", "High Fat")

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Header Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Food Library",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${foodItems.size} items available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { showFilters = !showFilters },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (showFilters || selectedFilters.isNotEmpty() || brandQuery.isNotEmpty() || groupQuery.isNotEmpty()) 
                            MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Filters",
                        tint = if (showFilters || selectedFilters.isNotEmpty()) 
                            MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Integrated Search Bar
            OutlinedTextField(
                value = nameQuery,
                onValueChange = { viewModel.onNameQueryChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search meals, recipes, brands...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                trailingIcon = {
                    if (nameQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onNameQueryChanged("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            AnimatedVisibility(
                visible = showFilters,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Brand and Group Dropdowns
                    ResponsiveInputRow(
                        content1 = { modifier ->
                            val brandOptions = listOf("All Brands") + brands
                            DropdownSelector(
                                label = "Brand",
                                selected = if (brandQuery.isEmpty()) "All Brands" else brandQuery,
                                options = brandOptions,
                                onSelected = { 
                                    viewModel.onBrandQueryChanged(if (it == "All Brands") "" else it) 
                                },
                                modifier = modifier,
                                shape = RoundedCornerShape(12.dp)
                            )
                        },
                        content2 = { modifier ->
                            val groupOptions = listOf("All Groups") + groups
                            DropdownSelector(
                                label = "Group",
                                selected = if (groupQuery.isEmpty()) "All Groups" else groupQuery,
                                options = groupOptions,
                                onSelected = { 
                                    viewModel.onGroupQueryChanged(if (it == "All Groups") "" else it) 
                                },
                                modifier = modifier,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    )

                    // Meal Type Chips
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Meal Type", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(mealFilters) { filter ->
                                FilterChip(
                                    selected = selectedFilters.contains(filter),
                                    onClick = { viewModel.toggleFilter(filter) },
                                    label = { Text(filter) },
                                    shape = CircleShape
                                )
                            }
                        }
                    }

                    // Nutritional Goals Chips
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Nutritional Profile", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(macroFilters) { filter ->
                                FilterChip(
                                    selected = selectedFilters.contains(filter),
                                    onClick = { viewModel.toggleFilter(filter) },
                                    label = { Text(filter) },
                                    shape = CircleShape
                                )
                            }
                        }
                    }
                    
                    if (selectedFilters.isNotEmpty() || brandQuery.isNotEmpty() || groupQuery.isNotEmpty()) {
                        TextButton(
                            onClick = { 
                                viewModel.onBrandQueryChanged("")
                                viewModel.onGroupQueryChanged("")
                                // viewModel doesn't have a clearFilters but we can toggle them off or add a clear function
                                selectedFilters.forEach { viewModel.toggleFilter(it) }
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Reset Filters", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        // Food List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            items(foodItems, key = { it.recipeName + it.brandType }) { item ->
                FoodItemCard(
                    foodItem = item,
                    remainingCalories = remainingCalories,
                    onLogClick = { food, multiplier ->
                        val today = LocalDate.now().toString()
                        val suggestedMealType = suggestDefaultMealType()
                        val entry = MealEntry(
                            date = today,
                            day = LocalDate.now().dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() },
                            mealType = suggestedMealType,
                            mealName = food.recipeName,
                            groupName = food.groupName,
                            portionEaten = multiplier,
                            measurementServings = food.measurementServings * multiplier,
                            measurementType = food.measurementType,
                            calories = food.caloriesPerServing * multiplier,
                            protein = food.protein * multiplier,
                            carbs = food.carbs * multiplier,
                            fats = food.fats * multiplier,
                            fiber = food.fiber * multiplier
                        )
                        mealEntryViewModel.addMealEntry(entry) {
                            macroViewModel.loadSummary()
                        }
                    }
                )
            }
            
            if (foodItems.isEmpty()) {
                item {
                    EmptySearchState()
                }
            }
        }
    }
}

@Composable
private fun EmptySearchState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 64.dp, start = 32.dp, end = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.outlineVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No culinary matches found",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Try adjusting your filters or search terms.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}


private fun suggestDefaultMealType(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 6..10 -> "Breakfast"
        in 11..15 -> "Lunch"
        in 16..19 -> "Dinner"
        else -> "Snack"
    }
}
