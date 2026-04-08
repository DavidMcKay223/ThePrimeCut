package com.primecut.theprimecut.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.primecut.theprimecut.PrimeCutApplication
import com.primecut.theprimecut.data.model.FoodItem
import com.primecut.theprimecut.data.model.MealEntry
import com.primecut.theprimecut.data.model.UserProfile
import com.primecut.theprimecut.ui.viewmodels.FoodItemViewModel
import com.primecut.theprimecut.ui.viewmodels.MealEntryViewModel
import com.primecut.theprimecut.ui.viewmodels.UserProfileViewModel
import com.primecut.theprimecut.ui.viewmodels.ViewModelFactory
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen(
    mealEntryViewModel: MealEntryViewModel = viewModel(
        factory = ViewModelFactory((LocalContext.current.applicationContext as PrimeCutApplication).container)
    ),
    userProfileViewModel: UserProfileViewModel = viewModel(
        factory = ViewModelFactory((LocalContext.current.applicationContext as PrimeCutApplication).container)
    ),
    foodItemViewModel: FoodItemViewModel = viewModel(
        factory = ViewModelFactory((LocalContext.current.applicationContext as PrimeCutApplication).container)
    )
) {
    val allProfiles by userProfileViewModel.allProfiles.collectAsState()
    val allUsersEntries by mealEntryViewModel.allUsersEntries.collectAsState()
    val allFoodItems by foodItemViewModel.foodItems.collectAsState()
    
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }

    LaunchedEffect(Unit) {
        foodItemViewModel.clearFilters()
    }
    
    LaunchedEffect(currentMonth, allProfiles) {
        if (allProfiles.isNotEmpty()) {
            val start = currentMonth.atDay(1).toString()
            val end = currentMonth.atEndOfMonth().toString()
            mealEntryViewModel.loadAllUsersEntriesRange(start, end, allProfiles.map { it.userName })
        } else {
            userProfileViewModel.refreshAllProfiles()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Performance Overview", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                CalendarView(
                    currentMonth = currentMonth,
                    selectedDate = selectedDate,
                    profiles = allProfiles,
                    allEntries = allUsersEntries,
                    onMonthChange = { currentMonth = it },
                    onDateSelect = { selectedDate = it }
                )
            }

            if (selectedDate != null) {
                val dayEntries = allUsersEntries.values.flatten().filter { it.date == selectedDate.toString() }
                
                item {
                    DailyDetailSection(
                        date = selectedDate!!,
                        profiles = allProfiles,
                        allEntries = allUsersEntries
                    )
                }

                if (dayEntries.isNotEmpty()) {
                    item {
                        HallOfFameSection(
                            title = "Daily Performance Ranking",
                            entries = dayEntries,
                            foodItems = allFoodItems
                        )
                    }
                }
            }
            
            item {
                MonthlyTeamStats(currentMonth, allProfiles, allUsersEntries)
            }

            val monthEntries = allUsersEntries.values.flatten()
            if (monthEntries.isNotEmpty()) {
                item {
                    HallOfFameSection(
                        title = "Monthly Hall of Fame",
                        entries = monthEntries,
                        foodItems = allFoodItems
                    )
                }
            }

            item {
                TrendingFoodsSection(allUsersEntries)
            }
        }
    }
}

