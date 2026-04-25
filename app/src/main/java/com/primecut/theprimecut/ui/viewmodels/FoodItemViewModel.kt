package com.primecut.theprimecut.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.primecut.theprimecut.data.model.FoodItem
import com.primecut.theprimecut.data.repository.FoodItemRepository
import com.primecut.theprimecut.data.repository.MealEntryRepository
import com.primecut.theprimecut.util.AppSession
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class FoodItemViewModel(
    private val repository: FoodItemRepository,
    private val mealEntryRepository: MealEntryRepository
) : ViewModel() {

    private val _allFoodItems = MutableStateFlow<List<FoodItem>>(emptyList())

    // Filter States
    private val _nameQuery = MutableStateFlow("")
    val nameQuery: StateFlow<String> = _nameQuery

    private val _brandQuery = MutableStateFlow("")
    val brandQuery: StateFlow<String> = _brandQuery

    private val _groupQuery = MutableStateFlow("")
    val groupQuery: StateFlow<String> = _groupQuery

    private val _selectedFilters = MutableStateFlow<Set<String>>(emptySet())
    val selectedFilters: StateFlow<Set<String>> = _selectedFilters

    // Brands: Filtered by the selected Category (Group)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val historicalNamesFlow: StateFlow<Set<String>> = AppSession.userNameFlow
        .flatMapLatest { user ->
            flow {
                val today = LocalDate.now().toString()
                val allEntries = mealEntryRepository.getAll(user)
                val names = allEntries
                    .filter { it.date != today }
                    .map { it.mealName }
                    .toSet()
                emit(names)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    // Brands: Filtered by the selected Category (Group)
    val brands: StateFlow<List<String>> = combine(
        _allFoodItems,
        _groupQuery
    ) { items, group ->
        items.filter { item ->
            group.isEmpty() || item.groupName == group
        }.map { it.brandType }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Groups: Always show all available groups so the user can always switch categories
    val groups: StateFlow<List<String>> = _allFoodItems.map { items ->
        items.mapNotNull { it.groupName }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // The Main Result List
    val foodItems: StateFlow<List<HistoricalFoodItem>> = combine(
        combine(_allFoodItems, _nameQuery, _brandQuery, _groupQuery, _selectedFilters) { items, name, brand, group, filters ->
            FilterParams(items, name, brand, group, filters)
        },
        historicalNamesFlow
    ) { params, logged ->
        val filtered = params.items.filter { item ->
            // Search name or brand
            val matchesName = params.name.isEmpty() || 
                item.recipeName.contains(params.name, ignoreCase = true) || 
                item.brandType.contains(params.name, ignoreCase = true)
            
            // Dropdown exact matches
            val matchesBrand = params.brand.isEmpty() || item.brandType == params.brand
            val matchesGroup = params.group.isEmpty() || item.groupName == params.group

            // Macro toggle filters
            val matchesFilters = params.filters.isEmpty() || params.filters.all { filter ->
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

        val sortedList = filtered.map { item ->
            val isHistorical = logged.any { it.equals(item.recipeName, ignoreCase = true) }
            HistoricalFoodItem(item, isHistorical)
        }.sortedWith(
            compareByDescending<HistoricalFoodItem> { it.isHistorical }
                .thenBy { it.item.recipeName }
        )

        sortedList
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    data class HistoricalFoodItem(
        val item: FoodItem,
        val isHistorical: Boolean
    )

    private data class FilterParams(
        val items: List<FoodItem>,
        val name: String,
        val brand: String,
        val group: String,
        val filters: Set<String>
    )

    init { refreshFoodItems() }

    fun clearFilters() {
        _nameQuery.value = ""
        _brandQuery.value = ""
        _groupQuery.value = ""
        _selectedFilters.value = emptySet()
    }

    private fun refreshFoodItems() {
        viewModelScope.launch {
            _allFoodItems.value = repository.getAll()
        }
    }

    fun onNameQueryChanged(query: String) { _nameQuery.value = query }
    fun onBrandQueryChanged(query: String) { _brandQuery.value = query }
    fun onGroupQueryChanged(query: String) { _groupQuery.value = query }

    fun syncFoodItemsFromAssets(items: List<FoodItem>, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.insertAll(items)
            refreshFoodItems()
            onComplete()
        }
    }

    fun deleteAllFoodItems(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteAll()
            refreshFoodItems()
            onComplete()
        }
    }

    fun toggleFilter(filter: String) {
        val currentFilters = _selectedFilters.value.toMutableSet()
        if (currentFilters.contains(filter)) currentFilters.remove(filter)
        else currentFilters.add(filter)
        _selectedFilters.value = currentFilters
    }
}
