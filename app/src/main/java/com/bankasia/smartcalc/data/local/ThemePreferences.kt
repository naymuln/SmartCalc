package com.bankasia.smartcalc.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages user theme preference (System Default, Light, or Dark).
 * Persists the choice in SharedPreferences and provides a reactive StateFlow.
 */
object ThemePreferences {
    private const val PREFS_NAME = "smartcalc_theme_prefs"
    private const val KEY_THEME_MODE = "theme_mode"

    const val MODE_SYSTEM = "system"
    const val MODE_LIGHT = "light"
    const val MODE_DARK = "dark"

    private var prefs: SharedPreferences? = null
    private val _themeMode = MutableStateFlow(MODE_SYSTEM)
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    fun init(context: Context) {
        if (prefs == null) {
            val appCtx = context.applicationContext
            prefs = appCtx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val savedMode = prefs?.getString(KEY_THEME_MODE, MODE_SYSTEM) ?: MODE_SYSTEM
            _themeMode.value = savedMode
        }
    }

    fun setThemeMode(mode: String) {
        _themeMode.value = mode
        prefs?.edit()?.putString(KEY_THEME_MODE, mode)?.apply()
    }

    fun getThemeMode(): String = _themeMode.value
}
