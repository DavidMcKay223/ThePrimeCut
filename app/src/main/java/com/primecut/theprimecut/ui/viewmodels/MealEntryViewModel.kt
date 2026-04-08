package com.primecut.theprimecut.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.primecut.theprimecut.data.model.MealEntry
import com.primecut.theprimecut.data.repository.MealEntryRepository
import com.primecut.theprimecut.util.AppSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MealEntryViewModel(
    private val repository: MealEntryRepository
) : ViewModel() {

    private val _mealEntries = MutableStateFlow<List<MealEntry>>(emptyList())
    val mealEntries: StateFlow<List<MealEntry>> = _mealEntries

    init {
        refreshMealEntries()
    }

    private val _allUsersEntries = MutableStateFlow<Map<String, List<MealEntry>>>(emptyMap())
    val allUsersEntries: StateFlow<Map<String, List<MealEntry>>> = _allUsersEntries

    fun loadAllUsersEntries(date: String, userNames: List<String>) {
        viewModelScope.launch {
            val map = mutableMapOf<String, List<MealEntry>>()
            userNames.forEach { user ->
                val items = withContext(Dispatchers.IO) {
                    repository.getByDate(date, user)
                }
                if (items.isNotEmpty()) {
                    map[user] = items
                }
            }
            _allUsersEntries.value = map
        }
    }

    fun refreshMealEntries(date: String? = null) {
        viewModelScope.launch {
            val items = withContext(Dispatchers.IO) {
                date?.let { repository.getByDate(it, AppSession.userName) } ?: repository.getAll(AppSession.userName)
            }
            _mealEntries.value = items
        }
    }

    fun addMealEntry(entry: MealEntry, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val entryWithUser = entry.copy(id = 0, userName = AppSession.userName)
            repository.add(entryWithUser)
            refreshMealEntries(entryWithUser.date)
            onComplete?.let { withContext(Dispatchers.Main) { it() } }
        }
    }

    fun addMealEntries(entries: List<MealEntry>, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            entries.forEach { entry ->
                repository.add(entry.copy(id = 0, userName = AppSession.userName))
            }
            if (entries.isNotEmpty()) {
                refreshMealEntries(entries.first().date)
            }
            onComplete?.let { withContext(Dispatchers.Main) { it() } }
        }
    }

    fun getEntriesForUser(userName: String, date: String, onResult: (List<MealEntry>) -> Unit) {
        viewModelScope.launch {
            val items = withContext(Dispatchers.IO) {
                repository.getByDate(date, userName)
            }
            onResult(items)
        }
    }

    fun updateMealEntry(entry: MealEntry, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.update(entry)
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
