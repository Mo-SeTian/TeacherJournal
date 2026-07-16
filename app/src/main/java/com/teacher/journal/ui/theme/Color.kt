package com.teacher.journal.ui.theme

import androidx.compose.ui.graphics.Color

// === 主色调：稳重蓝 ===
val Blue50 = Color(0xFFE8F0FE)
val Blue100 = Color(0xFFC5D9F8)
val Blue200 = Color(0xFF9EBFF2)
val Blue400 = Color(0xFF5A8FDB)
val Blue500 = Color(0xFF4A6FA5)
val Blue600 = Color(0xFF3B5A8A)
val Blue700 = Color(0xFF2C4570)
val Blue800 = Color(0xFF1E3056)
val Blue900 = Color(0xFF101B3C)

// === 辅助色：沉稳绿 ===
val Green50 = Color(0xFFEBF5EC)
val Green100 = Color(0xFFC8E6C9)
val Green500 = Color(0xFF5B8C5A)
val Green600 = Color(0xFF4A7349)
val Green700 = Color(0xFF3A5B39)

// === 强调色：温暖琥珀 ===
val Amber50 = Color(0xFFFFF8E1)
val Amber100 = Color(0xFFFFECB3)
val Amber400 = Color(0xFFFFCA28)
val Amber500 = Color(0xFFE07B39)
val Amber600 = Color(0xFFC56A2E)

// === 功能色 ===
val SuccessGreen = Color(0xFF43A047)
val SuccessBg = Color(0xFFE8F5E9)
val WarningOrange = Color(0xFFEF6C00)
val WarningBg = Color(0xFFFFF3E0)
val ErrorRed = Color(0xFFE53935)
val ErrorBg = Color(0xFFFFEBEE)
val InfoBlue = Color(0xFF1E88E5)
val InfoBg = Color(0xFFE3F2FD)

// === 中性色 ===
val Gray50 = Color(0xFFF8F9FC)
val Gray100 = Color(0xFFF0F2F8)
val Gray200 = Color(0xFFE4E7ED)
val Gray300 = Color(0xFFCDD1D9)
val Gray400 = Color(0xFFA8ADB8)
val Gray500 = Color(0xFF7B808C)
val Gray600 = Color(0xFF595E6A)
val Gray700 = Color(0xFF40444D)
val Gray800 = Color(0xFF2A2D35)
val Gray900 = Color(0xFF1A1C1E)

// === Surface ===
val SurfaceWhite = Color.White
val SurfaceDefault = Color(0xFFF8F9FC)
val SurfaceCard = Color.White

// === 兼容旧代码别名 ===
val Primary = Blue500
val PrimaryLight = Blue200
val PrimaryDark = Blue700
val OnPrimary = Color.White
val Secondary = Green500
val SecondaryLight = Green100
val SecondaryDark = Green700
val OnSecondary = Color.White
val Tertiary = Amber500
val TertiaryLight = Amber100
val TertiaryDark = Amber600
val Background = SurfaceDefault
val Surface = SurfaceWhite
val SurfaceVariant = Gray100
val TextPrimary = Gray900
val TextSecondary = Gray600
val TextTertiary = Gray400
val StatusPaid = SuccessGreen
val StatusUnpaid = WarningOrange
val StatusOverdue = ErrorRed
