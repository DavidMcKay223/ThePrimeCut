package com.primecut.theprimecut.ui.screen

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.primecut.theprimecut.PrimeCutApplication
import com.primecut.theprimecut.data.model.FoodItem
import com.primecut.theprimecut.data.model.MealEntry
import com.primecut.theprimecut.data.model.UserProfile
import com.primecut.theprimecut.ui.component.DropdownSelector
import com.primecut.theprimecut.ui.component.InlineFoodSearch
import com.primecut.theprimecut.ui.component.ResponsiveInputRow
import com.primecut.theprimecut.ui.viewmodels.FoodItemViewModel
import com.primecut.theprimecut.ui.viewmodels.MealEntryViewModel
import com.primecut.theprimecut.ui.viewmodels.UserProfileViewModel
import com.primecut.theprimecut.ui.viewmodels.ViewModelFactory
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

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
    val allProfiles by userProfileViewModel.allProfiles.collectAsState()

    val nameQuery by foodItemViewModel.nameQuery.collectAsState()
    val brandQuery by foodItemViewModel.brandQuery.collectAsState()
    val groupQuery by foodItemViewModel.groupQuery.collectAsState()
    val selectedFilters by foodItemViewModel.selectedFilters.collectAsState()
    val brands by foodItemViewModel.brands.collectAsState()
    val groups by foodItemViewModel.groups.collectAsState()

    var selectedDate by remember {
        mutableStateOf(LocalDate.now().toString())
    }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showCopyDayDialog by remember { mutableStateOf(false) }
    
    if (showCopyDayDialog) {
        CopyDayDialog(
            allProfiles = allProfiles,
            onDismiss = { showCopyDayDialog = false },
            onConfirm = { sourceUser, sourceDate ->
                mealEntryViewModel.copyEntries(sourceUser, sourceDate, selectedDate) {
                    Toast.makeText(context, "Entries copied successfully", Toast.LENGTH_SHORT).show()
                }
                showCopyDayDialog = false
            }
        )
    }
    
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = LocalDate.parse(selectedDate).atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.of("UTC"))
                            .toLocalDate()
                            .toString()
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    LaunchedEffect(Unit) {
        foodItemViewModel.clearFilters()
    }

    LaunchedEffect(userProfileViewModel) {
        userProfileViewModel.onUserSwitched = {
            mealEntryViewModel.refreshMealEntries(selectedDate)
        }
    }

    LaunchedEffect(com.primecut.theprimecut.util.AppSession.userName, selectedDate) {
        userProfileViewModel.loadProfile(com.primecut.theprimecut.util.AppSession.userName)
        mealEntryViewModel.refreshMealEntries(selectedDate)
    }

    var activeMealType by remember { mutableStateOf<String?>(null) }


    var showFoodSearchSheet by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<MealEntry?>(null) }

    val todayEntries = mealEntries.filter { it.date == selectedDate }
    val totalCalories = todayEntries.sumOf { it.calories.toDouble() }.toInt()
    val totalProtein = todayEntries.sumOf { it.protein.toDouble() }.toInt()
    val totalCarbs = todayEntries.sumOf { it.carbs.toDouble() }.toInt()
    val totalFats = todayEntries.sumOf { it.fats.toDouble() }.toInt()
    val totalFiber = todayEntries.sumOf { it.fiber.toDouble() }.toInt()

    val calorieGoal = profile?.calorieGoal ?: 2000f

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
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
            
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    onClick = { showDatePicker = true },
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

                IconButton(
                    onClick = { showCopyDayDialog = true },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                ) {
                    Icon(
                        Icons.Default.ContentCopy, 
                        contentDescription = "Copy from another day",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                CalorieBudgetCard(
                    goal = calorieGoal.toInt(),
                    food = totalCalories,
                    remaining = (calorieGoal - totalCalories).toInt()
                )
            }

            item {
                MacroStrip(
                    protein = totalProtein,
                    carbs = totalCarbs,
                    fats = totalFats,
                    fiber = totalFiber
                )
            }

            val groupedEntries = todayEntries.groupBy { it.mealType }
            val mealOrder = listOf("Breakfast", "Lunch", "Dinner", "Snack")

            mealOrder.forEach { type ->
                item(key = type) {
                    val entries = groupedEntries[type] ?: emptyList()
                    val isSearchActive = activeMealType == type
                    
                    MealSection(
                        title = type,
                        entries = entries,
                        onAddClick = { 
                            if (activeMealType == type) {
                                activeMealType = null
                            } else {
                                activeMealType = type
                            }
                        },
                        onDeleteEntry = { mealEntryViewModel.deleteMealEntry(it) },
                        onEntryClick = { editingEntry = it },
                        inlineSearchContent = {
                            AnimatedVisibility(
                                visible = isSearchActive,
                                enter = expandVertically(),
                                exit = shrinkVertically()
                            ) {
                                InlineFoodSearch(
                                    filteredFoodItems = foodItems,
                                    recentEntries = mealEntries,
                                    brands = brands,
                                    groups = groups,
                                    nameQuery = nameQuery,
                                    onNameQueryChanged = { foodItemViewModel.onNameQueryChanged(it) },
                                    brandQuery = brandQuery,
                                    onBrandQueryChanged = { foodItemViewModel.onBrandQueryChanged(it) },
                                    groupQuery = groupQuery,
                                    onGroupQueryChanged = { foodItemViewModel.onGroupQueryChanged(it) },
                                    selectedFilters = selectedFilters,
                                    onToggleFilter = { foodItemViewModel.toggleFilter(it) },
                                    onClearAllFilters = {
                                        foodItemViewModel.onNameQueryChanged("")
                                        foodItemViewModel.onBrandQueryChanged("")
                                        foodItemViewModel.onGroupQueryChanged("")
                                        selectedFilters.forEach { foodItemViewModel.toggleFilter(it) }
                                    },
                                    onFoodSelected = { historical ->
                                        val food = historical.item
                                        val dateObj = LocalDate.parse(selectedDate)
                                        val dayOfWeek = dateObj.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
                                        
                                        val entry = MealEntry(
                                            userName = profile?.userName ?: "",
                                            date = selectedDate,
                                            day = dayOfWeek,
                                            mealType = type,
                                            mealName = food.recipeName,
                                            groupName = food.groupName,
                                            portionEaten = food.servings,
                                            measurementServings = food.measurementServings,
                                            measurementType = food.measurementType,
                                            calories = food.totalCalories,
                                            protein = food.totalProtein,
                                            carbs = food.totalCarbs,
                                            fats = food.totalFats,
                                            fiber = food.totalFiber
                                        )
                                        mealEntryViewModel.addMealEntries(listOf(entry))
                                        Toast.makeText(context, "${food.recipeName} added", Toast.LENGTH_SHORT).show()
                                        // Keep search open or close it? User said "easier", maybe keep it open for multiple adds?
                                        // But usually one add at a time is fine. Let's keep it open for "easier" multiple entries.
                                    },
                                    onClose = { activeMealType = null }
                                )
                            }
                        }
                    )
                }
            }
        }

        if (editingEntry != null) {
            ModalBottomSheet(
                onDismissRequest = { editingEntry = null },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                val entry = editingEntry!!
                val foodItem = foodItems.find { it.item.recipeName == entry.mealName }?.item
                val baseServings = foodItem?.servings ?: 1f
                var currentMultiplier by remember { mutableFloatStateOf(entry.portionEaten / baseServings) }

                Column(modifier = Modifier.padding(16.dp).padding(bottom = 32.dp)) {
                    Text(
                        text = "Edit Log: ${entry.mealType}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = entry.mealName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(Modifier.height(16.dp))

                    if (foodItem != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp), 
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                NutritionPreviewItem("Calories", (foodItem.totalCalories * currentMultiplier).toInt().toString())
                                NutritionPreviewItem("Protein", (foodItem.totalProtein * currentMultiplier).toInt().toString() + "g")
                                NutritionPreviewItem("Carbs", (foodItem.totalCarbs * currentMultiplier).toInt().toString() + "g")
                                NutritionPreviewItem("Fat", (foodItem.totalFats * currentMultiplier).toInt().toString() + "g")
                            }
                        }
                    }

                    NutritionPortionSlider(
                        foodItem = foodItem,
                        portion = currentMultiplier,
                        onPortionChange = { currentMultiplier = it },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = {
                            val updated = entry.copy(
                                portionEaten = currentMultiplier * baseServings,
                                calories = (foodItem?.caloriesPerServing ?: (entry.calories / entry.portionEaten)) * (currentMultiplier * baseServings),
                                protein = (foodItem?.protein ?: (entry.protein / entry.portionEaten)) * (currentMultiplier * baseServings),
                                carbs = (foodItem?.carbs ?: (entry.carbs / entry.portionEaten)) * (currentMultiplier * baseServings),
                                fats = (foodItem?.fats ?: (entry.fats / entry.portionEaten)) * (currentMultiplier * baseServings),
                                fiber = (foodItem?.fiber ?: (entry.fiber / entry.portionEaten)) * (currentMultiplier * baseServings)
                            )
                            mealEntryViewModel.updateMealEntry(updated)
                            editingEntry = null
                            Toast.makeText(context, "Log updated", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Update Entry", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    val allUsersEntries by mealEntryViewModel.allUsersEntries.collectAsState()

    LaunchedEffect(showFoodSearchSheet, allProfiles) {
        if (showFoodSearchSheet) {
            val end = LocalDate.now().toString()
            val start = LocalDate.now().minusDays(30).toString()
            mealEntryViewModel.loadAllUsersEntriesRange(start, end, allProfiles.map { it.userName })
        }
    }

    if (showFoodSearchSheet && activeMealType != null) {
        ModalBottomSheet(
            onDismissRequest = { 
                showFoodSearchSheet = false
                activeMealType = null
            },
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            modifier = Modifier.fillMaxHeight(0.9f)
        ) {
            AdvancedFoodSelectionSheet(
                foodItems = foodItems,
                nameQuery = nameQuery,
                brandQuery = brandQuery,
                groupQuery = groupQuery,
                selectedFilters = selectedFilters,
                brands = brands,
                groups = groups,
                allProfiles = allProfiles,
                allUsersEntries = allUsersEntries,
                activeMealType = activeMealType,
                onNameQueryChanged = { foodItemViewModel.onNameQueryChanged(it) },
                onBrandQueryChanged = { foodItemViewModel.onBrandQueryChanged(it) },
                onGroupQueryChanged = { foodItemViewModel.onGroupQueryChanged(it) },
                onToggleFilter = { foodItemViewModel.toggleFilter(it) },
                onAddEntries = { selectedEntries ->
                    val dateObj = LocalDate.parse(selectedDate)
                    val dayOfWeek = dateObj.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }

                    val entriesToLog = selectedEntries.map { 
                        it.copy(
                            mealType = activeMealType!!, 
                            date = selectedDate,
                            day = dayOfWeek
                        ) 
                    }
                    mealEntryViewModel.addMealEntries(entriesToLog)
                    showFoodSearchSheet = false
                    activeMealType = null
                    Toast.makeText(context, "${selectedEntries.size} items added", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedFoodSelectionSheet(
    foodItems: List<com.primecut.theprimecut.ui.viewmodels.FoodItemViewModel.HistoricalFoodItem>,
    nameQuery: String,
    brandQuery: String,
    groupQuery: String,
    selectedFilters: Set<String>,
    brands: List<String>,
    groups: List<String>,
    allProfiles: List<UserProfile>,
    allUsersEntries: Map<String, List<MealEntry>> = emptyMap(),
    activeMealType: String? = null,
    onNameQueryChanged: (String) -> Unit,
    onBrandQueryChanged: (String) -> Unit,
    onGroupQueryChanged: (String) -> Unit,
    onToggleFilter: (String) -> Unit,
    onAddEntries: (List<MealEntry>) -> Unit
) {
    val focusManager = LocalFocusManager.current
    var selectedPortions by remember { mutableStateOf(mapOf<String, Float>()) }
    var selectedPreviewIds by remember { mutableStateOf(setOf<Int>()) }

    val selectedItems = foodItems.filter { selectedPortions.containsKey(it.item.recipeName) }
    var showFilters by remember { mutableStateOf(false) }
    var itemToAdjust by remember { mutableStateOf<FoodItem?>(null) }

    val totalSelectedCount = selectedPortions.size + selectedPreviewIds.size

    val topEntriesByCategory = remember(allUsersEntries) {
        val allEntries = allUsersEntries.values.flatten()
        val mealTypeOrder = listOf("Breakfast", "Lunch", "Dinner", "Snack")
        
        allEntries.groupBy { it.mealType }
            .mapValues { (_, entries) ->
                entries.groupBy { it.mealName }
                    .toList()
                    .sortedByDescending { it.second.size }
                    .take(5)
                    .map { group ->
                        // Return the most recent entry of this name to preserve its macros/portion
                        group.second.first()
                    }
            }
            .toList()
            .filter { it.second.isNotEmpty() }
            .sortedBy { (type, _) -> 
                val index = mealTypeOrder.indexOf(type)
                if (index == -1) 99 else index
            }
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Add to ${activeMealType ?: "Log"}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(
                onClick = { showFilters = !showFilters },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (showFilters || selectedFilters.isNotEmpty() || brandQuery.isNotEmpty() || groupQuery.isNotEmpty()) 
                        MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.Tune, contentDescription = "Filters")
            }
        }

        OutlinedTextField(
            value = nameQuery,
            onValueChange = onNameQueryChanged,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search foods...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (nameQuery.isNotEmpty()) {
                    IconButton(onClick = { onNameQueryChanged("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear Search")
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
            shape = RoundedCornerShape(12.dp)
        )

        AnimatedVisibility(visible = showFilters) {
            Column(modifier = Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Filters", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    TextButton(onClick = {
                        onBrandQueryChanged("")
                        onGroupQueryChanged("")
                        // Clear all selected filters one by one since we don't have a clearAll method
                        selectedFilters.forEach { onToggleFilter(it) }
                    }) {
                        Text("Clear All", style = MaterialTheme.typography.labelSmall)
                    }
                }
                
                ResponsiveInputRow(
                    content1 = { modifier ->
                        DropdownSelector(
                            label = "Brand",
                            selected = brandQuery.ifEmpty { "All Brands" },
                            options = listOf("All Brands") + brands,
                            onSelected = { onBrandQueryChanged(if (it == "All Brands") "" else it) },
                            modifier = modifier,
                            shape = RoundedCornerShape(12.dp)
                        )
                    },
                    content2 = { modifier ->
                        DropdownSelector(
                            label = "Group",
                            selected = groupQuery.ifEmpty { "All Groups" },
                            options = listOf("All Groups") + groups,
                            onSelected = { onGroupQueryChanged(if (it == "All Groups") "" else it) },
                            modifier = modifier,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                )
                
                val mealFilters = listOf("Breakfast", "Lunch", "Dinner", "Snack")
                val macroFilters = listOf("High Protein", "Low Carb", "Keto", "Bulk", "Low Fiber", "Balanced", "High Fat")
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Meal Type", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(mealFilters) { filter ->
                            FilterChip(
                                selected = selectedFilters.contains(filter),
                                onClick = { onToggleFilter(filter) },
                                label = { Text(filter) }
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Nutritional Profile", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(macroFilters) { filter ->
                            FilterChip(
                                selected = selectedFilters.contains(filter),
                                onClick = { onToggleFilter(filter) },
                                label = { Text(filter) }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            val isSearching = nameQuery.isNotEmpty() || selectedFilters.isNotEmpty() || brandQuery.isNotEmpty() || groupQuery.isNotEmpty()
            
            if (foodItems.isEmpty() && isSearching) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 64.dp, horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No matching foods found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Try adjusting your filters or search terms",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            if (!isSearching && !showFilters) {
                if (topEntriesByCategory.isNotEmpty()) {
                    item {
                        Text(
                            "Frequently Logged (Last 30 Days)",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    topEntriesByCategory.forEach { (category, entries) ->
                        item {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp)
                            ) {
                                Text(
                                    category,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }
                        items(entries) { entry ->
                            val isSelected = selectedPreviewIds.contains(entry.id)
                            ListItem(
                                headlineContent = { Text(entry.mealName) },
                                supportingContent = { Text("${entry.portionEaten}x • ${entry.calories.toInt()} kcal") },
                                leadingContent = {
                                    Icon(
                                        imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                modifier = Modifier.clickable {
                                    selectedPreviewIds = if (isSelected) {
                                        selectedPreviewIds - entry.id
                                    } else {
                                        selectedPreviewIds + entry.id
                                    }
                                }
                            )
                        }
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
                item { Text("All Foods", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary) }
            }

            items(foodItems) { historical ->
                val food = historical.item
                val isHistorical = historical.isHistorical
                val isSelected = selectedPortions.containsKey(food.recipeName)
                val currentPortion = selectedPortions[food.recipeName] ?: 1.0f
                ListItem(
                    headlineContent = { 
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isHistorical) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp).padding(end = 4.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                )
                            }
                            Text(food.recipeName, fontWeight = FontWeight.Bold)
                        }
                    },
                    supportingContent = { 
                        if (isSelected) {
                            Column {
                                val portion = selectedPortions[food.recipeName] ?: 1.0f
                                Text("Portion: ${currentPortion.toTwoDecimals()}x (${food.servings.toInt()}x ${food.measurementServings.toInt()} ${food.measurementType})")
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("${(food.totalCalories * portion).toInt()} kcal", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                    Text("P: ${(food.totalProtein * portion).toInt()}g", style = MaterialTheme.typography.labelSmall)
                                    Text("C: ${(food.totalCarbs * portion).toInt()}g", style = MaterialTheme.typography.labelSmall)
                                    Text("F: ${(food.totalFats * portion).toInt()}g", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        } else {
                            Text("${food.brandType} • ${food.servings.toInt()}x ${food.measurementServings.toInt()} ${food.measurementType} • ${food.totalCalories.toInt()} kcal")
                        }
                    },
                    leadingContent = {
                        Icon(
                            imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingContent = {
                        if (isSelected) {
                            IconButton(onClick = { itemToAdjust = food }) {
                                Icon(Icons.Default.Tune, contentDescription = "Adjust Portion", tint = MaterialTheme.colorScheme.primary)
                    }
                        }
                    },
                    modifier = Modifier.clickable {
                        selectedPortions = if (isSelected) {
                            selectedPortions - food.recipeName
                        } else {
                            selectedPortions + (food.recipeName to 1.0f)
                        }
                    }
                )
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f))
            }
        }

        if (totalSelectedCount > 0) {
            Button(
                onClick = {
                    val searchEntries = selectedItems.map { historical ->
                        val food = historical.item
                        val portion = selectedPortions[food.recipeName] ?: 1.0f
                        MealEntry(
                            userName = "", 
                            date = "", 
                            day = "", 
                            mealType = "", 
                            mealName = food.recipeName,
                            groupName = food.groupName,
                            portionEaten = portion * food.servings,
                            measurementServings = food.measurementServings,
                            measurementType = food.measurementType,
                            calories = food.totalCalories * portion,
                            protein = food.totalProtein * portion,
                            carbs = food.totalCarbs * portion,
                            fats = food.totalFats * portion,
                            fiber = food.totalFiber * portion
                        )
                    }
                    val allPreselectedEntries = allUsersEntries.values.flatten()
                    val previewSelectedEntries = allPreselectedEntries
                        .filter { selectedPreviewIds.contains(it.id) }
                        .distinctBy { it.id }

                    onAddEntries(searchEntries + previewSelectedEntries)
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Add $totalSelectedCount Items")
            }
        }
    }

    if (itemToAdjust != null) {
        AlertDialog(
            onDismissRequest = { itemToAdjust = null },
            title = { Text("Adjust Portion: ${itemToAdjust?.recipeName}") },
            text = {
                val food = itemToAdjust!!
                NutritionPortionSlider(
                    foodItem = food,
                    portion = selectedPortions[food.recipeName] ?: 1.0f,
                    onPortionChange = { newPortion ->
                        selectedPortions = selectedPortions + (food.recipeName to newPortion)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = { itemToAdjust = null }) {
                    Text("Done")
                }
            }
        )
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
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
fun MacroStrip(protein: Int, carbs: Int, fats: Int, fiber: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SummaryMacro("Protein", "${protein}g")
        SummaryMacro("Carbs", "${carbs}g")
        SummaryMacro("Fats", "${fats}g")
        SummaryMacro("Fiber", "${fiber}g")
    }
}

@Composable
fun SummaryMacro(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun MealSection(
    title: String,
    entries: List<MealEntry>,
    onAddClick: () -> Unit,
    onDeleteEntry: (MealEntry) -> Unit,
    onEntryClick: (MealEntry) -> Unit,
    inlineSearchContent: @Composable (() -> Unit)? = null
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column {
                entries.forEachIndexed { index, entry ->
                    MealEntryRow(
                        entry = entry,
                        onDelete = { onDeleteEntry(entry) },
                        onClick = { onEntryClick(entry) }
                    )
                    if (index < entries.size - 1) {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    }
                }
                
                Surface(
                    onClick = onAddClick,
                    color = Color.Transparent,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Add, 
                            contentDescription = null, 
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Add Item", 
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                inlineSearchContent?.invoke()
            }
        }
    }
}

@Composable
fun MealEntryRow(
    entry: MealEntry,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable { onClick() },
        headlineContent = { Text(entry.mealName, fontWeight = FontWeight.Medium) },
        supportingContent = { 
            val sizeText = if ((entry.measurementServings ?: 0f) > 0f) {
                " • ${entry.portionEaten.toTwoDecimals()}x ${entry.measurementServings?.toInt()} ${entry.measurementType}"
            } else {
                " • ${entry.portionEaten.toTwoDecimals()} servings"
            }
            Text("${entry.calories.toInt()} kcal$sizeText") 
        },
        trailingContent = {
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
            }
        }
    )
}

@Composable
fun NutritionPortionSlider(
    foodItem: FoodItem?,
    portion: Float, // Multiplier (1.0 = whole item)
    onPortionChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val baseServings = foodItem?.servings ?: 1f
    val totalServings = portion * baseServings
    
    // Determine max range: at least 10 servings or 3x the default item size
    val maxServings = maxOf(baseServings * 3f, 10f)
    val steps = (maxServings / 0.25f).toInt() - 1

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Column {
                Text("Logged Quantity", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                if (foodItem != null) {
                    Text(
                        text = "Standard: ${foodItem.servings.toInt()}x ${foodItem.measurementServings.toInt()} ${foodItem.measurementType}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text("${totalServings.toTwoDecimals()} Servings", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        
        Slider(
            value = totalServings,
            onValueChange = { onPortionChange(it / baseServings) },
            valueRange = 0f..maxServings,
            steps = steps,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        if (foodItem != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp), 
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    NutritionPreviewItem("Calories", (foodItem.totalCalories * portion).toInt().toString())
                    NutritionPreviewItem("Protein", (foodItem.totalProtein * portion).toInt().toString() + "g")
                    NutritionPreviewItem("Carbs", (foodItem.totalCarbs * portion).toInt().toString() + "g")
                    NutritionPreviewItem("Fat", (foodItem.totalFats * portion).toInt().toString() + "g")
                }
            }
        }
    }
}

@Composable
fun CopyDayDialog(
    allProfiles: List<UserProfile>,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var sourceUser by remember { mutableStateOf(allProfiles.firstOrNull()?.userName ?: "") }
    var sourceDate by remember { mutableStateOf(LocalDate.now().toString()) }
    var showSourceDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Copy Day") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Select source to copy entries from.", style = MaterialTheme.typography.bodyMedium)
                
                DropdownSelector(
                    label = "From User",
                    options = allProfiles.map { it.userName },
                    selected = sourceUser,
                    onSelected = { sourceUser = it }
                )

                Surface(
                    onClick = { showSourceDatePicker = true },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(20.dp))
                        Text(sourceDate, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(sourceUser, sourceDate) }) {
                Text("Copy Entries")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    if (showSourceDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = LocalDate.parse(sourceDate).atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showSourceDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        sourceDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.of("UTC"))
                            .toLocalDate()
                            .toString()
                    }
                    showSourceDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSourceDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun NutritionPreviewItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
    }
}

fun Float.toTwoDecimals() = "%.2f".format(this)
