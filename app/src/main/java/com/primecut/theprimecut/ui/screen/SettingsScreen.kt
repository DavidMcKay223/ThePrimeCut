package com.primecut.theprimecut.ui.screen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.primecut.theprimecut.PrimeCutApplication
import com.primecut.theprimecut.ui.viewmodels.ViewModelFactory
import com.primecut.theprimecut.data.model.DietType
import com.primecut.theprimecut.data.model.Sex
import com.primecut.theprimecut.data.model.UserProfile
import com.primecut.theprimecut.ui.component.DropdownSelector
import com.primecut.theprimecut.ui.component.ResponsiveInputRow
import com.primecut.theprimecut.ui.viewmodels.FoodItemViewModel
import com.primecut.theprimecut.ui.viewmodels.UserProfileViewModel
import com.primecut.theprimecut.util.AppSession
import com.primecut.theprimecut.util.loadFoodItemsFromAssets
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    foodItemViewModel: FoodItemViewModel = viewModel(
        factory = ViewModelFactory((LocalContext.current.applicationContext as PrimeCutApplication).container)
    ),
    userProfileViewModel: UserProfileViewModel = viewModel(
        factory = ViewModelFactory((LocalContext.current.applicationContext as PrimeCutApplication).container)
    ),
    macroViewModel: com.primecut.theprimecut.ui.viewmodels.MacroViewModel = viewModel(
        factory = ViewModelFactory((LocalContext.current.applicationContext as PrimeCutApplication).container)
    ),
    mealEntryViewModel: com.primecut.theprimecut.ui.viewmodels.MealEntryViewModel = viewModel(
        factory = ViewModelFactory((LocalContext.current.applicationContext as PrimeCutApplication).container)
    )
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val profile by userProfileViewModel.userProfile.collectAsState()
    val allUsers by userProfileViewModel.allUserNames.collectAsState()

    // Sync all viewmodels when user switches
    LaunchedEffect(userProfileViewModel) {
        userProfileViewModel.onUserSwitched = {
            macroViewModel.refresh()
            mealEntryViewModel.refreshMealEntries()
            // weightLogViewModel.refresh() // if we had it here
        }
    }

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

    // Helper to clear form
    fun clearForm() {
        userName = ""
        age = ""
        height = ""
        weight = ""
        sex = ""
        activity = ""
        goal = ""
        diet = ""
    }

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
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Button(
                onClick = { clearForm() },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Add User", style = MaterialTheme.typography.labelLarge)
            }
        }

        // --- User Selector ---
        if (allUsers.isNotEmpty()) {
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "Active User",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                    DropdownSelector(
                        label = "Select User",
                        selected = AppSession.userName,
                        options = allUsers,
                        onSelected = { 
                            userProfileViewModel.loadProfile(it)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                    )
                }
            }
        }

        // --- Personal Information Section ---
        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Personal Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = userName,
                    onValueChange = { userName = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    shape = RoundedCornerShape(12.dp)
                )

                ResponsiveInputRow(
                    content1 = { modifier ->
                        OutlinedTextField(
                            value = age,
                            onValueChange = { if (it.all { char -> char.isDigit() }) age = it },
                            label = { Text("Age") },
                            modifier = modifier,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            shape = RoundedCornerShape(12.dp)
                        )
                    },
                    content2 = { modifier ->
                        DropdownSelector(
                            label = "Sex",
                            selected = sex,
                            options = sexes,
                            onSelected = { sex = it },
                            modifier = modifier,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                )

                ResponsiveInputRow(
                    content1 = { modifier ->
                        OutlinedTextField(
                            value = height,
                            onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) height = it },
                            label = { Text("Height (in)") },
                            modifier = modifier,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                            shape = RoundedCornerShape(12.dp)
                        )
                    },
                    content2 = { modifier ->
                        OutlinedTextField(
                            value = weight,
                            onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) weight = it },
                            label = { Text("Weight (lbs)") },
                            modifier = modifier,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                )
            }
        }

        // --- Goals & Preferences Section ---
        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Goals & Activity",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                DropdownSelector(
                    label = "Activity Level",
                    selected = activity,
                    options = activityLevels,
                    onSelected = { activity = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                ResponsiveInputRow(
                    content1 = { modifier ->
                        DropdownSelector(
                            label = "Goal",
                            selected = goal,
                            options = goals,
                            onSelected = { goal = it },
                            modifier = modifier,
                            shape = RoundedCornerShape(12.dp)
                        )
                    },
                    content2 = { modifier ->
                        DropdownSelector(
                            label = "Diet Style",
                            selected = diet,
                            options = diets,
                            onSelected = { diet = it },
                            modifier = modifier,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                )
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
                    sex = if (sex == "Male") Sex.Male else Sex.Female,
                    activityLevel = activity,
                    goalType = goal,
                    dietType = DietType.valueOf(diet)
                )
                userProfileViewModel.saveProfile(newProfile) {
                    userProfileViewModel.recalcGoals(userName) {
                        Toast.makeText(context, "Profile and Goals updated!", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Save Profile", fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
            onClick = {
                scope.launch {
                    val items = loadFoodItemsFromAssets(context)
                    foodItemViewModel.syncFoodItemsFromAssets(items) {
                        Toast.makeText(context, "Database Synced!", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Sync Food Database")
        }

        var showDeleteDialog by remember { mutableStateOf(false) }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete All Food Items?") },
                text = { Text("This will permanently remove all food items from your database. You will need to re-sync from assets to restore them.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            foodItemViewModel.deleteAllFoodItems {
                                showDeleteDialog = false
                                Toast.makeText(context, "All food items deleted", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete Everything")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        OutlinedButton(
            onClick = { showDeleteDialog = true },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
        ) {
            Text("Clear Food Database")
        }

        // --- Current Targets Display ---
        profile?.let {
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Your Daily Targets",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TargetItem("Calories", "${it.calorieGoal.toInt()}", MaterialTheme.colorScheme.onPrimaryContainer)
                        TargetItem("Protein", "${it.proteinGoal.toInt()}g", MaterialTheme.colorScheme.onPrimaryContainer)
                        TargetItem("Carbs", "${it.carbsGoal.toInt()}g", MaterialTheme.colorScheme.onPrimaryContainer)
                        TargetItem("Fat", "${it.fatGoal.toInt()}g", MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun TargetItem(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color.copy(alpha = 0.7f)
        )
    }
}
