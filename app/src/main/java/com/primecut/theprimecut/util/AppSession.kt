package com.primecut.theprimecut.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AppSession {
    private const val PREFS_NAME = "PrimeCutPrefs"
    private const val KEY_USER_NAME = "userName"

    private lateinit var prefs: SharedPreferences
    private val _userNameFlow = MutableStateFlow("User")
    val userNameFlow: StateFlow<String> = _userNameFlow.asStateFlow()

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val storedName = prefs.getString(KEY_USER_NAME, "User") ?: "User"
        userName = storedName
    }

    var userName: String
        get() = _userNameFlow.value
        set(value) {
            _userNameFlow.value = value
            if (::prefs.isInitialized) {
                prefs.edit().putString(KEY_USER_NAME, value).apply()
            }
        }
}
