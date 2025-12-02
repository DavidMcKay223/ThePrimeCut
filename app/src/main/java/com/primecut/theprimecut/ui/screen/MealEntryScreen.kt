package com.primecut.theprimecut.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
            .padding(16.dp)
    ) {
        Column {
            Row(Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                DateSelector(selectedDate = selectedDate){ selectedDate = it }

                DropdownSelector(
                    label = "Meal Type",
                    selected = mealType,
                    options = listOf("Breakfast", "Lunch", "Dinner", "Snack"),
                    onSelected = { mealType = it },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(
                    expanded = foodExpanded,
                    onExpandedChange = { foodExpanded = !foodExpanded },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
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
                        singleLine = true
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
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

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
                } else {
                    Toast.makeText(context, "Food item not found", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Meal")
        }

        Spacer(Modifier.height(24.dp))

        if (mealEntries.isEmpty()) {
            Text("No meals found for this date", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn {
                items(mealEntries.filter { it.date == selectedDate }) { entry ->
                    MealEntryCard(mealEntryItem = entry,
                        onDelete = { mealToDelete ->
                            mealEntryViewModel.deleteMealEntry(mealToDelete)
                        })
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
