package com.teacher.journal.ui.theme

import android.app.Activity
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.teacher.journal.ui.components.AppBackground
import com.teacher.journal.ui.components.AppDividerColor
import com.teacher.journal.ui.components.AppFill
import com.teacher.journal.ui.components.AppTextSecondary

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
    background = AppBackground,
    onBackground = Color(0xFF1C1F26),
    surface = Color.White,
    onSurface = Color(0xFF1C1F26),
    surfaceVariant = AppFill,
    onSurfaceVariant = AppTextSecondary,
    outline = AppDividerColor,
    outlineVariant = AppDividerColor
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
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
