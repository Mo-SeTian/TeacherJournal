package com.teacher.journal.ui.theme

import android.app.Activity
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Blue600,
    onPrimary = OnPrimary,
    primaryContainer = Blue50,
    onPrimaryContainer = Blue800,
    secondary = Green600,
    onSecondary = OnSecondary,
    secondaryContainer = Green50,
    onSecondaryContainer = Green700,
    tertiary = Amber600,
    onTertiary = OnPrimary,
    tertiaryContainer = Amber50,
    onTertiaryContainer = Amber600,
    error = ErrorRed,
    errorContainer = ErrorBg,
    onErrorContainer = ErrorRed,
    background = Gray50,
    onBackground = Gray900,
    surface = SurfaceWhite,
    onSurface = Gray900,
    surfaceVariant = Gray100,
    onSurfaceVariant = Gray600,
    outline = Gray300,
    outlineVariant = Gray200
)

@Composable
fun TeacherJournalTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
