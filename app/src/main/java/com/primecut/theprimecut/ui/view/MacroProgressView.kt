package com.primecut.theprimecut.ui.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.graphicsLayer
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
import com.primecut.theprimecut.data.model.MacroSummary
import com.primecut.theprimecut.data.model.MealEntry
import com.primecut.theprimecut.data.model.UserProfile
import com.primecut.theprimecut.ui.component.MultiMacroProgressCircle
import com.primecut.theprimecut.ui.viewmodels.MacroViewModel
import com.primecut.theprimecut.ui.viewmodels.MealEntryViewModel
import com.primecut.theprimecut.ui.viewmodels.UserProfileViewModel
import com.primecut.theprimecut.ui.viewmodels.ViewModelFactory
import com.primecut.theprimecut.util.AppSession
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale

@Composable
fun MacroProgressView(
    onProfileClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    userProfileViewModel: UserProfileViewModel = viewModel(
        factory = ViewModelFactory((LocalContext.current.applicationContext as PrimeCutApplication).container)
    ),
    macroViewModel: MacroViewModel = viewModel(
        factory = ViewModelFactory((LocalContext.current.applicationContext as PrimeCutApplication).container)
    ),
    mealEntryViewModel: MealEntryViewModel = viewModel(
        factory = ViewModelFactory((LocalContext.current.applicationContext as PrimeCutApplication).container)
    )
) {
    val profile by userProfileViewModel.userProfile.collectAsState()
    val allUserNames by userProfileViewModel.allUserNames.collectAsState()
    val allProfiles by userProfileViewModel.allProfiles.collectAsState()
    val summary by macroViewModel.summary.collectAsState()
    val allUsersEntries by mealEntryViewModel.allUsersEntries.collectAsState()

    LaunchedEffect(allUserNames) {
        if (allUserNames.isNotEmpty()) {
            macroViewModel.loadAllUsersSummaries(allUserNames)
            
            // Load current week (Sunday to Saturday)
            val today = LocalDate.now()
            val currentSunday = today.minusDays(today.dayOfWeek.value % 7.toLong())
            val start = currentSunday.toString()
            val end = currentSunday.plusDays(6).toString()
            
            mealEntryViewModel.loadAllUsersEntriesRange(start, end, allUserNames)
        }
    }

    LaunchedEffect(userProfileViewModel) {
        userProfileViewModel.onUserSwitched = {
            macroViewModel.refresh()
        }
    }

    LaunchedEffect(AppSession.userName, profile?.calorieGoal) {
        userProfileViewModel.loadProfile(AppSession.userName)
        macroViewModel.loadSummary()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (profile != null) {
            val userProfile = profile!!
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Greeting and Date Section
                item {
                    Column(modifier = Modifier.padding(top = 16.dp)) {
                        Text(
                            text = "${getFriendlyGreeting()}, ${userProfile.userName}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = java.text.SimpleDateFormat("EEEE, MMMM d", java.util.Locale.getDefault()).format(java.util.Date()),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Main Calorie & Macro Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Daily Progress",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))

                            MultiMacroProgressCircle(
                                caloriesCurrent = summary.calories,
                                caloriesGoal = userProfile.calorieGoal,
                                proteinCurrent = summary.protein,
                                proteinGoal = userProfile.proteinGoal,
                                carbsCurrent = summary.carbs,
                                carbsGoal = userProfile.carbsGoal,
                                fatCurrent = summary.fats,
                                fatGoal = userProfile.fatGoal,
                                fiberCurrent = summary.fiber,
                                fiberGoal = userProfile.fiberGoal,
                                size = 240.dp,
                                strokeWidth = 10.dp,
                                spacing = 8.dp
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Legend for the circle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                LegendItem("Prot", Color(0xFFE91E63))
                                LegendItem("Carb", Color(0xFF03A9F4))
                                LegendItem("Fat", Color(0xFFFFC107))
                                LegendItem("Fib", Color(0xFF4CAF50))
                            }
                        }
                    }
                }

                // Nutrient Details Section
                item {
                    Text(
                        text = "Macro Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MacroRow("Protein", summary.protein, userProfile.proteinGoal, Color(0xFFE91E63))
                            MacroRow("Carbs", summary.carbs, userProfile.carbsGoal, Color(0xFF03A9F4))
                            MacroRow("Fat", summary.fats, userProfile.fatGoal, Color(0xFFFFC107))
                            MacroRow("Fiber", summary.fiber, userProfile.fiberGoal, Color(0xFF4CAF50))
                        }
                    }
                }

                // Daily Standings Section (7-Day Consistency)
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "7-Day Activity",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Calculate week bounds: Start of current week (Sunday) to End of week (Saturday)
                        val today = LocalDate.now()
                        val currentSunday = today.minusDays(today.dayOfWeek.value % 7.toLong())
                        val weekDates = (0..6).map { currentSunday.plusDays(it.toLong()).toString() }
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Header row with dates
                                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                                    Spacer(modifier = Modifier.width(80.dp))
                                    weekDates.forEach { dateStr ->
                                        val date = LocalDate.parse(dateStr)
                                        Text(
                                            text = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.NARROW, Locale.getDefault()),
                                            modifier = Modifier.weight(1f),
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                allProfiles.forEach { userProfile ->
                                    val userEntries = allUsersEntries[userProfile.userName] ?: emptyList()
                                    ActivityRow(
                                        userName = userProfile.userName,
                                        isActiveUser = userProfile.userName == AppSession.userName,
                                        dates = weekDates,
                                        userEntries = userEntries
                                    )
                                    if (userProfile != allProfiles.last()) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(strokeWidth = 3.dp, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun ActivityRow(
    userName: String,
    isActiveUser: Boolean,
    dates: List<String>,
    userEntries: List<MealEntry>
) {
    val entriesByDate = userEntries.groupBy { it.date }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = userName,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isActiveUser) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.width(80.dp),
            maxLines = 1,
            color = if (isActiveUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            dates.forEach { date ->
                val hasLogged = entriesByDate.containsKey(date)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (hasLogged) MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                )
            }
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun MacroRow(label: String, current: Float, goal: Float, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Text("${current.toInt()}g / ${goal.toInt()}g", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        val progress = if (goal > 0) (current / goal).coerceIn(0f, 1f) else 0f
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.width(100.dp).height(8.dp).clip(CircleShape),
            color = color,
            trackColor = color.copy(alpha = 0.1f)
        )
    }
}

@Composable
fun LeaderboardCard(
    rank: Int,
    profile: UserProfile,
    summary: MacroSummary,
    score: Float
) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(if (expanded) 180f else 0f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (profile.userName == AppSession.userName)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rank Circle
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            when (rank) {
                                1 -> Color(0xFFFFD700) // Gold
                                2 -> Color(0xFFC0C0C0) // Silver
                                3 -> Color(0xFFCD7F32) // Bronze
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#$rank",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (rank <= 3) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1.0f)) {
                    Text(
                        text = profile.userName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${summary.calories.toInt()} / ${profile.calorieGoal.toInt()} kcal",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${score.toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp)
                            .graphicsLayer(rotationZ = rotation),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MacroMiniStat("Protein", summary.protein, profile.proteinGoal)
                        MacroMiniStat("Carbs", summary.carbs, profile.carbsGoal)
                        MacroMiniStat("Fat", summary.fats, profile.fatGoal)
                        MacroMiniStat("Fiber", summary.fiber, profile.fiberGoal)
                    }
                }
            }
        }
    }
}

@Composable
fun MacroMiniStat(label: String, current: Float, goal: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = "${current.toInt()}g",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = if (current > goal && goal > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
        val progress = if (goal > 0) (current / goal).coerceIn(0f, 1f) else 0f
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(CircleShape),
            color = if (current > goal && goal > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

private fun getFriendlyGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 0..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        else -> "Good Evening"
    }
}
