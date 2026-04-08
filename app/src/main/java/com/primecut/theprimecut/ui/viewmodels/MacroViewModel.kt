package com.primecut.theprimecut.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.primecut.theprimecut.data.model.MacroSummary
import com.primecut.theprimecut.data.repository.MealEntryRepository
import com.primecut.theprimecut.util.AppSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class MacroViewModel(
    private val repository: MealEntryRepository
) : ViewModel() {

    private val _summary = MutableStateFlow(MacroSummary())
    val summary: StateFlow<MacroSummary> = _summary

    private val _weeklySummaries = MutableStateFlow<Map<String, MacroSummary>>(emptyMap())
    val weeklySummaries: StateFlow<Map<String, MacroSummary>> = _weeklySummaries

    private val _allUsersSummaries = MutableStateFlow<Map<String, MacroSummary>>(emptyMap())
    val allUsersSummaries: StateFlow<Map<String, MacroSummary>> = _allUsersSummaries

    init {
        refresh()
    }

    fun loadSummary(date: String = LocalDate.now().toString()) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.getMacroSummary(date, AppSession.userName)
            _summary.value = result
        }
    }

    fun loadAllUsersSummaries(userNames: List<String>, date: String = LocalDate.now().toString()) {
        viewModelScope.launch(Dispatchers.IO) {
            val summaries = userNames.associateWith { userName ->
                repository.getMacroSummary(date, userName)
            }
            _allUsersSummaries.value = summaries
        }
    }

    fun loadWeeklySummaries() {
        viewModelScope.launch(Dispatchers.IO) {
            val end = LocalDate.now()
            val start = end.minusDays(6)
            val result = repository.getMacroSummariesByDateRange(
                start.toString(),
                end.toString(),
                AppSession.userName
            )
            _weeklySummaries.value = result
        }
    }

    fun refresh() {
        loadSummary()
        loadWeeklySummaries()
    }
}
