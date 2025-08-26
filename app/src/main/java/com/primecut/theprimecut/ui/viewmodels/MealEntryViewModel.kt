package com.primecut.theprimecut.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.primecut.theprimecut.data.model.MealEntry
import com.primecut.theprimecut.data.repository.MealEntryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MealEntryViewModel @Inject constructor(
    private val repository: MealEntryRepository
) : ViewModel() {

    private val _mealEntries = MutableStateFlow<List<MealEntry>>(emptyList())
    val mealEntries: StateFlow<List<MealEntry>> = _mealEntries

    init {
        refreshMealEntries()
    }

    fun refreshMealEntries(date: String? = null) {
        viewModelScope.launch {
            val items = withContext(Dispatchers.IO) {
                date?.let { repository.getByDate(it) } ?: repository.getAll()
            }
            _mealEntries.value = items
        }
    }

    fun addMealEntry(entry: MealEntry, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.add(entry)
            refreshMealEntries(entry.date)
            onComplete?.let { withContext(Dispatchers.Main) { it() } }
        }
    }

    fun deleteMealEntry(entry: MealEntry, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(entry.id)
            refreshMealEntries(entry.date)
            onComplete?.let { withContext(Dispatchers.Main) { it() } }
        }
    }
}
