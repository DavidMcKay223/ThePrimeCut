package com.primecut.theprimecut.ui.screen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.primecut.theprimecut.PrimeCutApplication
import com.primecut.theprimecut.ui.viewmodels.ViewModelFactory
import com.primecut.theprimecut.data.model.FoodItem
import com.primecut.theprimecut.data.model.MealEntry
import com.primecut.theprimecut.ui.viewmodels.MealEntryViewModel
import com.primecut.theprimecut.ui.viewmodels.FoodItemViewModel
import java.time.LocalDate
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
    mealEntryViewModel: MealEntryViewModel = viewModel(
        factory = ViewModelFactory((LocalContext.current.applicationContext as PrimeCutApplication).container)
    ),
    foodItemViewModel: FoodItemViewModel = viewModel(
        factory = ViewModelFactory((LocalContext.current.applicationContext as PrimeCutApplication).container)
    )
) {
    val context = LocalContext.current

    val mealEntries by mealEntryViewModel.mealEntries.collectAsState()
    val foodItems by foodItemViewModel.foodItems.collectAsState()

    var selectedDate by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()))
    }
    var mealType by remember { mutableStateOf(suggestDefaultMealType()) }
    var foodName by remember { mutableStateOf("") }
    var portion by remember { mutableFloatStateOf(1f) }

    var foodExpanded by remember { mutableStateOf(false) }
    var filteredFoodItems by remember { mutableStateOf(foodItems.map { it.recipeName }) }
    var showFoodSearchSheet by remember { mutableStateOf(false) }

    val todayEntries = remember(mealEntries, selectedDate) {
        mealEntries.filter { it.date == selectedDate }
    }

    val currentFoodItem by remember(foodName, foodItems) {
        derivedStateOf {
            foodItems.find { it.recipeName.equals(foodName, ignoreCase = true) }
        }
    }

    val totalCalories = remember(todayEntries) { todayEntries.sumOf { it.calories.toInt() } }
    val totalProtein = remember(todayEntries) { todayEntries.sumOf { it.protein.toInt() } }
    val totalFiber = remember(todayEntries) { todayEntries.sumOf { it.fiber.toInt() } }
    val totalCarbs = remember(todayEntries) { todayEntries.sumOf { it.carbs.toInt() } }
    val totalFats = remember(todayEntries) { todayEntries.sumOf { it.fats.toInt() } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
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

                        OutlinedButton(
                            onClick = { showFoodSearchSheet = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text(
                                text = foodName.ifBlank { "Select Food Item" },
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        if (showFoodSearchSheet) {
                            ModalBottomSheet(
                                onDismissRequest = { showFoodSearchSheet = false }
                            ) {
                                FoodSearchSheetContent(
                                    foodItems = foodItems,
                                    onFoodSelected = { selectedFood ->
                                        foodName = selectedFood.recipeName
                                        portion = selectedFood.servings
                                        showFoodSearchSheet = false
                                    }
                                )
                            }
                        }

                        NutritionPortionSlider(
                            foodItem = currentFoodItem,
                            portion = portion,
                            onPortionChange = { portion = it },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                val portionVal = portion
                                val foodItem: FoodItem? = foodItems.find { it.recipeName == foodName }
                                if (foodItem != null) {
                                    val entry = MealEntry(
                                        date = selectedDate,
                                        day = LocalDate.parse(selectedDate).dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() },
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
                                    portion = 1f
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

        item {
            Text(
                text = "Daily Summary",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        if (todayEntries.isEmpty()) {
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

            val groupedEntries = todayEntries.groupBy { it.mealType }
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

            val otherEntries = todayEntries.filter { it.mealType !in mealOrder }
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

@Composable
fun NutritionPortionSlider(
    foodItem: FoodItem?,
    portion: Float,
    onPortionChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    if (foodItem == null) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            )
        ) {
            Text(
                text = "Select a food item above to adjust portion and preview nutrition",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val calories = foodItem.caloriesPerServing * portion
    val protein = foodItem.protein * portion
    val carbs = foodItem.carbs * portion
    val fats = foodItem.fats * portion
    val fiber = foodItem.fiber * portion

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Portion",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "${portion.toOneDecimal()} x ${foodItem.measurementServings} ${foodItem.measurementType}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Slider(
                value = portion,
                onValueChange = onPortionChange,
                valueRange = 0.25f..10f,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Live macro preview
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                NutritionPreviewItem("Cal", "${calories.toInt()}")
                NutritionPreviewItem("Protein", "${protein.toOneDecimal()}g")
                NutritionPreviewItem("Carbs", "${carbs.toOneDecimal()}g")
                NutritionPreviewItem("Fat", "${fats.toOneDecimal()}g")
                NutritionPreviewItem("Fiber", "${fiber.toOneDecimal()}g")
            }
        }
    }
}

@Composable
private fun NutritionPreviewItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun Float.toOneDecimal(): String = "%.1f".format(this)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FoodSearchSheetContent(
    foodItems: List<FoodItem>,
    onFoodSelected: (FoodItem) -> Unit
) {
    var searchText by remember { mutableStateOf("") }

    val filteredItems by remember(foodItems, searchText) {
        derivedStateOf {
            if (searchText.isBlank()) foodItems
            else foodItems.filter {
                it.recipeName.contains(searchText, ignoreCase = true)
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text(
            text = "Search Food",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            label = { Text("Food name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp)
        ) {
            items(filteredItems) { food ->
                ListItem(
                    headlineContent = { Text(food.recipeName) },
                    supportingContent = {
                        Text("${food.caloriesPerServing.toInt()} cal • ${food.servings} servings")
                    },
                    modifier = Modifier.clickable { onFoodSelected(food) }
                )
                HorizontalDivider()
            }

            if (filteredItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No matching food found")
                    }
                }
            }
        }
    }
}
