package com.primecut.theprimecut.ui.screen

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import com.primecut.theprimecut.data.model.FoodItem
import com.primecut.theprimecut.data.model.MealEntry
import com.primecut.theprimecut.ui.viewmodels.MealEntryViewModel
import com.primecut.theprimecut.ui.viewmodels.FoodItemViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealEntryScreen(
    mealEntryViewModel: MealEntryViewModel = hiltViewModel(),
    foodItemViewModel: FoodItemViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val mealEntries by mealEntryViewModel.mealEntries.collectAsState()
    val foodItems by foodItemViewModel.foodItems.collectAsState()

    var selectedDate by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()))
    }
    var mealType by remember { mutableStateOf(suggestDefaultMealType()) }
    var foodName by remember { mutableStateOf("") }
    var portion by remember { mutableStateOf("1") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // Date input (just a clickable TextField to launch MaterialDatePicker)
        OutlinedTextField(
            value = selectedDate,
            onValueChange = {},
            label = { Text("Date") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val initialLocalDate = LocalDate.parse(selectedDate)
                    val initialSelectionMillis =
                        initialLocalDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

                    val picker = MaterialDatePicker.Builder.datePicker()
                        .setSelection(initialSelectionMillis)
                        .build()

                    picker.addOnPositiveButtonClickListener { utcMillis ->
                        val newLocalDate = Instant.ofEpochMilli(utcMillis)
                            .atZone(ZoneId.of("UTC"))
                            .toLocalDate()
                        selectedDate = newLocalDate.toString()
                        mealEntryViewModel.refreshMealEntries()
                    }

                    picker.show((context as androidx.fragment.app.FragmentActivity).supportFragmentManager, "date_picker")
                },
            readOnly = true
        )

        Spacer(Modifier.height(8.dp))

        // Meal type dropdown
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = mealType,
                onValueChange = {},
                label = { Text("Meal Type") },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                readOnly = true
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                listOf("Breakfast", "Lunch", "Dinner", "Snack").forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type) },
                        onClick = {
                            mealType = type
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Food item text input (autocomplete can be added with Accompanist or custom dropdown)
        OutlinedTextField(
            value = foodName,
            onValueChange = { foodName = it },
            label = { Text("Food Item") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        // Portion
        OutlinedTextField(
            value = portion,
            onValueChange = { portion = it },
            label = { Text("Portion") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                val portionVal = portion.toFloatOrNull() ?: 1f
                val foodItem: FoodItem? = foodItems.find { it.recipeName == foodName }
                if (foodItem != null) {
                    val entry = MealEntry(
                        date = selectedDate,
                        day = SimpleDateFormat("EEEE", Locale.US)
                            .format(SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(selectedDate)!!),
                        mealType = mealType,
                        mealName = foodItem.recipeName,
                        groupName = foodItem.groupName,
                        portionEaten = portionVal,
                        measurementServings = foodItem.measurementServings,
                        measurementType = foodItem.measurementType,
                        calories = foodItem.caloriesPerServing * portionVal,
                        protein = foodItem.protein * portionVal,
                        carbs = foodItem.carbs * portionVal,
                        fats = foodItem.fats * portionVal,
                        fiber = foodItem.fiber * portionVal
                    )
                    mealEntryViewModel.addMealEntry(entry)
                    foodName = ""
                    portion = "1"
                } else {
                    Toast.makeText(context, "Food item not found", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Meal")
        }

        Spacer(Modifier.height(24.dp))

        if (mealEntries.isEmpty()) {
            Text("No meals found for this date", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn {
                items(mealEntries.filter { it.date == selectedDate }) { entry ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("${entry.mealType}: ${entry.mealName}")
                                Text("Calories: ${entry.calories.toInt()}")
                            }
                            IconButton(onClick = {
                                scope.launch {
                                    mealEntryViewModel.deleteMealEntry(entry)
                                }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun suggestDefaultMealType(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 6..10 -> "Breakfast"
        in 11..15 -> "Lunch"
        in 16..19 -> "Dinner"
        else -> "Snack"
    }
}
