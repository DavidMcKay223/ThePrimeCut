package com.primecut.theprimecut.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.primecut.theprimecut.data.model.UserProfile
import com.primecut.theprimecut.data.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import com.primecut.theprimecut.util.AppSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val repository: UserProfileRepository
) : ViewModel() {

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> get() = _userProfile

    init {
        loadProfile(AppSession.userName)
    }

    fun loadProfile(userName: String) {
        viewModelScope.launch {
            val profile = withContext(Dispatchers.IO) { repository.getUserProfile(userName) }
            _userProfile.value = profile
        }
    }

    fun saveProfile(profile: UserProfile, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveUserProfile(profile)
            onComplete?.let { callback ->
                launch(Dispatchers.Main) { callback() }
            }
        }
    }

    fun recalcGoals(userName: String, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateUserGoals(userName)
            val updatedProfile = repository.getUserProfile(userName)
            _userProfile.value = updatedProfile
            onComplete?.let { callback ->
                launch(Dispatchers.Main) { callback() }
            }
        }
    }
}
