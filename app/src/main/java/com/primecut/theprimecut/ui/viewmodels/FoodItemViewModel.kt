package com.primecut.theprimecut.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.primecut.theprimecut.data.model.FoodItem
import com.primecut.theprimecut.data.repository.FoodItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class FoodItemViewModel @Inject constructor(
    private val repository: FoodItemRepository
) : ViewModel() {

    private val _foodItems = MutableStateFlow<List<FoodItem>>(emptyList())
    val foodItems: StateFlow<List<FoodItem>> = _foodItems

    init {
        refreshFoodItems()
    }

    private fun refreshFoodItems() {
        viewModelScope.launch {
            val items = repository.getAll() // suspend function
            _foodItems.value = items
        }
    }

    fun syncFoodItemsFromAssets(
        newItems: List<FoodItem>,
        onComplete: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            repository.insertAll(newItems)
            refreshFoodItems() // update StateFlow after insert
            onComplete?.invoke()
        }
    }
}
