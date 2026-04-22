package com.primecut.theprimecut.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.CalendarMonth
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
import androidx.compose.ui.unit.sp
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

import com.primecut.theprimecut.ui.theme.*

@Composable
fun OverviewScreen(
    currentProfile: UserProfile? = null,
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
    var isCalendarExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        foodItemViewModel.clearFilters()
    }
    
    LaunchedEffect(currentMonth, allProfiles) {
        if (allProfiles.isNotEmpty()) {
            val start = currentMonth.atDay(1).minusDays(7).toString()
            val end = currentMonth.atEndOfMonth().toString()
            mealEntryViewModel.loadAllUsersEntriesRange(start, end, allProfiles.map { it.userName })
        } else {
            userProfileViewModel.refreshAllProfiles()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
        ) {
            // 1. Team Discipline (At the very top)
            item {
                MonthlyTeamStats(currentMonth, allProfiles, allUsersEntries, currentProfile?.userName)
            }

            // 2. Daily/Weekly Section & Calendar Toggle
            if (selectedDate != null) {
                item {
                    DailyDetailSection(
                        date = selectedDate!!,
                        profiles = allProfiles,
                        allEntries = allUsersEntries,
                        activeUser = currentProfile?.userName,
                        isCalendarExpanded = isCalendarExpanded,
                        onToggleCalendar = { isCalendarExpanded = !isCalendarExpanded }
                    )
                }
            }

            // 3. Expandable Calendar View
            item {
                AnimatedVisibility(
                    visible = isCalendarExpanded,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    CalendarView(
                        currentMonth = currentMonth,
                        selectedDate = selectedDate,
                        profiles = allProfiles,
                        allEntries = allUsersEntries,
                        onMonthChange = { currentMonth = it },
                        onDateSelect = { 
                            selectedDate = it
                            isCalendarExpanded = false // Close after selection for a natural flow
                        }
                    )
                }
            }
            
            // 4. Team Insights (Popularity & Performance)
            item {
                UnifiedInsightsSection(
                    selectedDate = selectedDate,
                    currentMonth = currentMonth,
                    allEntries = allUsersEntries,
                    foodItems = allFoodItems
                )
            }
        }
    }
}

