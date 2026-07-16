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
    background = Gray50,
    onBackground = Gray900,
    surface = SurfaceContainer,
    onSurface = Gray900,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = Gray600,
    outline = Gray300,
    outlineVariant = OutlineVariant
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
            window.statusBarColor = themePreset.primaryDark.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
