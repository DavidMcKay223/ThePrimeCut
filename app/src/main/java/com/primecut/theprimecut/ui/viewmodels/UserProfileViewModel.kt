package com.primecut.theprimecut.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.primecut.theprimecut.data.model.UserProfile
import com.primecut.theprimecut.data.repository.UserProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.primecut.theprimecut.util.AppSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserProfileViewModel(
    private val repository: UserProfileRepository
) : ViewModel() {

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> get() = _userProfile

    private val _allUserNames = MutableStateFlow<List<String>>(emptyList())
    val allUserNames: StateFlow<List<String>> get() = _allUserNames

    private val _allProfiles = MutableStateFlow<List<UserProfile>>(emptyList())
    val allProfiles: StateFlow<List<UserProfile>> get() = _allProfiles

    var onUserSwitched: (() -> Unit)? = null

    init {
        loadProfile(AppSession.userName)
        refreshAllProfiles()
    }

    fun refreshAllProfiles() {
        viewModelScope.launch {
            val names = withContext(Dispatchers.IO) { repository.getAllUserNames() }
            _allUserNames.value = names
            val profiles = withContext(Dispatchers.IO) { repository.getAllProfiles() }
            _allProfiles.value = profiles
        }
    }

    fun loadAllUserNames() {
        refreshAllProfiles()
    }

    fun loadProfile(userName: String) {
        viewModelScope.launch {
            val profile = withContext(Dispatchers.IO) { repository.getUserProfile(userName) }
            _userProfile.value = profile
            AppSession.userName = userName // Update session
            
            withContext(Dispatchers.Main) {
                onUserSwitched?.invoke()
            }
        }
    }

    fun saveProfile(profile: UserProfile, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveUserProfile(profile)
            loadAllUserNames() // Refresh list
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
