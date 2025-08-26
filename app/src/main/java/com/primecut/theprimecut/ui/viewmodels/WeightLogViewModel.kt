package com.primecut.theprimecut.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.primecut.theprimecut.data.model.WeightLog
import com.primecut.theprimecut.data.repository.WeightLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class WeightLogViewModel @Inject constructor(
    private val repository: WeightLogRepository
) : ViewModel() {

    private val _logs = MutableStateFlow<List<WeightLog>>(emptyList())
    val logs: StateFlow<List<WeightLog>> = _logs

    init {
        loadUserLogs("defaultUser")
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
            val updatedLogs = repository.getUserLogs(userId)
            _logs.value = updatedLogs
        }
    }
}
