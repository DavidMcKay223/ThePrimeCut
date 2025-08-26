package com.primecut.theprimecut.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.primecut.theprimecut.util.loadFoodItemsFromAssets
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import com.primecut.theprimecut.ui.viewmodels.FoodItemViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.primecut.theprimecut.ui.viewmodels.UserProfileViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import com.primecut.theprimecut.data.model.DietType
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import com.primecut.theprimecut.data.model.Sex
import com.primecut.theprimecut.data.model.UserProfile
import com.primecut.theprimecut.ui.component.DropdownSelector
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.LaunchedEffect

@Composable
fun SettingsScreen(
    foodItemViewModel: FoodItemViewModel = hiltViewModel(),
    userProfileViewModel: UserProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val profile by userProfileViewModel.userProfile.collectAsState()

    val sexes = listOf("Male", "Female")
    val activityLevels = listOf("Sedentary", "Lightly Active", "Moderately Active", "Very Active", "Super Active")
    val goals = listOf("Maintain", "Lose0.5", "Lose1", "Lose2", "Gain0.5", "Gain1")
    val diets = DietType.entries.map { it.name }

    var userName by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var sex by remember { mutableStateOf("") }
    var activity by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("") }
    var diet by remember { mutableStateOf("") }

    LaunchedEffect(profile) {
        profile?.let {
            userName = it.userName
            age = it.age.toInt().toString()
            height = it.heightInches.toInt().toString()
            weight = it.weightPounds.toInt().toString()
            sex = it.sex.name
            activity = it.activityLevel
            goal = it.goalType
            diet = it.dietType.name
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
        .verticalScroll(rememberScrollState())
    ) {
        Button(
            onClick = {
                scope.launch {
                    val items = loadFoodItemsFromAssets(context)
                    foodItemViewModel.syncFoodItemsFromAssets(items) {
                        Toast.makeText(context, "Sync Complete", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        ) {
            Text("Sync Data")
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = userName,
                onValueChange = { userName = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                label = { Text("Age") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = height,
                onValueChange = { height = it },
                label = { Text("Height (inches)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text("Weight (lbs)") },
                modifier = Modifier.fillMaxWidth()
            )

            DropdownSelector("Sex", sex, sexes) { sex = it }
            DropdownSelector("Activity", activity, activityLevels) { activity = it }
            DropdownSelector("Goal", goal, goals) { goal = it }
            DropdownSelector("Diet", diet, diets) { diet = it }

            Button(
                onClick = {
                    val newProfile = UserProfile(
                        userName = userName,
                        age = age.toFloatOrNull() ?: 25f,
                        heightInches = height.toFloatOrNull() ?: 70f,
                        weightPounds = weight.toFloatOrNull() ?: 180f,
                        sex = Sex.valueOf(sex),
                        activityLevel = activity,
                        goalType = goal,
                        dietType = DietType.valueOf(diet)
                    )
                    userProfileViewModel.saveProfile(newProfile) {
                        Toast.makeText(context, "Profile saved!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Profile")
            }

            Button(
                onClick = {
                    userProfileViewModel.recalcGoals(userName) {
                        Toast.makeText(context, "Goals recalculated!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Recalculate Goals")
            }

            profile?.let {
                Text(
                    text = """
                    Calories: ${it.calorieGoal} cal
                    Protein: ${it.proteinGoal} g
                    Carbs: ${it.carbsGoal} g
                    Fat: ${it.fatGoal} g
                    Fiber: ${it.fiberGoal} g
                """.trimIndent()
                )
            }
        }
    }
}
