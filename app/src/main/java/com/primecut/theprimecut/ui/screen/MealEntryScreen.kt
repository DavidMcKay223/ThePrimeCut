package com.primecut.theprimecut.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.primecut.theprimecut.data.model.FoodItem
import com.primecut.theprimecut.data.model.MealEntry
import com.primecut.theprimecut.ui.viewmodels.MealEntryViewModel
import com.primecut.theprimecut.ui.viewmodels.FoodItemViewModel
import java.text.SimpleDateFormat
import java.util.*
import com.primecut.theprimecut.ui.component.MealEntryCard
import com.primecut.theprimecut.ui.component.DateSelector
import com.primecut.theprimecut.ui.component.DropdownSelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealEntryScreen(
    mealEntryViewModel: MealEntryViewModel = hiltViewModel(),
    foodItemViewModel: FoodItemViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val mealEntries by mealEntryViewModel.mealEntries.collectAsState()
    val foodItems by foodItemViewModel.foodItems.collectAsState()

    val calendar = remember { Calendar.getInstance() }
    var selectedDate by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()))
    }
    var mealType by remember { mutableStateOf(suggestDefaultMealType()) }
    var foodName by remember { mutableStateOf("") }
    var portion by remember { mutableStateOf("1") }

    var foodExpanded by remember { mutableStateOf(false) }
    var filteredFoodItems by remember { mutableStateOf(foodItems.map { it.recipeName }) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Input Section
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Log a Meal",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = MaterialTheme.shapes.medium,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            DateSelector(selectedDate = selectedDate) { selectedDate = it }
                        }

                        DropdownSelector(
                            label = "Meal Type",
                            selected = mealType,
                            options = listOf("Breakfast", "Lunch", "Dinner", "Snack"),
                            onSelected = { mealType = it },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = foodExpanded,
                            onExpandedChange = { foodExpanded = !foodExpanded },
                            modifier = Modifier.weight(1.5f)
                        ) {
                            OutlinedTextField(
                                value = foodName,
                                onValueChange = {
                                    foodName = it
                                    filteredFoodItems = if (it.isBlank()) {
                                        foodItems.map { item -> item.recipeName }
                                    } else {
                                        foodItems.map { item -> item.recipeName }
                                            .filter { name -> name.contains(it, ignoreCase = true) }
                                    }
                                    foodExpanded = true
                                },
                                label = { Text("Food Item") },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                )
                            )

                            ExposedDropdownMenu(
                                expanded = foodExpanded && filteredFoodItems.isNotEmpty(),
                                onDismissRequest = { foodExpanded = false }
                            ) {
                                filteredFoodItems.forEach { itemName ->
                                    DropdownMenuItem(
                                        text = { Text(itemName) },
                                        onClick = {
                                            foodName = itemName
                                            // auto-fill portion if match
                                            val item = foodItems.find { it.recipeName == itemName }
                                            if (item != null) portion = item.servings.toString()
                                            foodExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = portion,
                            onValueChange = { portion = it },
                            label = { Text("Portion") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }

                    Button(
                        onClick = {
                            val portionVal = portion.toFloatOrNull() ?: 1f
                            val foodItem: FoodItem? = foodItems.find { it.recipeName == foodName }
                            if (foodItem != null) {
                                val entry = MealEntry(
                                    date = selectedDate,
                                    day = SimpleDateFormat("EEEE", Locale.US)
                                        .format(SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(selectedDate)!!),
                                    mealType = mealType,
                                    mealName = foodItem.recipeName,
                                    groupName = foodItem.groupName,
                                    portionEaten = portionVal,
                                    measurementServings = foodItem.measurementServings,
                                    measurementType = foodItem.measurementType,
                                    calories = foodItem.caloriesPerServing * portionVal,
                                    protein = foodItem.protein * portionVal,
                                    carbs = foodItem.carbs * portionVal,
                                    fats = foodItem.fats * portionVal,
                                    fiber = foodItem.fiber * portionVal
                                )
                                mealEntryViewModel.addMealEntry(entry)
                                foodName = ""
                                portion = "1"
                                Toast.makeText(context, "Meal added!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Food item not found", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Add Meal")
                    }
                }
            }
        }

        // List Section
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Today's Entries",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            val todaysEntries = mealEntries.filter { it.date == selectedDate }

            if (todaysEntries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No meals logged for this date.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(todaysEntries) { entry ->
                        MealEntryCard(
                            mealEntryItem = entry,
                            onDelete = { mealToDelete ->
                                mealEntryViewModel.deleteMealEntry(mealToDelete)
                            }
                        )
                    }
                }
            }
        }
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
