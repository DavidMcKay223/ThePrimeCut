package com.primecut.theprimecut.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.primecut.theprimecut.data.model.MacroSummary
import com.primecut.theprimecut.data.repository.MealEntryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class MacroViewModel @Inject constructor(
    private val repository: MealEntryRepository
) : ViewModel() {

    private val _summary = MutableStateFlow(MacroSummary())
    val summary: StateFlow<MacroSummary> = _summary

    private val _weeklySummaries = MutableStateFlow<Map<String, MacroSummary>>(emptyMap())
    val weeklySummaries: StateFlow<Map<String, MacroSummary>> = _weeklySummaries

    fun loadSummary(date: String = LocalDate.now().toString()) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.getMacroSummary(date)
            _summary.value = result
        }
    }

    fun loadWeeklySummaries() {
        viewModelScope.launch(Dispatchers.IO) {
            val end = LocalDate.now()
            val start = end.minusDays(6)
            val result = repository.getMacroSummariesByDateRange(
                start.toString(),
                end.toString()
            )
            _weeklySummaries.value = result
        }
    }

    fun refresh() {
        loadSummary()
        loadWeeklySummaries()
    }
}
