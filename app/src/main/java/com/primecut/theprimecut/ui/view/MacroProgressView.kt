package com.primecut.theprimecut.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
        Column(modifier = Modifier.padding(16.dp)) {
            MacroProgressRow("Calories", summary.calories, profile!!.calorieGoal, " cal")
            MacroProgressRow("Protein", summary.protein, profile!!.proteinGoal, " g")
            MacroProgressRow("Carbs", summary.carbs, profile!!.carbsGoal, " g")
            MacroProgressRow("Fat", summary.fats, profile!!.fatGoal, " g")
            MacroProgressRow("Fiber", summary.fiber, profile!!.fiberGoal, " g")
        }
    } else {
        Text("Loading user profile...")
    }
}