@Composable
fun CalendarView(
    currentMonth: YearMonth,
    selectedDate: LocalDate?,
    profiles: List<UserProfile>,
    allEntries: Map<String, List<MealEntry>>,
    onMonthChange: (YearMonth) -> Unit,
    onDateSelect: (LocalDate) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onMonthChange(currentMonth.minusMonths(1)) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous")
                }
                Text(
                    text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { onMonthChange(currentMonth.plusMonths(1)) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
                }
            }

            Spacer(Modifier.height(16.dp))

            val daysInMonth = currentMonth.lengthOfMonth()
            val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value % 7 // 0 for Sunday
            
            Row(modifier = Modifier.fillMaxWidth()) {
                val weekDays = listOf("S", "M", "T", "W", "T", "F", "S")
                weekDays.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            var currentDay = 1
            for (week in 0..5) {
                if (currentDay > daysInMonth) break
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    for (dayOfWeek in 0..6) {
                        if ((week == 0 && dayOfWeek < firstDayOfWeek) || currentDay > daysInMonth) {
                            Box(modifier = Modifier.weight(1f))
                        } else {
                            val date = currentMonth.atDay(currentDay)
                            DayCell(
                                date = date,
                                isSelected = date == selectedDate,
                                modifier = Modifier.weight(1f),
                                profiles = profiles,
                                allEntries = allEntries,
                                onClick = { onDateSelect(date) }
                            )
                            currentDay++
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DayCell(
    date: LocalDate,
    isSelected: Boolean,
    profiles: List<UserProfile>,
    allEntries: Map<String, List<MealEntry>>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(Modifier.height(4.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            profiles.take(4).forEach { profile ->
                val userEntries = allEntries[profile.userName]?.filter { it.date == date.toString() } ?: emptyList()
                val totalCals = userEntries.sumOf { it.calories.toDouble() }.toFloat()
                
                if (userEntries.isNotEmpty()) {
                    val color = when {
                        totalCals > profile.calorieGoal + 50 -> MaterialTheme.colorScheme.error
                        totalCals < profile.calorieGoal - 150 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.primary
                    }
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                } else {
                    Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)))
                }
            }
        }
    }
}

@Composable
fun DailyDetailSection(date: LocalDate, profiles: List<UserProfile>, allEntries: Map<String, List<MealEntry>>) {
    var viewMode by remember { mutableStateOf("Daily") }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (viewMode == "Daily") date.format(DateTimeFormatter.ofPattern("EEEE, MMM d")) else "Weekly Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                // Weekly Toggle
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(2.dp)
                ) {
                    listOf("Daily", "Weekly").forEach { mode ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (viewMode == mode) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { viewMode = mode }
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = mode,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (viewMode == mode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (viewMode == mode) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            profiles.forEach { profile ->
                val entries = if (viewMode == "Daily") {
                    allEntries[profile.userName]?.filter { it.date == date.toString() } ?: emptyList()
                } else {
                    val startOfWeek = date.minusDays(date.dayOfWeek.value.toLong() % 7)
                    val weekDates = (0..6).map { startOfWeek.plusDays(it.toLong()).toString() }
                    allEntries[profile.userName]?.filter { it.date in weekDates } ?: emptyList()
                }

                val totalCals = entries.sumOf { it.calories.toDouble() }.toFloat()
                val avgCals = if (viewMode == "Weekly") totalCals / 7 else totalCals
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                        Spacer(Modifier.width(8.dp))
                        Text(profile.userName, style = MaterialTheme.typography.bodyMedium)
                    }
                    
                    if (entries.isNotEmpty()) {
                        val target = profile.calorieGoal
                        val color = when {
                            avgCals > target + 50 -> MaterialTheme.colorScheme.error
                            avgCals < target - 150 -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.primary
                        }
                        
                        Text(
                            text = "${avgCals.roundToInt()} / ${target.roundToInt()} kcal${if(viewMode == "Weekly") " (avg)" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    } else {
                        Text("No logs", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }
    }
}


@Composable
fun MonthlyTeamStats(month: YearMonth, profiles: List<UserProfile>, allEntries: Map<String, List<MealEntry>>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text("Team Discipline (${month.month.name.lowercase().replaceFirstChar { it.uppercase() }})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        profiles.forEach { profile ->
            val userEntriesByDay = allEntries[profile.userName]?.groupBy { it.date } ?: emptyMap()
            val daysOnTrack = userEntriesByDay.count { (_, entries) ->
                val total = entries.sumOf { it.calories.toDouble() }
                total >= profile.calorieGoal - 200 && total <= profile.calorieGoal + 100
            }
            
            val progress = if (month.monthValue == LocalDate.now().monthValue) LocalDate.now().dayOfMonth else month.lengthOfMonth()
            val percentage = (daysOnTrack.toFloat() / progress.toFloat()).coerceIn(0f, 1f)

            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(profile.userName, style = MaterialTheme.typography.labelMedium)
                    Text("$daysOnTrack days on target", style = MaterialTheme.typography.labelSmall)
                }
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { percentage },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
fun HallOfFameSection(title: String, entries: List<MealEntry>, foodItems: List<FoodItem>) {
    // Objective Ranking using the static FoodItem table data
    val consumedFoodNames = entries.map { it.mealName.lowercase().trim() }.distinct()
    
    val rankedItems = foodItems.filter { 
        it.recipeName.lowercase().trim() in consumedFoodNames 
    }.sortedByDescending { 
        it.protein + it.fiber 
    }.take(10)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (rankedItems.isEmpty()) {
                    Text("No food items identified in the database", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(8.dp))
                }

                rankedItems.forEachIndexed { index, item ->
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "#${index + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = item.recipeName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                        
                        Text(
                            text = "${item.totalCalories.roundToInt()} kcal | P: ${item.protein.roundToInt()}g | C: ${item.carbs.roundToInt()}g | F: ${item.fats.roundToInt()}g | Fib: ${item.fiber.roundToInt()}g",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 28.dp)
                        )
                        
                        Text(
                            text = "Standard Serving: ${item.measurementServings.roundToInt()} ${item.measurementType}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(start = 28.dp)
                        )
                    }
                    if (index < rankedItems.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 28.dp, top = 4.dp, bottom = 4.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TrendingFoodsSection(allEntries: Map<String, List<MealEntry>>) {
    val allFoodNames = allEntries.values.flatten().map { it.mealName }
    val topFoods = allFoodNames.groupBy { it }
        .mapValues { it.value.size }
        .toList()
        .sortedByDescending { it.second }
        .take(3)

    if (topFoods.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Most Popular Items", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            topFoods.forEach { (name, count) ->
                ListItem(
                    headlineContent = { Text(name, fontWeight = FontWeight.Medium) },
                    supportingContent = { Text("Logged $count times this month") },
                    trailingContent = {
                        Text("#${topFoods.indexOf(Pair(name, count)) + 1}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }
        }
    }
}
