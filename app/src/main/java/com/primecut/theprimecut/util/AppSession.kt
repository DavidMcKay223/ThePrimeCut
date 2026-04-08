package com.primecut.theprimecut.util

import android.content.Context
import android.content.SharedPreferences

object AppSession {
    private const val PREFS_NAME = "PrimeCutPrefs"
    private const val KEY_USER_NAME = "userName"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        userName = prefs.getString(KEY_USER_NAME, "User") ?: "User"
    }

    var userName: String = "User"
        set(value) {
            field = value
            if (::prefs.isInitialized) {
                prefs.edit().putString(KEY_USER_NAME, value).apply()
            }
        }
}
