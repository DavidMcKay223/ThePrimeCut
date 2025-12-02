package com.primecut.theprimecut.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.primecut.theprimecut.data.model.FoodItem
import com.primecut.theprimecut.data.repository.FoodItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FoodItemViewModel @Inject constructor(
    private val repository: FoodItemRepository
) : ViewModel() {

    private val _allFoodItems = MutableStateFlow<List<FoodItem>>(emptyList())

    // Filters
    private val _nameQuery = MutableStateFlow("")
    val nameQuery: StateFlow<String> = _nameQuery

    private val _brandQuery = MutableStateFlow("")
    val brandQuery: StateFlow<String> = _brandQuery

    private val _groupQuery = MutableStateFlow("")
    val groupQuery: StateFlow<String> = _groupQuery

    private val _selectedFilters = MutableStateFlow<Set<String>>(emptySet())
    val selectedFilters: StateFlow<Set<String>> = _selectedFilters

    val foodItems: StateFlow<List<FoodItem>> = combine(
        _allFoodItems,
        _nameQuery,
        _brandQuery,
        _groupQuery,
        _selectedFilters
    ) { items, name, brand, group, filters ->
        items.filter { item ->
            // Search Logic
            val matchesName = if (name.isEmpty()) true else item.recipeName.contains(name, ignoreCase = true)
            val matchesBrand = if (brand.isEmpty()) true else item.brandType.contains(brand, ignoreCase = true)
            val matchesGroup = if (group.isEmpty()) true else (item.groupName?.contains(group, ignoreCase = true) == true)

            // Filter Logic (AND condition: must match all selected filters)
            val matchesFilters = filters.isEmpty() || filters.all { filter ->
                when (filter) {
                    "High Protein" -> item.isHighProtein
                    "Low Carb" -> item.isLowCarb
                    "Keto" -> item.isKeto
                    "Bulk" -> item.isBulkMeal
                    "Low Fiber" -> item.isLowFiber
                    "Balanced" -> item.isBalancedMeal
                    "High Fat" -> item.isHighFat
                    "Breakfast" -> item.isBreakfast
                    "Lunch" -> item.isLunch
                    "Dinner" -> item.isDinner
                    "Snack" -> item.isSnack
                    else -> true
                }
            }

            matchesName && matchesBrand && matchesGroup && matchesFilters
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        refreshFoodItems()
    }

    private fun refreshFoodItems() {
        viewModelScope.launch {
            val items = repository.getAll()
            _allFoodItems.value = items
        }
    }

    fun onNameQueryChanged(query: String) {
        _nameQuery.value = query
    }

    fun onBrandQueryChanged(query: String) {
        _brandQuery.value = query
    }

    fun onGroupQueryChanged(query: String) {
        _groupQuery.value = query
    }

    fun toggleFilter(filter: String) {
        val currentFilters = _selectedFilters.value.toMutableSet()
        if (currentFilters.contains(filter)) {
            currentFilters.remove(filter)
        } else {
            currentFilters.add(filter)
        }
        _selectedFilters.value = currentFilters
    }

    fun syncFoodItemsFromAssets(
        newItems: List<FoodItem>,
        onComplete: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            repository.insertAll(newItems)
            refreshFoodItems()
            onComplete?.invoke()
        }
    }
}
