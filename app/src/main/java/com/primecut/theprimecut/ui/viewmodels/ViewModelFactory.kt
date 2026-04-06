package com.primecut.theprimecut.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.primecut.theprimecut.di.AppContainer

class ViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(FoodItemViewModel::class.java) -> {
                FoodItemViewModel(container.foodItemRepository) as T
            }
            modelClass.isAssignableFrom(MealEntryViewModel::class.java) -> {
                MealEntryViewModel(container.mealEntryRepository) as T
            }
            modelClass.isAssignableFrom(UserProfileViewModel::class.java) -> {
                UserProfileViewModel(container.userProfileRepository) as T
            }
            modelClass.isAssignableFrom(WeightLogViewModel::class.java) -> {
                WeightLogViewModel(container.weightLogRepository) as T
            }
            modelClass.isAssignableFrom(MacroViewModel::class.java) -> {
                MacroViewModel(container.mealEntryRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
