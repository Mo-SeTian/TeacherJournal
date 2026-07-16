package com.teacher.journal.ui.theme

import androidx.compose.ui.graphics.Color

// === 主色调：柔和蓝（对标书迹风格，温暖不严肃）===
val Blue50 = Color(0xFFE8F1FB)
val Blue100 = Color(0xFFC5D9F4)
val Blue200 = Color(0xFF9DBFEB)
val Blue400 = Color(0xFF5D94D9)
val Blue500 = Color(0xFF4A85CC)
val Blue600 = Color(0xFF3B6FAE)
val Blue700 = Color(0xFF2D5A92)
val Blue800 = Color(0xFF1F4576)
val Blue900 = Color(0xFF12305A)

// === 辅助色：柔和绿 ===
val Green50 = Color(0xFFEBF5EC)
val Green100 = Color(0xFFC8E6C9)
val Green500 = Color(0xFF4CAF50)
val Green600 = Color(0xFF43A047)
val Green700 = Color(0xFF388E3C)

// === 强调色：暖橙 ===
val Amber50 = Color(0xFFFFF8E1)
val Amber100 = Color(0xFFFFECB3)
val Amber400 = Color(0xFFFFB74D)
val Amber500 = Color(0xFFFFA726)
val Amber600 = Color(0xFFFB8C00)

// === 功能色 ===
val SuccessGreen = Color(0xFF4CAF50)
val SuccessBg = Color(0xFFE8F5E9)
val WarningOrange = Color(0xFFFF9800)
val WarningBg = Color(0xFFFFF3E0)
val ErrorRed = Color(0xFFF44336)
val ErrorBg = Color(0xFFFFEBEE)

// === 中性色 ===
val Gray50 = Color(0xFFFAFAFA)
val Gray100 = Color(0xFFF5F5F5)
val Gray200 = Color(0xFFEEEEEE)
val Gray300 = Color(0xFFE0E0E0)
val Gray400 = Color(0xFFBDBDBD)
val Gray500 = Color(0xFF9E9E9E)
val Gray600 = Color(0xFF757575)
val Gray700 = Color(0xFF424242)
val Gray800 = Color(0xFF303030)
val Gray900 = Color(0xFF212121)

// === Material 3 Container ===
val PrimaryContainer = Blue50
val OnPrimaryContainer = Blue800
val SecondaryContainer = Green50
val OnSecondaryContainer = Green700
val TertiaryContainer = Amber50
val OnTertiaryContainer = Amber600
val SurfaceContainer = Color.White
val SurfaceVariant = Gray100
val OutlineVariant = Gray200

// === 兼容别名 ===
val Primary = Blue600
val PrimaryLight = Blue200
val PrimaryDark = Blue700
val OnPrimary = Color.White
val Secondary = Green600
val OnSecondary = Color.White
val Tertiary = Amber600
val Background = Gray50
val Surface = SurfaceContainer
val SurfaceWhite = SurfaceContainer
val TextPrimary = Gray900
val TextSecondary = Gray600
val TextTertiary = Gray500
val StatusPaid = SuccessGreen
val StatusUnpaid = WarningOrange
val StatusOverdue = ErrorRed
