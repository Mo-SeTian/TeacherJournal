package com.teacher.journal.ui.theme

import androidx.compose.ui.graphics.Color

// === 虹彩清新主题 ===

// 主色：淡紫蓝 Periwinkle
val Primary50 = Color(0xFFF0EEFC)
val Primary100 = Color(0xFFDDD9F7)
val Primary200 = Color(0xFFC5BEF0)
val Primary400 = Color(0xFF8E82E0)
val Primary500 = Color(0xFF6E61D0)
val Primary600 = Color(0xFF5A4EBA)
val Primary700 = Color(0xFF4A40A0)
val Primary800 = Color(0xFF3B3485)

// 辅助：薄荷绿
val Green50 = Color(0xFFEFFAF4)
val Green100 = Color(0xFFD5F2E4)
val Green500 = Color(0xFF5CB882)
val Green600 = Color(0xFF4AA370)
val Green700 = Color(0xFF39855A)

// 强调：蜜桃色
val Peach50 = Color(0xFFFFF4F0)
val Peach100 = Color(0xFFFFE3D6)
val Peach500 = Color(0xFFF09B80)
val Peach600 = Color(0xFFE8836A)

// 虹彩渐变色
val IridescentPink = Color(0xFFF5E0EB)
val IridescentBlue = Color(0xFFE0E8F5)
val IridescentMint = Color(0xFFE0F2ED)
val IridescentLavender = Color(0xFFECE4F5)
val IridescentPeach = Color(0xFFF5EBE0)

// 功能色
val SuccessGreen = Color(0xFF5CB882)
val SuccessBg = Color(0xFFEFFAF4)
val WarningOrange = Color(0xFFF09B50)
val WarningBg = Color(0xFFFFF4EC)
val ErrorRed = Color(0xFFE07B7B)
val ErrorBg = Color(0xFFFFF0F0)

// 中性色
val Gray50 = Color(0xFFFAFAFC)
val Gray100 = Color(0xFFF4F3F8)
val Gray200 = Color(0xFFEBEAF0)
val Gray300 = Color(0xFFDCDBE5)
val Gray400 = Color(0xFFC0BFC9)
val Gray500 = Color(0xFF9D9BA8)
val Gray600 = Color(0xFF747280)
val Gray700 = Color(0xFF504E5C)
val Gray800 = Color(0xFF33313D)
val Gray900 = Color(0xFF1C1B24)

// === Material 3 Container ===
val PrimaryContainer = Primary50
val OnPrimaryContainer = Primary800
val SecondaryContainer = Green50
val OnSecondaryContainer = Green700
val TertiaryContainer = Peach50
val OnTertiaryContainer = Peach600
val SurfaceContainer = Color.White
val SurfaceVariant = Gray100
val OutlineVariant = Gray200

// === 兼容别名 ===
val Primary = Primary600
val PrimaryLight = Primary200
val PrimaryDark = Primary700
val OnPrimary = Color.White
val Secondary = Green600
val OnSecondary = Color.White
val Tertiary = Peach600
val Background = Gray50
val Surface = SurfaceContainer
val SurfaceWhite = SurfaceContainer

// 保留旧别名兼容
val Blue50 = Primary50
val Blue100 = Primary100
val Blue200 = Primary200
val Blue500 = Primary500
val Blue600 = Primary600
val Blue700 = Primary700
val Blue800 = Primary800
val Amber50 = Peach50
val Amber100 = Peach100
val Amber500 = Peach500
val Amber600 = Peach600
val TextPrimary = Gray900
val TextSecondary = Gray600
val TextTertiary = Gray500
val StatusPaid = SuccessGreen
val StatusUnpaid = WarningOrange
val StatusOverdue = ErrorRed
