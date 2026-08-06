package com.teacher.journal.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teacher.journal.data.entity.PaymentStatus
import com.teacher.journal.data.entity.PaymentType
import com.teacher.journal.ui.theme.*
import com.teacher.journal.util.DateUtils
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// ═══════════════════════════════════════════
// 设计令牌
// ═══════════════════════════════════════════

val AppBackground = Neutral50
val AppSurface = Neutral0
val AppDividerColor = Neutral200
val AppTextSecondary = Neutral500
val AppTextTertiary = Neutral400
val AppFill = Neutral100
val CardRadius = 20.dp
val ButtonRadius = 16.dp
val InputRadius = 14.dp
val ChipRadius = 10.dp

// ═══════════════════════════════════════════
// 顶栏 — 二级页面返回 + 标题
// ═══════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = MaterialTheme.colorScheme.background
        )
    )
}

// ═══════════════════════════════════════════
// 页面标题
// ═══════════════════════════════════════════

@Composable
fun AppScreenTitle(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(vertical = 4.dp)) {
        Text(
            title,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            letterSpacing = (-0.5).sp
        )
        if (subtitle != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                subtitle,
                fontSize = 14.sp,
                color = AppTextSecondary,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

// ═══════════════════════════════════════════
// 段落标题
// ═══════════════════════════════════════════

@Composable
fun AppSectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        title,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = Neutral500,
        letterSpacing = 0.5.sp,
        modifier = modifier.padding(top = 24.dp, bottom = 10.dp, start = 4.dp)
    )
}

// ═══════════════════════════════════════════
// 卡片
// ═══════════════════════════════════════════

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CardRadius),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        tonalElevation = 0.dp
    ) {
        Column(content = content)
    }
}

// ═══════════════════════════════════════════
// 渐变卡片 — 用于收入/统计展示
// ═══════════════════════════════════════════

@Composable
fun AppGradientCard(
    gradientStart: Color,
    gradientEnd: Color,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CardRadius),
        shadowElevation = 4.dp,
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(gradientStart, gradientEnd)
                    ),
                    RoundedCornerShape(CardRadius)
                )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                content = content
            )
        }
    }
}

// ═══════════════════════════════════════════
// 统计小卡片
// ═══════════════════════════════════════════

