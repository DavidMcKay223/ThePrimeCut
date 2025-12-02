package com.primecut.theprimecut.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.primecut.theprimecut.data.model.DietType
import com.primecut.theprimecut.data.model.Sex
import com.primecut.theprimecut.data.model.UserProfile
import com.primecut.theprimecut.ui.component.DropdownSelector
import com.primecut.theprimecut.ui.viewmodels.FoodItemViewModel
import com.primecut.theprimecut.ui.viewmodels.UserProfileViewModel
import com.primecut.theprimecut.util.loadFoodItemsFromAssets
import kotlinx.coroutines.launch

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

    // Local state for form fields
    var userName by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var sex by remember { mutableStateOf("") }
    var activity by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("") }
    var diet by remember { mutableStateOf("") }

    // Populate fields when profile loads
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // --- Personal Information Section ---
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Personal Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                OutlinedTextField(
                    value = userName,
                    onValueChange = { userName = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = age,
                        onValueChange = { if (it.all { char -> char.isDigit() }) age = it },
                        label = { Text("Age") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    DropdownSelector(
                        label = "Sex",
                        selected = sex,
                        options = sexes,
                        onSelected = { sex = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = height,
                        onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) height = it },
                        label = { Text("Height (in)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) weight = it },
                        label = { Text("Weight (lbs)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        }

        // --- Goals & Preferences Section ---
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Goals & Preferences",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                DropdownSelector(
                    label = "Activity Level",
                    selected = activity,
                    options = activityLevels,
                    onSelected = { activity = it },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DropdownSelector(
                        label = "Goal",
                        selected = goal,
                        options = goals,
                        onSelected = { goal = it },
                        modifier = Modifier.weight(1f)
                    )
                    DropdownSelector(
                        label = "Diet",
                        selected = diet,
                        options = diets,
                        onSelected = { diet = it },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // --- Actions ---
        Button(
            onClick = {
                if (userName.isBlank() || age.isBlank() || height.isBlank() || weight.isBlank() || sex.isBlank() || activity.isBlank() || goal.isBlank() || diet.isBlank()) {
                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@Button
                }
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

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
             OutlinedButton(
                onClick = {
                    userProfileViewModel.recalcGoals(userName) {
                        Toast.makeText(context, "Goals recalculated!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Recalc Goals")
            }

            OutlinedButton(
                onClick = {
                    scope.launch {
                        val items = loadFoodItemsFromAssets(context)
                        foodItemViewModel.syncFoodItemsFromAssets(items) {
                            Toast.makeText(context, "Sync Complete", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Sync DB")
            }
        }

        // --- Current Targets Display ---
        profile?.let {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Daily Targets",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TargetItem("Calories", "${it.calorieGoal}")
                        TargetItem("Protein", "${it.proteinGoal}g")
                        TargetItem("Carbs", "${it.carbsGoal}g")
                        TargetItem("Fat", "${it.fatGoal}g")
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun TargetItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
        )
    }
}
