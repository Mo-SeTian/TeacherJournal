package com.teacher.journal.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 预设主题配色 — 授业札记 2.0
 * 每套主题有独立的主色、辅助色、语义色，以及渐变背景色
 */
data class ThemePreset(
    val id: String,
    val name: String,
    val primary: Color,
    val primaryDark: Color,
    val primaryLight: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val secondaryContainer: Color,
    val tertiary: Color,
    val tertiaryContainer: Color,
    val gradientStart: Color,
    val gradientEnd: Color,
    /** 背景色 — 比纯白更柔和 */
    val background: Color,
    /** 卡片底色 */
    val surface: Color,
    /** 表面变体 */
    val surfaceVariant: Color
)

val ThemePresets = listOf(
    ThemePreset(
        id = "periwinkle",
        name = "淡紫蓝",
        primary = Color(0xFF6C5CE7),
        primaryDark = Color(0xFF5A4BD1),
        primaryLight = Color(0xFFA29BFE),
        primaryContainer = Color(0xFFF0EEFF),
        onPrimaryContainer = Color(0xFF3A3290),
        secondary = Color(0xFF00B894),
        secondaryContainer = Color(0xFFE6F9F4),
        tertiary = Color(0xFFFD9644),
        tertiaryContainer = Color(0xFFFFF3E8),
        gradientStart = Color(0xFF6C5CE7),
        gradientEnd = Color(0xFFA29BFE),
        background = Color(0xFFF7F8FC),
        surface = Color.White,
        surfaceVariant = Color(0xFFF2F3F7)
    ),
    ThemePreset(
        id = "mint",
        name = "薄荷绿",
        primary = Color(0xFF00B894),
        primaryDark = Color(0xFF00A381),
        primaryLight = Color(0xFF55EFC4),
        primaryContainer = Color(0xFFE6F9F4),
        onPrimaryContainer = Color(0xFF0D4030),
        secondary = Color(0xFF6C5CE7),
        secondaryContainer = Color(0xFFF0EEFF),
        tertiary = Color(0xFFFDCB6E),
        tertiaryContainer = Color(0xFFFFF8E5),
        gradientStart = Color(0xFF00B894),
        gradientEnd = Color(0xFF55EFC4),
        background = Color(0xFFF7FAF8),
        surface = Color.White,
        surfaceVariant = Color(0xFFF2F7F4)
    ),
    ThemePreset(
        id = "sky",
        name = "天空蓝",
        primary = Color(0xFF4A90D9),
        primaryDark = Color(0xFF3A7BC8),
        primaryLight = Color(0xFF74B9FF),
        primaryContainer = Color(0xFFEBF4FD),
        onPrimaryContainer = Color(0xFF1C4268),
        secondary = Color(0xFF00B894),
        secondaryContainer = Color(0xFFE6F9F4),
        tertiary = Color(0xFFE17055),
        tertiaryContainer = Color(0xFFFFF0ED),
        gradientStart = Color(0xFF4A90D9),
        gradientEnd = Color(0xFF74B9FF),
        background = Color(0xFFF5F8FC),
        surface = Color.White,
        surfaceVariant = Color(0xFFF0F3F8)
    ),
    ThemePreset(
        id = "peach",
        name = "蜜桃橘",
        primary = Color(0xFFE17055),
        primaryDark = Color(0xFFD05A40),
        primaryLight = Color(0xFFFF8A75),
        primaryContainer = Color(0xFFFFF0ED),
        onPrimaryContainer = Color(0xFF6B2518),
        secondary = Color(0xFF6C5CE7),
        secondaryContainer = Color(0xFFF0EEFF),
        tertiary = Color(0xFF00B894),
        tertiaryContainer = Color(0xFFE6F9F4),
        gradientStart = Color(0xFFE17055),
        gradientEnd = Color(0xFFFF8A75),
        background = Color(0xFFFDF7F5),
        surface = Color.White,
        surfaceVariant = Color(0xFFFBF2EF)
    ),
    ThemePreset(
        id = "lavender",
        name = "薰衣草",
        primary = Color(0xFF9B59B6),
        primaryDark = Color(0xFF8E44AD),
        primaryLight = Color(0xFFB07CC6),
        primaryContainer = Color(0xFFF5EEF9),
        onPrimaryContainer = Color(0xFF3D1A52),
        secondary = Color(0xFF00B894),
        secondaryContainer = Color(0xFFE6F9F4),
        tertiary = Color(0xFFF39C12),
        tertiaryContainer = Color(0xFFFFF5E0),
        gradientStart = Color(0xFF9B59B6),
        gradientEnd = Color(0xFFB07CC6),
        background = Color(0xFFF9F6FC),
        surface = Color.White,
        surfaceVariant = Color(0xFFF5F1F8)
    )
)

fun getThemePreset(id: String): ThemePreset =
    ThemePresets.find { it.id == id } ?: ThemePresets[0]