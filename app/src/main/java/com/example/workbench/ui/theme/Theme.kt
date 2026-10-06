package com.example.workbench.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.workbench.model.AppThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = WorkshopDarkPrimary,
    onPrimary = Color(0xFF0F172A),
    primaryContainer = Color(0xFF0C4A6E),
    onPrimaryContainer = Color(0xFFE0F2FE),
    secondary = WorkshopDarkSecondary,
    onSecondary = Color(0xFF042F2E),
    secondaryContainer = Color(0xFF134E4A),
    onSecondaryContainer = Color(0xFFCCFBF1),
    background = WorkshopDarkBg,
    onBackground = WorkshopDarkTextPrimary,
    surface = WorkshopDarkSurface,
    onSurface = WorkshopDarkTextPrimary,
    surfaceVariant = WorkshopDarkSurfaceVariant,
    onSurfaceVariant = WorkshopDarkTextSecondary,
    outline = WorkshopDarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = WorkshopCharcoal,
    onPrimary = Color.White,
    primaryContainer = WorkshopContainerLight,
    onPrimaryContainer = WorkshopCharcoal,
    secondary = WorkshopAccentTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCFBF1),
    onSecondaryContainer = Color(0xFF115E59),
    background = WorkshopBackgroundLight,
    onBackground = WorkshopCharcoal,
    surface = WorkshopSurfaceLight,
    onSurface = WorkshopCharcoal,
    surfaceVariant = WorkshopContainerLight,
    onSurfaceVariant = WorkshopSlateLight,
    outline = WorkshopOutlineLight
)

@Composable
fun WorkBenchTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}