package com.primecut.theprimecut.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.primecut.theprimecut.data.model.WeightLog
import com.primecut.theprimecut.data.repository.WeightLogRepository
import com.primecut.theprimecut.util.AppSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WeightLogViewModel(
    private val repository: WeightLogRepository
) : ViewModel() {

    private val _logs = MutableStateFlow<List<WeightLog>>(emptyList())
    val logs: StateFlow<List<WeightLog>> = _logs

    init {
        refresh()
    }

    fun refresh() {
        loadUserLogs(AppSession.userName)
    }

    fun loadUserLogs(userId: String, showProjection: Boolean = false) {
        viewModelScope.launch {
            val userLogs = withContext(Dispatchers.IO) {
                repository.getUserLogs(userId, showProjection)
            }
            _logs.value = userLogs
        }
    }

    fun addOrUpdateLog(userId: String, date: String, weightLbs: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addOrUpdateLog(userId, date, weightLbs)
            refresh()
        }
    }
}
