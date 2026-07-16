package com.teacher.journal.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 预设主题配色
 */
data class ThemePreset(
    val id: String,
    val name: String,
    val primary: Color,
    val primaryDark: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val secondaryContainer: Color,
    val tertiary: Color,
    val tertiaryContainer: Color
)

val ThemePresets = listOf(
    ThemePreset(
        id = "periwinkle",
        name = "淡紫蓝",
        primary = Color(0xFF5A4EBA),
        primaryDark = Color(0xFF4A40A0),
        primaryContainer = Color(0xFFF0EEFC),
        onPrimaryContainer = Color(0xFF3B3485),
        secondary = Color(0xFF4AA370),
        secondaryContainer = Color(0xFFEFFAF4),
        tertiary = Color(0xFFE8836A),
        tertiaryContainer = Color(0xFFFFF4F0)
    ),
    ThemePreset(
        id = "mint",
        name = "薄荷绿",
        primary = Color(0xFF2E9D7A),
        primaryDark = Color(0xFF1E7A5E),
        primaryContainer = Color(0xFFE6F7F0),
        onPrimaryContainer = Color(0xFF0D3B2C),
        secondary = Color(0xFF6389CC),
        secondaryContainer = Color(0xFFEEF2FA),
        tertiary = Color(0xFFD4865A),
        tertiaryContainer = Color(0xFFFFF2EB)
    ),
    ThemePreset(
        id = "sky",
        name = "天空蓝",
        primary = Color(0xFF4D8FCC),
        primaryDark = Color(0xFF3670AA),
        primaryContainer = Color(0xFFE3F0FD),
        onPrimaryContainer = Color(0xFF1A3D60),
        secondary = Color(0xFF5C9988),
        secondaryContainer = Color(0xFFEDF6F3),
        tertiary = Color(0xFFD4926B),
        tertiaryContainer = Color(0xFFFFF4EE)
    ),
    ThemePreset(
        id = "peach",
        name = "蜜桃橘",
        primary = Color(0xFFE0886A),
        primaryDark = Color(0xFFC06A4E),
        primaryContainer = Color(0xFFFFF0EB),
        onPrimaryContainer = Color(0xFF5C2A1A),
        secondary = Color(0xFF5E9C8A),
        secondaryContainer = Color(0xFFECF7F3),
        tertiary = Color(0xFF7B8CC0),
        tertiaryContainer = Color(0xFFEDEFF9)
    ),
    ThemePreset(
        id = "lavender",
        name = "薰衣草",
        primary = Color(0xFF8E7CC0),
        primaryDark = Color(0xFF6E5CA0),
        primaryContainer = Color(0xFFF3F0FB),
        onPrimaryContainer = Color(0xFF352A55),
        secondary = Color(0xFF6DA882),
        secondaryContainer = Color(0xFFEDF6F0),
        tertiary = Color(0xFFD4877E),
        tertiaryContainer = Color(0xFFFFF2F0)
    )
)

fun getThemePreset(id: String): ThemePreset = ThemePresets.find { it.id == id } ?: ThemePresets[0]