@Composable
fun UnifiedInsightsSection(
    selectedDate: LocalDate?,
    currentMonth: YearMonth,
    allEntries: Map<String, List<MealEntry>>,
    foodItems: List<FoodItem>
) {
    var insightMode by remember { mutableStateOf("Daily") }
    
    val displayEntries = remember(insightMode, selectedDate, currentMonth, allEntries) {
        if (insightMode == "Daily" && selectedDate != null) {
            allEntries.values.flatten().filter { it.date == selectedDate.toString() }
        } else {
            allEntries.values.flatten().filter { it.date.startsWith(currentMonth.toString()) }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Team Insights", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            // Insight Toggle
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(2.dp)
            ) {
                listOf("Daily", "Monthly").forEach { mode ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (insightMode == mode) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { insightMode = mode }
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = mode,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (insightMode == mode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (insightMode == mode) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        if (displayEntries.isNotEmpty()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.weight(1.2f)) {
                    HallOfFameCard(
                        title = if (insightMode == "Daily") "Top Rated (Today)" else "Top Rated (Month)",
                        entries = displayEntries,
                        foodItems = foodItems
                    )
                }
                Box(modifier = Modifier.weight(0.8f)) {
                    TrendingFoodsCard(displayEntries)
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
            ) {
                Text(
                    "No logs recorded for this period",
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun HallOfFameCard(title: String, entries: List<MealEntry>, foodItems: List<FoodItem>) {
    val consumedFoodNames = entries.map { it.mealName.lowercase().trim() }.distinct()
    
    val rankedItems = foodItems.filter { 
        it.recipeName.lowercase().trim() in consumedFoodNames 
    }.sortedByDescending { 
        it.protein + it.fiber 
    }.take(5)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            
            if (rankedItems.isEmpty()) {
                Text("Log foods to see rankings", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }

            rankedItems.forEachIndexed { index, item ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("#${index + 1}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(20.dp))
                    Column {
                        Text(item.recipeName, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, maxLines = 1)
                        Text("${item.totalCalories.roundToInt()} kcal", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                        Text("${item.protein.roundToInt()}g P | ${item.carbs.roundToInt()}g C | ${item.fats.roundToInt()}g F | ${item.fiber.roundToInt()}g Fib", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                    }
                }
                if (index < rankedItems.size - 1) HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            }
        }
    }
}

@Composable
fun TrendingFoodsCard(entries: List<MealEntry>) {
    val topFoods = entries.map { it.mealName }
        .groupBy { it }
        .mapValues { it.value.size }
        .toList()
        .sortedByDescending { it.second }
        .take(5)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.width(6.dp))
                Text("Popular", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            if (topFoods.isEmpty()) {
                Text("No data", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
            topFoods.forEach { (name, count) ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(name, style = MaterialTheme.typography.labelSmall, maxLines = 1, modifier = Modifier.weight(1f))
                    Text("×$count", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                }
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
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
            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(Modifier.height(4.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            profiles.take(4).forEach { profile ->
                val userEntries = allEntries[profile.userName]?.filter { it.date == date.toString() } ?: emptyList()
                val totalCals = userEntries.sumOf { it.calories.toDouble() }.toFloat()
                
                if (userEntries.isNotEmpty()) {
                    val color = when {
                        isSelected -> MaterialTheme.colorScheme.onPrimary
                        totalCals > profile.calorieGoal + 50 -> MaterialTheme.colorScheme.error
                        totalCals < profile.calorieGoal - 150 -> macroAmber
                        else -> MaterialTheme.colorScheme.primary
                    }
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                } else {
                    Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(if(isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)))
                }
            }
        }
    }
}

@Composable
fun DailyDetailSection(
    date: LocalDate, 
    profiles: List<UserProfile>, 
    allEntries: Map<String, List<MealEntry>>, 
    activeUser: String?,
    isCalendarExpanded: Boolean,
    onToggleCalendar: () -> Unit
) {
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
                Column(modifier = Modifier.clickable { onToggleCalendar() }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (viewMode == "Daily") date.format(DateTimeFormatter.ofPattern("EEEE, MMM d")) else "Weekly Summary",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            Icons.Default.CalendarMonth, 
                            contentDescription = null, 
                            modifier = Modifier.size(18.dp).padding(start = 4.dp),
                            tint = if (isCalendarExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }
                    Text(
                        text = if (isCalendarExpanded) "Select a date below" else "Click to change date",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                
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
                    val weekDates = (0..6).map { date.minusDays(it.toLong()).toString() }
                    allEntries[profile.userName]?.filter { it.date in weekDates } ?: emptyList()
                }

                val totalCals = entries.sumOf { it.calories.toDouble() }.toFloat()
                val avgCals = if (viewMode == "Weekly") totalCals / 7 else totalCals
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        .then(if (profile.userName == activeUser) Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f), RoundedCornerShape(4.dp)) else Modifier),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp)) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp), tint = if (profile.userName == activeUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary)
                        Spacer(Modifier.width(8.dp))
                        Text(profile.userName, style = MaterialTheme.typography.bodyMedium, fontWeight = if (profile.userName == activeUser) FontWeight.Bold else FontWeight.Normal)
                    }
                    
                    if (entries.isNotEmpty()) {
                        val target = profile.calorieGoal
                        val color = when {
                            avgCals > target + 50 -> MaterialTheme.colorScheme.error
                            avgCals < target - 150 -> macroAmber
                            else -> MaterialTheme.colorScheme.primary
                        }
                        
                        Text(
                            text = "${avgCals.roundToInt()} / ${target.roundToInt()} kcal${if(viewMode == "Weekly") " (avg)" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = color,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    } else {
                        Text("No logs", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(horizontal = 4.dp))
                    }
                }
            }
        }
    }
}


@Composable
fun MonthlyTeamStats(month: YearMonth, profiles: List<UserProfile>, allEntries: Map<String, List<MealEntry>>, activeUser: String?) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text("Team Discipline (${month.month.name.lowercase().replaceFirstChar { it.uppercase() }})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        profiles.forEach { profile ->
            val userEntriesByDay = allEntries[profile.userName]
                ?.filter { it.date.startsWith(month.toString()) }
                ?.groupBy { it.date } ?: emptyMap()
            val daysOnTrack = userEntriesByDay.size
            
            val progress = if (month.monthValue == LocalDate.now().monthValue) LocalDate.now().dayOfMonth else month.lengthOfMonth()
            val percentage = (daysOnTrack.toFloat() / progress.toFloat()).coerceIn(0f, 1f)

            Column(modifier = Modifier.padding(vertical = 4.dp).then(if (profile.userName == activeUser) Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f), RoundedCornerShape(4.dp)) else Modifier)) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(profile.userName, style = MaterialTheme.typography.labelMedium, fontWeight = if (profile.userName == activeUser) FontWeight.Bold else FontWeight.Normal)
                    Text("$daysOnTrack days logged", style = MaterialTheme.typography.labelSmall)
                }
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { percentage },
                    modifier = Modifier.fillMaxWidth().height(8.dp).padding(horizontal = 4.dp).clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}
