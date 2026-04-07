package com.primecut.theprimecut.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.primecut.theprimecut.ui.component.CalorieProgressCircle
import com.primecut.theprimecut.ui.component.MacroProgressRow
import com.primecut.theprimecut.ui.viewmodels.MacroViewModel
import com.primecut.theprimecut.ui.viewmodels.UserProfileViewModel
import com.primecut.theprimecut.ui.viewmodels.ViewModelFactory
import com.primecut.theprimecut.util.AppSession
import java.util.Calendar

@Composable
fun MacroProgressView(
    onProfileClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    userProfileViewModel: UserProfileViewModel = viewModel(
        factory = ViewModelFactory((LocalContext.current.applicationContext as PrimeCutApplication).container)
    ),
    macroViewModel: MacroViewModel = viewModel(
        factory = ViewModelFactory((LocalContext.current.applicationContext as PrimeCutApplication).container)
    )
) {
    val profile by userProfileViewModel.userProfile.collectAsState()
    val summary by macroViewModel.summary.collectAsState()

    LaunchedEffect(Unit) {
        userProfileViewModel.loadProfile(AppSession.userName)
        macroViewModel.loadSummary()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Dashboard Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = getWittyGreeting(),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )
                )
                Text(
                    text = getDailyMotivation(summary.calories, profile?.calorieGoal ?: 2000f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    onClick = onProfileClick
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        if (profile != null) {
            val userProfile = profile!!
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Main Calorie Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(32.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = Color(0xFFFF7043),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Daily Calories",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))

                            val hasMacroOverflow = (summary.protein > userProfile.proteinGoal) ||
                                    (summary.carbs > userProfile.carbsGoal) ||
                                    (summary.fats > userProfile.fatGoal) ||
                                    (summary.fiber > userProfile.fiberGoal)

                            CalorieProgressCircle(
                                current = summary.calories,
                                goal = userProfile.calorieGoal,
                                hasMacroOverflow = hasMacroOverflow,
                                size = 220.dp,
                                strokeWidth = 20.dp
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }

                // Nutrient Details Section
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "The Prime Breakdown",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Precision Fueling",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            MacroProgressRow("Protein", summary.protein, userProfile.proteinGoal, " g")
                            MacroProgressRow("Carbs", summary.carbs, userProfile.carbsGoal, " g")
                            MacroProgressRow("Fat", summary.fats, userProfile.fatGoal, " g")
                            MacroProgressRow("Fiber", summary.fiber, userProfile.fiberGoal, " g")
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

private fun getWittyGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 0..11 -> "Morning, Operator"
        in 12..16 -> "Mid-Day Calibration"
        in 17..20 -> "Evening Refuel"
        else -> "Late-Night Optimization"
    }
}

private fun getDailyMotivation(current: Float, goal: Float): String {
    val ratio = current / goal
    return when {
        ratio == 0f -> "System Idle: Awaiting Initial Fuel Injection."
        ratio < 0.3f -> "Initializing: Deploy Primary Macronutrients."
        ratio < 0.7f -> "Status Nominal: Maintain Current Trajectory."
        ratio < 1.0f -> "Final Phase: Precision Intake Required."
        else -> "Capacity Reached: Cease Further Intake."
    }
}

