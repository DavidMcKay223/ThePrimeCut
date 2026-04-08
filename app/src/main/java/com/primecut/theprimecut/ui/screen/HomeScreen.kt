package com.primecut.theprimecut.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.primecut.theprimecut.PrimeCutApplication
import com.primecut.theprimecut.ui.view.MacroProgressView
import com.primecut.theprimecut.ui.viewmodels.FoodItemViewModel
import com.primecut.theprimecut.ui.viewmodels.ViewModelFactory

@Composable
fun HomeScreen(
    onProfileClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    foodItemViewModel: FoodItemViewModel = viewModel(
        factory = ViewModelFactory((LocalContext.current.applicationContext as PrimeCutApplication).container)
    )
) {
    LaunchedEffect(Unit) {
        foodItemViewModel.clearFilters()
    }

    MacroProgressView(
        onProfileClick = onProfileClick,
        onSettingsClick = onSettingsClick
    )
}