@Composable
fun AppStatCard(
    icon: ImageVector,
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(16.dp),
        color = accentColor.copy(alpha = 0.06f),
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                letterSpacing = (-0.3).sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                label,
                fontSize = 12.sp,
                color = Neutral500,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ═══════════════════════════════════════════
// 行 — 可点击的信息行
// ═══════════════════════════════════════════

@Composable
fun AppRow(
    title: String,
    subtitle: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    showChevron: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        color = Color.Transparent
    ) {
        Row(
            Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leading != null) {
                leading()
                Spacer(Modifier.width(14.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        subtitle,
                        fontSize = 13.sp,
                        color = AppTextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (trailing != null) {
                Spacer(Modifier.width(12.dp))
                trailing()
            }
            if (showChevron) {
                Spacer(Modifier.width(6.dp))
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = AppTextTertiary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════
// 分隔线
// ═══════════════════════════════════════════

@Composable
fun AppDivider() {
    HorizontalDivider(
        color = AppDividerColor,
        thickness = 0.5.dp,
        modifier = Modifier.padding(horizontal = 18.dp)
    )
}

// ═══════════════════════════════════════════
// 徽章 — 文本标签
// ═══════════════════════════════════════════

@Composable
fun AppPill(
    label: String,
    color: Color,
    bgColor: Color
) {
    Surface(
        shape = RoundedCornerShape(ChipRadius),
        color = bgColor
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = color,
            letterSpacing = 0.3.sp
        )
    }
}

// ═══════════════════════════════════════════
// 状态徽章
// ═══════════════════════════════════════════

@Composable
fun AppStatusBadge(status: PaymentStatus) {
    val (color, bg, label) = when (status) {
        PaymentStatus.PAID -> Triple(AppSuccess, AppSuccessLight, "已收费")
        PaymentStatus.UNPAID -> Triple(AppWarning, AppWarningLight, "待收费")
    }
    AppPill(label, color, bg)
}

// ═══════════════════════════════════════════
// 付费类型徽章
// ═══════════════════════════════════════════

@Composable
fun AppTypeBadge(type: PaymentType) {
    val (color, bg, label) = when (type) {
        PaymentType.PREPAID -> Triple(AppSuccess, AppSuccessLight, "课时包")
        PaymentType.PER_SESSION -> Triple(AppWarning, AppWarningLight, "按次付")
        PaymentType.MONTHLY -> Triple(AppInfo, AppInfoLight, "月结算")
    }
    AppPill(label, color, bg)
}

// ═══════════════════════════════════════════
// 头像
// ═══════════════════════════════════════════

@Composable
fun AppAvatar(
    name: String,
    size: Dp = 44.dp,
    modifier: Modifier = Modifier
) {
    val initial = name.firstOrNull()?.toString()?.uppercase() ?: "?"
    val colors = listOf(
        Color(0xFF6C5CE7), Color(0xFF00B894), Color(0xFF4A90D9),
        Color(0xFFE17055), Color(0xFF9B59B6), Color(0xFFFDCB6E)
    )
    val color = colors[name.hashCode().mod(colors.size).let { if (it < 0) it + colors.size else it }]

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(
            initial,
            fontSize = (size.value * 0.4f).sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

// ═══════════════════════════════════════════
// 空状态
// ═══════════════════════════════════════════

@Composable
fun AppEmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Neutral100),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Neutral300,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            title,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Neutral700
        )
        if (subtitle != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                subtitle,
                fontSize = 13.sp,
                color = AppTextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ═══════════════════════════════════════════
// 主按钮
// ═══════════════════════════════════════════

@Composable
fun AppPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(54.dp),
        shape = RoundedCornerShape(ButtonRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        ),
        enabled = enabled,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        )
    ) {
        Text(
            text,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ═══════════════════════════════════════════
// 文本输入框
// ═══════════════════════════════════════════

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    prefix: String? = null,
    suffix: String? = null,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    readOnly: Boolean = false,
    isError: Boolean = false,
    supportingText: String? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier) {
        Text(
            label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (isError) AppError else Neutral600,
            modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
        )

        Surface(
            shape = RoundedCornerShape(InputRadius),
            color = if (readOnly) Neutral100 else Neutral50,
            border = if (isError)
                androidx.compose.foundation.BorderStroke(1.5.dp, AppError)
            else
                androidx.compose.foundation.BorderStroke(1.dp, Neutral200)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = if (singleLine) 14.dp else 12.dp),
                verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top
            ) {
                if (prefix != null) {
                    Text(
                        prefix,
                        fontSize = 15.sp,
                        color = if (value.isNotEmpty()) MaterialTheme.colorScheme.onSurface else AppTextTertiary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
                Box(Modifier.weight(1f)) {
                    if (value.isEmpty() && placeholder.isNotEmpty()) {
                        Text(
                            placeholder,
                            fontSize = 15.sp,
                            color = AppTextTertiary
                        )
                    }
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        readOnly = readOnly,
                        singleLine = singleLine,
                        maxLines = maxLines,
                        textStyle = TextStyle(
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 22.sp
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (suffix != null) {
                    Text(
                        suffix,
                        fontSize = 15.sp,
                        color = AppTextSecondary,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                if (trailingIcon != null) {
                    Spacer(Modifier.width(4.dp))
                    trailingIcon()
                }
            }
        }

        if (supportingText != null) {
            Text(
                supportingText,
                fontSize = 12.sp,
                color = if (isError) AppError else AppTextSecondary,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}

// ═══════════════════════════════════════════
// 搜索框
// ═══════════════════════════════════════════

@Composable
fun AppSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "搜索",
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(InputRadius),
        color = Neutral100,
        border = androidx.compose.foundation.BorderStroke(1.dp, Neutral200)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.Search,
                contentDescription = null,
                tint = AppTextTertiary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Box(Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(placeholder, fontSize = 15.sp, color = AppTextTertiary)
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ═══════════════════════════════════════════
// 进度条
// ═══════════════════════════════════════════

@Composable
fun AppProgressBar(
    progress: Float,
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
    height: Dp = 6.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(Neutral100)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(RoundedCornerShape(height / 2))
                .background(color)
        )
    }
}

// ═══════════════════════════════════════════
// 日期徽章
// ═══════════════════════════════════════════

@Composable
fun AppDateBadge(
    timestamp: Long,
    modifier: Modifier = Modifier
) {
    val localDate = Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault()).toLocalDate()
    val today = java.time.LocalDate.now()
    val label = when {
        localDate == today -> "今天"
        localDate == today.minusDays(1) -> "昨天"
        localDate == today.plusDays(1) -> "明天"
        localDate.year == today.year -> localDate.format(
            DateTimeFormatter.ofPattern("M月d日", Locale.CHINESE)
        )
        else -> localDate.format(
            DateTimeFormatter.ofPattern("yyyy/M/d", Locale.CHINESE)
        )
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Neutral100
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Neutral500
        )
    }
}