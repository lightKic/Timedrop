package com.example.timedrop.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import com.example.timedrop.ui.screens.settings.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFA5A5FF), // Lavender
    onPrimary = Color(0xFF000040),
    primaryContainer = Color(0xFF2A2A60),
    onPrimaryContainer = Color(0xFFE0E0FF),
    
    secondary = Color(0xFFD277FF), // Orchid
    onSecondary = Color(0xFF400060),
    secondaryContainer = Color(0xFF4A2A60),
    onSecondaryContainer = Color(0xFFF0E0FF),
    
    tertiary = Color(0xFF679CFF), // Blue
    onTertiary = Color(0xFF002060),
    
    background = Color(0xFF050505),
    onBackground = Color.White,
    
    surface = Color(0xFF111111),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1A1A1A),
    onSurfaceVariant = Color(0xFFADAAAA), // Slate
    
    outline = Color(0xFF454545),
    outlineVariant = Color(0xFF2A2A2A),
    
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6259EF), // More vibrant for light mode
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E0FF),
    onPrimaryContainer = Color(0xFF100060),
    
    secondary = Color(0xFFB548F6),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF0E0FF),
    onSecondaryContainer = Color(0xFF2A0060),
    
    tertiary = Color(0xFF3B64FF),
    onTertiary = Color.White,
    
    background = Color(0xFFF7FBFC),
    onBackground = Color(0xFF0E0E0E),
    
    surface = Color.White,
    onSurface = Color(0xFF0E0E0E),
    surfaceVariant = Color(0xFFEDF2F4),
    onSurfaceVariant = Color(0xFF454545),
    
    outline = Color(0xFF757575),
    outlineVariant = Color(0xFFD1D5D7),
    
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

@Composable
fun TimeDropTheme(
    themeMode: ThemeMode = ThemeMode.System,
    dynamicColor: Boolean = false, // DISABLED BY DEFAULT TO PRESERVE IDENTITY
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.System -> systemDark
        ThemeMode.Dark -> true
        ThemeMode.Light -> false
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
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