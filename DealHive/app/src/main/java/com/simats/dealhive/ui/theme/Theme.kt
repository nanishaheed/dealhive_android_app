package com.simats.dealhive.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Light mode colors
private val LightBackground = Color(0xFFF8FAFC)  // Slate 50
private val LightSurface = Color(0xFFFFFFFF)  // White
private val LightSurfaceLight = Color(0xFFF1F5F9)  // Slate 100
private val LightTextPrimary = Color(0xFF1E293B)  // Slate 800
private val LightTextSecondary = Color(0xFF64748B)  // Slate 500
private val LightTextMuted = Color(0xFF94A3B8)  // Slate 400
private val LightBorder = Color(0xFFE2E8F0)  // Slate 200

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    secondary = Secondary,
    tertiary = Success,
    background = Background,
    surface = Surface,
    surfaceVariant = SurfaceLight,
    onPrimary = White,
    onSecondary = White,
    onTertiary = White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    error = Error,
    onError = White,
    outline = TextMuted,
    outlineVariant = Border
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    secondary = Secondary,
    tertiary = Success,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceLight,
    onPrimary = White,
    onSecondary = White,
    onTertiary = White,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary,
    onSurfaceVariant = LightTextSecondary,
    error = Error,
    onError = White,
    outline = LightTextMuted,
    outlineVariant = LightBorder
)

@Composable
fun DealHiveTheme(
    isDarkMode: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (isDarkMode) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val bgColor = if (isDarkMode) Background else LightBackground
            window.statusBarColor = bgColor.toArgb()
            window.navigationBarColor = bgColor.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDarkMode
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Export light mode colors for use in Composables when needed
object AppColors {
    @Composable
    fun background(isDark: Boolean) = if (isDark) Background else LightBackground
    
    @Composable
    fun surface(isDark: Boolean) = if (isDark) Surface else LightSurface
    
    @Composable
    fun surfaceLight(isDark: Boolean) = if (isDark) SurfaceLight else LightSurfaceLight
    
    @Composable
    fun textPrimary(isDark: Boolean) = if (isDark) TextPrimary else LightTextPrimary
    
    @Composable
    fun textSecondary(isDark: Boolean) = if (isDark) TextSecondary else LightTextSecondary
    
    @Composable
    fun textMuted(isDark: Boolean) = if (isDark) TextMuted else LightTextMuted
    
    @Composable
    fun border(isDark: Boolean) = if (isDark) Border else LightBorder
}
