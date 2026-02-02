package com.simats.dealhive.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Theme mode options
 */
enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

/**
 * Theme Manager for persisting and managing theme preferences
 */
class ThemeManager private constructor(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREFS_NAME = "dealhive_theme_prefs"
        private const val KEY_THEME_MODE = "theme_mode"
        
        @Volatile
        private var instance: ThemeManager? = null
        
        fun getInstance(context: Context): ThemeManager {
            return instance ?: synchronized(this) {
                instance ?: ThemeManager(context.applicationContext).also { instance = it }
            }
        }
    }
    
    /**
     * Get the current theme mode
     */
    fun getThemeMode(): ThemeMode {
        val value = prefs.getString(KEY_THEME_MODE, ThemeMode.DARK.name)
        return try {
            ThemeMode.valueOf(value ?: ThemeMode.DARK.name)
        } catch (e: Exception) {
            ThemeMode.DARK
        }
    }
    
    /**
     * Set the theme mode
     */
    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
    }
    
    /**
     * Check if dark mode is enabled
     */
    fun isDarkMode(): Boolean {
        return getThemeMode() == ThemeMode.DARK
    }
    
    /**
     * Toggle between light and dark mode
     */
    fun toggleDarkMode() {
        val newMode = if (isDarkMode()) ThemeMode.LIGHT else ThemeMode.DARK
        setThemeMode(newMode)
    }
}

/**
 * Composable state holder for theme
 */
object ThemeState {
    private var _isDarkMode: MutableState<Boolean>? = null
    
    @Composable
    fun rememberThemeState(): MutableState<Boolean> {
        val context = LocalContext.current
        val themeManager = ThemeManager.getInstance(context)
        
        return remember {
            _isDarkMode ?: mutableStateOf(themeManager.isDarkMode()).also { _isDarkMode = it }
        }
    }
    
    fun updateTheme(isDark: Boolean, context: Context) {
        _isDarkMode?.value = isDark
        ThemeManager.getInstance(context).setThemeMode(
            if (isDark) ThemeMode.DARK else ThemeMode.LIGHT
        )
    }
}
