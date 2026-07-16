package com.teacher.journal.ui.theme

import android.app.Activity
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

fun themePresetColorScheme(preset: ThemePreset) = lightColorScheme(
    primary = preset.primary,
    onPrimary = Color.White,
    primaryContainer = preset.primaryContainer,
    onPrimaryContainer = preset.onPrimaryContainer,
    secondary = preset.secondary,
    onSecondary = Color.White,
    secondaryContainer = preset.secondaryContainer,
    onSecondaryContainer = preset.onPrimaryContainer,
    tertiary = preset.tertiary,
    onTertiary = Color.White,
    tertiaryContainer = preset.tertiaryContainer,
    onTertiaryContainer = preset.onPrimaryContainer,
    error = ErrorRed,
    errorContainer = ErrorBg,
    onErrorContainer = ErrorRed,
    background = Color.White,
    onBackground = Gray900,
    surface = Color.White,
    onSurface = Gray900,
    surfaceVariant = Color.White.copy(alpha = 0.9f),
    onSurfaceVariant = Gray600,
    outline = Gray200,
    outlineVariant = Gray100
)

@Composable
fun TeacherJournalTheme(
    themePreset: ThemePreset = ThemePresets[0],
    content: @Composable () -> Unit
) {
    val colorScheme = themePresetColorScheme(themePreset)
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.White.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
