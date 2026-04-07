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
import androidx.compose.material.icons.filled.Delete
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
import com.primecut.theprimecut.ui.viewmodels.UserProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealEntryScreen(
    mealEntryViewModel: MealEntryViewModel = viewModel(
        factory = ViewModelFactory((LocalContext.current.applicationContext as PrimeCutApplication).container)
    ),
    foodItemViewModel: FoodItemViewModel = viewModel(
        factory = ViewModelFactory((LocalContext.current.applicationContext as PrimeCutApplication).container)
    ),
    userProfileViewModel: UserProfileViewModel = viewModel(
        factory = ViewModelFactory((LocalContext.current.applicationContext as PrimeCutApplication).container)
    )
) {
    val context = LocalContext.current

    val mealEntries by mealEntryViewModel.mealEntries.collectAsState()
    val foodItems by foodItemViewModel.foodItems.collectAsState()
    val profile by userProfileViewModel.userProfile.collectAsState()

    LaunchedEffect(Unit) {
        userProfileViewModel.loadProfile(com.primecut.theprimecut.util.AppSession.userName)
    }

    var selectedDate by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()))
    }
    
    // UI State for the "Add Food" workflow
    var activeMealType by remember { mutableStateOf<String?>(null) }
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

    val calorieGoal = profile?.calorieGoal ?: 2000f

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Journal",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Daily fuel tracking",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Date Selector
            Surface(
                onClick = { /* Future: Date Picker */ },
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text(selectedDate, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // MyFitnessPal-style Summary Header
            item {
                CalorieBudgetCard(
                    goal = calorieGoal.toInt(),
                    food = totalCalories,
                    remaining = (calorieGoal - totalCalories).toInt()
                )
            }

            // Macro Strip
            item {
                MacroStrip(
                    protein = totalProtein,
                    carbs = totalCarbs,
                    fats = totalFats,
                    fiber = totalFiber
                )
            }

            // Meal Sections
            val groupedEntries = todayEntries.groupBy { it.mealType }
            val mealOrder = listOf("Breakfast", "Lunch", "Dinner", "Snack")

            mealOrder.forEach { type ->
                item(key = type) {
                    val entries = groupedEntries[type] ?: emptyList()
                    MealSection(
                        title = type,
                        entries = entries,
                        onAddClick = { 
                            activeMealType = type
                            showFoodSearchSheet = true 
                        },
                        onDeleteEntry = { mealEntryViewModel.deleteMealEntry(it) }
                    )
                }
            }
        }
    }

    // Add Food Workflow (Bottom Sheets)
    if (showFoodSearchSheet) {
        ModalBottomSheet(
            onDismissRequest = { 
                showFoodSearchSheet = false
                if (foodName.isEmpty()) activeMealType = null
            },
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
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

    if (foodName.isNotEmpty() && activeMealType != null) {
        ModalBottomSheet(
            onDismissRequest = { 
                foodName = ""
                activeMealType = null
            },
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(modifier = Modifier.padding(16.dp).padding(bottom = 32.dp)) {
                Text(
                    text = "Log to $activeMealType",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = foodName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(Modifier.height(16.dp))

                NutritionPortionSlider(
                    foodItem = currentFoodItem,
                    portion = portion,
                    onPortionChange = { portion = it },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        val foodItem = currentFoodItem
                        if (foodItem != null) {
                            val entry = MealEntry(
                                date = selectedDate,
                                day = LocalDate.parse(selectedDate).dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() },
                                mealType = activeMealType!!,
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
                            activeMealType = null
                            Toast.makeText(context, "Fuel Synchronized", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Confirm Intake", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
fun CalorieBudgetCard(goal: Int, food: Int, remaining: Int) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BudgetUnit("Goal", goal.toString())
            Text("-", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            BudgetUnit("Food", food.toString())
            Text("=", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            BudgetUnit(
                label = "Remaining", 
                value = remaining.toString(), 
                valueColor = if (remaining >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun BudgetUnit(label: String, value: String, valueColor: Color = Color.Unspecified) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = if (valueColor == Color.Unspecified) MaterialTheme.colorScheme.onSurface else valueColor
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MacroStrip(protein: Int, carbs: Int, fats: Int, fiber: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val macros = listOf(
            "Protein" to "${protein}g",
            "Carbs" to "${carbs}g",
            "Fat" to "${fats}g",
            "Fiber" to "${fiber}g"
        )
        macros.forEach { (label, value) ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold)
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun MealSection(
    title: String,
    entries: List<MealEntry>,
    onAddClick: () -> Unit,
    onDeleteEntry: (MealEntry) -> Unit
) {
    val totalCals = entries.sumOf { it.calories.toInt() }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "$totalCals kcal",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column {
                entries.forEach { entry ->
                    MealEntryRow(entry, onDeleteEntry)
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                }
                
                // Add Food Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAddClick() }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "ADD FOOD",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun MealEntryRow(entry: MealEntry, onDelete: (MealEntry) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.mealName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${entry.portionEaten.toOneDecimal()} x ${entry.measurementServings} ${entry.measurementType}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${entry.calories.toInt()}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.width(12.dp))
            IconButton(
                onClick = { onDelete(entry) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
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
            Icon(Icons.Default.Restaurant, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(12.dp))
            Text(
                "The log is empty. Start the engine.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


@Composable
private fun SummaryMacro(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onBackground.copy(0.7f))
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
                onValueChange = { onPortionChange(it) },
                valueRange = 0.25f..10f,
                steps = 38,
                modifier = Modifier.padding(vertical = 8.dp),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
                )
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
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
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
