package com.primecut.theprimecut.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.primecut.theprimecut.ui.component.MacroProgressRow
import com.primecut.theprimecut.ui.viewmodels.MacroViewModel
import com.primecut.theprimecut.ui.viewmodels.UserProfileViewModel
import com.primecut.theprimecut.util.AppSession

@Composable
fun MacroProgressView(
    userProfileViewModel: UserProfileViewModel = hiltViewModel(),
    macroViewModel: MacroViewModel = hiltViewModel()
) {
    val profile by userProfileViewModel.userProfile.collectAsState()
    val summary by macroViewModel.summary.collectAsState()

    LaunchedEffect(Unit) {
        userProfileViewModel.loadProfile(AppSession.userName)
        macroViewModel.loadSummary()
    }

    if (profile != null) {
        val userProfile = profile!!
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Daily Nutrition Goals",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                MacroProgressRow("Calories", summary.calories, userProfile.calorieGoal, " cal")
                MacroProgressRow("Protein", summary.protein, userProfile.proteinGoal, " g")
                MacroProgressRow("Carbs", summary.carbs, userProfile.carbsGoal, " g")
                MacroProgressRow("Fat", summary.fats, userProfile.fatGoal, " g")
                MacroProgressRow("Fiber", summary.fiber, userProfile.fiberGoal, " g")
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}
