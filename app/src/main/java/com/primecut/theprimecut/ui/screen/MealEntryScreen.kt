package com.primecut.theprimecut.ui.screen

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
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

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Text(
                text = "Log Meal",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Track your intake for today",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Summary Card
            item {
                SummaryCard(
                    calories = totalCalories,
                    protein = totalProtein,
                    carbs = totalCarbs,
                    fats = totalFats,
                    fiber = totalFiber
                )
            }

            // Entry Form
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "New Entry",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        // Date Display/Selector Trigger
                        Surface(
                            onClick = { /* Could open a date picker here if DateSelector doesn't handle it */ },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text(selectedDate, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }

                    Card(
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            DropdownSelector(
                                label = "Meal Type",
                                selected = mealType,
                                options = listOf("Breakfast", "Lunch", "Dinner", "Snack"),
                                onSelected = { mealType = it },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Button(
                                onClick = { showFoodSearchSheet = true },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                elevation = null
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            text = foodName.ifBlank { "Select Food Item" },
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = if (foodName.isEmpty()) FontWeight.Normal else FontWeight.Bold
                                        )
                                    }
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                                }
                            }

                            if (showFoodSearchSheet) {
                                ModalBottomSheet(
                                    onDismissRequest = { showFoodSearchSheet = false },
                                    dragHandle = { BottomSheetDefaults.DragHandle() },
                                    containerColor = MaterialTheme.colorScheme.surface
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

                            AnimatedVisibility(visible = currentFoodItem != null) {
                                NutritionPortionSlider(
                                    foodItem = currentFoodItem,
                                    portion = portion,
                                    onPortionChange = { portion = it },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Button(
                                onClick = {
                                    val foodItem = currentFoodItem
                                    if (foodItem != null) {
                                        val entry = MealEntry(
                                            date = selectedDate,
                                            day = LocalDate.parse(selectedDate).dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() },
                                            mealType = mealType,
                                            mealName = foodItem.recipeName,
                                            groupName = foodItem.groupName,
                                            portionEaten = portion,
                                            measurementServings = foodItem.measurementServings,
                                            measurementType = foodItem.measurementType,
                                            calories = foodItem.caloriesPerServing * portion,
                                            protein = foodItem.protein * portion,
                                            carbs = foodItem.carbs * portion,
                                            fats = foodItem.fats * portion,
                                            fiber = foodItem.fiber * portion
                                        )
                                        mealEntryViewModel.addMealEntry(entry)
                                        foodName = ""
                                        portion = 1f
                                        Toast.makeText(context, "Meal Logged", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(12.dp),
                                enabled = currentFoodItem != null,
                                colors = ButtonDefaults.buttonColors(containerColor = VividBlue)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Log to Diary", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Entries List
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "Today's History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (todayEntries.isEmpty()) {
                        EmptyHistoryState()
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
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                    items(entries) { entry ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                            MealEntryCard(
                                mealEntryItem = entry,
                                onDelete = { mealEntryViewModel.deleteMealEntry(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    calories: Int,
    protein: Int,
    carbs: Int,
    fats: Int,
    fiber: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = VividBlue),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Calories", style = MaterialTheme.typography.labelLarge, color = OffWhite.copy(0.7f))
                    Text(
                        "$calories",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 36.sp
                        ),
                        color = OffWhite
                    )
                }
                Icon(
                    Icons.Default.History,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = OffWhite.copy(alpha = 0.2f)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryMacro("Protein", "${protein}g")
                SummaryMacro("Carbs", "${carbs}g")
                SummaryMacro("Fats", "${fats}g")
                SummaryMacro("Fiber", "${fiber}g")
            }
        }
    }
}

@Composable
private fun EmptyHistoryState() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Restaurant, contentDescription = null, tint = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(12.dp))
            Text(
                "No meals logged yet",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
