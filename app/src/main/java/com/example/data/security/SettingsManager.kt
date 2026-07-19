package com.example.data.security

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("smart_contacts_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_PIN = "security_pin"
        private const val KEY_PIN_ENABLED = "security_pin_enabled"
        private const val KEY_THEME_DARK = "theme_dark"
    }

    var pin: String
        get() = prefs.getString(KEY_PIN, "") ?: ""
        set(value) = prefs.edit().putString(KEY_PIN, value).apply()

    var isPinEnabled: Boolean
        get() = prefs.getBoolean(KEY_PIN_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_PIN_ENABLED, value).apply()

    var isDarkTheme: Boolean?
        get() = if (prefs.contains(KEY_THEME_DARK)) prefs.getBoolean(KEY_THEME_DARK, false) else null
        set(value) {
            if (value == null) {
                prefs.edit().remove(KEY_THEME_DARK).apply()
            } else {
                prefs.edit().putBoolean(KEY_THEME_DARK, value).apply()
            }
        }

    fun verifyPin(input: String): Boolean {
        return pin == input
    }
}
