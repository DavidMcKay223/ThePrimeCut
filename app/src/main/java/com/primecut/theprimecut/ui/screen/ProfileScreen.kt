package com.primecut.theprimecut.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.primecut.theprimecut.ui.viewmodels.WeightLogViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.Instant
import java.time.ZoneId

@Composable
fun ProfileScreen(
    viewModel: WeightLogViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val logs by viewModel.logs.collectAsState()

    var dateInput by remember { mutableStateOf("") }
    var weightInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = dateInput,
            onValueChange = { dateInput = it },
            label = { Text("Date") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = {
                    val picker = MaterialDatePicker.Builder.datePicker()
                        .setTitleText("Select Date")
                        .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                        .build()
                    picker.show((context as androidx.fragment.app.FragmentActivity).supportFragmentManager, "date_picker")
                    picker.addOnPositiveButtonClickListener { selection ->
                        val date = Instant.ofEpochMilli(selection)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        dateInput = date.toString()
                    }
                }) {
                    Icon(Icons.Default.DateRange, contentDescription = "Pick Date")
                }
            }
        )

        OutlinedTextField(
            value = weightInput,
            onValueChange = { weightInput = it },
            label = { Text("Weight (lbs)") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val weight = weightInput.toFloatOrNull()
                if (dateInput.isBlank() || weight == null) {
                    Toast.makeText(context, "Enter valid date and weight", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.addOrUpdateLog("DefaultUser", dateInput, weight)
                    dateInput = ""
                    weightInput = ""
                    Toast.makeText(context, "Weight added!", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add / Update Weight")
        }

        if (logs.isNotEmpty()) {

        } else {
            Text("No weight logs yet")
        }
    }
}
