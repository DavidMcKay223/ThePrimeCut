package com.primecut.theprimecut.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.primecut.theprimecut.util.loadFoodItemsFromAssets
import kotlinx.coroutines.launch
import android.widget.Toast
import com.primecut.theprimecut.ui.viewmodels.FoodItemViewModel
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SettingsScreen(
    foodItemViewModel: FoodItemViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
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
    }
}
