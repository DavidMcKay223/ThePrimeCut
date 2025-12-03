package com.primecut.theprimecut.ui.screen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import com.primecut.theprimecut.ui.component.ResponsiveInputRow
import com.primecut.theprimecut.ui.theme.OffWhite
import com.primecut.theprimecut.ui.theme.VividBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealEntryScreen(
    mealEntryViewModel: MealEntryViewModel = hiltViewModel(),
    foodItemViewModel: FoodItemViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val mealEntries by mealEntryViewModel.mealEntries.collectAsState()
    val foodItems by foodItemViewModel.foodItems.collectAsState()

    var selectedDate by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()))
    }
    var mealType by remember { mutableStateOf(suggestDefaultMealType()) }
    var foodName by remember { mutableStateOf("") }
    var portion by remember { mutableStateOf("1") }

    var foodExpanded by remember { mutableStateOf(false) }
    var filteredFoodItems by remember { mutableStateOf(foodItems.map { it.recipeName }) }

    // Derived state for daily totals
    val todaysEntries = remember(mealEntries, selectedDate) {
        mealEntries.filter { it.date == selectedDate }
    }
    
    val totalCalories = remember(todaysEntries) { todaysEntries.sumOf { it.calories.toInt() } }
    val totalProtein = remember(todaysEntries) { todaysEntries.sumOf { it.protein.toInt() } }
    val totalFiber = remember(todaysEntries) { todaysEntries.sumOf { it.fiber.toInt() } }
    val totalCarbs = remember(todaysEntries) { todaysEntries.sumOf { it.carbs.toInt() } }
    val totalFats = remember(todaysEntries) { todaysEntries.sumOf { it.fats.toInt() } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Input Section
        item {
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
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ResponsiveInputRow(
                            content1 = { modifier ->
                                DateSelector(
                                    selectedDate = selectedDate,
                                    onDateSelected = { selectedDate = it },
                                    modifier = modifier
                                )
                            },
                            content2 = { modifier ->
                                DropdownSelector(
                                    label = "Meal Type",
                                    selected = mealType,
                                    options = listOf("Breakfast", "Lunch", "Dinner", "Snack"),
                                    onSelected = { mealType = it },
                                    modifier = modifier,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                    )
                                )
                            }
                        )

                        ResponsiveInputRow(
                            content1 = { modifier ->
                                ExposedDropdownMenuBox(
                                    expanded = foodExpanded,
                                    onExpandedChange = { foodExpanded = !foodExpanded },
                                    modifier = modifier
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
                                        onDismissRequest = { foodExpanded = false },
                                        modifier = Modifier.exposedDropdownSize()
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
                            },
                            content2 = { modifier ->
                                OutlinedTextField(
                                    value = portion,
                                    onValueChange = { portion = it },
                                    label = { Text("Portion") },
                                    modifier = modifier,
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                    )
                                )
                            }
                        )

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
        }

        item {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }

        // List & Summary Section
        item {
            Text(
                text = "Daily Summary",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        if (todaysEntries.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No meals logged for this date.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // Totals Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = VividBlue),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Calories", style = MaterialTheme.typography.labelMedium, color = OffWhite.copy(0.8f))
                            Text("$totalCalories", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = OffWhite)
                        }
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            SummaryMacro("Protein", "${totalProtein}g")
                            SummaryMacro("Carbs", "${totalCarbs}g")
                            SummaryMacro("Fats", "${totalFats}g")
                            SummaryMacro("Fiber", "${totalFiber}g")
                        }
                    }
                }
            }

            // Grouped Meal List
            val groupedEntries = todaysEntries.groupBy { it.mealType }
            val mealOrder = listOf("Breakfast", "Lunch", "Dinner", "Snack")
            
            mealOrder.forEach { type ->
                val entries = groupedEntries[type]
                if (!entries.isNullOrEmpty()) {
                    item {
                        Text(
                            text = type,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                    }
                    items(entries) { entry ->
                        MealEntryCard(
                            mealEntryItem = entry,
                            onDelete = { mealToDelete ->
                                mealEntryViewModel.deleteMealEntry(mealToDelete)
                            }
                        )
                    }
                }
            }
            
            val otherEntries = todaysEntries.filter { it.mealType !in mealOrder }
            if (otherEntries.isNotEmpty()) {
                item {
                     Text(
                        text = "Other",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }
                items(otherEntries) { entry ->
                    MealEntryCard(
                        mealEntryItem = entry,
                        onDelete = { mealEntryViewModel.deleteMealEntry(it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryMacro(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = OffWhite)
        Text(label, style = MaterialTheme.typography.labelSmall, color = OffWhite.copy(0.7f))
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
