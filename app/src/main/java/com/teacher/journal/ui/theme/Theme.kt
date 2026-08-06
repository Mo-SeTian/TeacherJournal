package com.teacher.journal.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
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
    error = AppError,
    errorContainer = AppErrorLight,
    onErrorContainer = AppError,
    background = preset.background,
    onBackground = Neutral900,
    surface = preset.surface,
    onSurface = Neutral900,
    surfaceVariant = preset.surfaceVariant,
    onSurfaceVariant = Neutral500,
    outline = Neutral200,
    outlineVariant = Neutral100
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
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = true
                isAppearanceLightNavigationBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}