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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teacher.journal.data.entity.PaymentStatus
import com.teacher.journal.data.entity.PaymentType
import com.teacher.journal.util.DateUtils
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// ── 设计令牌 ──

val AppBackground = Color(0xFFF6F7F9)
val AppSurface = Color.White
val AppDividerColor = Color(0xFFECEDF0)
val AppTextSecondary = Color(0xFF8A8F99)
val AppTextTertiary = Color(0xFFB9BEC8)
val AppFill = Color(0xFFF1F2F5)
val AppSuccess = Color(0xFF2E9D7A)
val AppSuccessBg = Color(0xFFE6F7F0)
val AppWarning = Color(0xFFE0862E)
val AppWarningBg = Color(0xFFFFF4E6)
val AppError = Color(0xFFE5484D)
val AppErrorBg = Color(0xFFFFEDED)

val AppRadius = 16.dp
val AppRadiusSmall = 12.dp
val AppRadiusXSmall = 8.dp

// ── 顶栏 ──

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(top = 10.dp, bottom = 4.dp)
    ) {
        TopAppBar(
            windowInsets = WindowInsets(0, 0, 0, 0),
            title = {
                Text(
                    title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            navigationIcon = {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            actions = actions,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

// ── 页面大标题 ──

@Composable
fun AppScreenTitle(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp)) {
        Text(
            title,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 36.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (subtitle != null) {
            Spacer(Modifier.height(2.dp))
            Text(subtitle, fontSize = 14.sp, color = AppTextSecondary)
        }
    }
}

// ── 分组标题 ──

@Composable
fun AppSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.3.sp,
            color = AppTextSecondary,
            modifier = Modifier.weight(1f)
        )
        if (action != null) action()
    }
}

// ── 卡片 ──

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadius),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(contentPadding), content = content)
    }
}

// ── 行 ──

@Composable
fun AppRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    showChevron: Boolean = false,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(14.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(
                title,
                fontSize = 16.sp,
                color = titleColor,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    fontSize = 13.sp,
                    color = AppTextSecondary,
                    modifier = Modifier.padding(top = 2.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (trailing != null) trailing()
        if (showChevron) {
            Spacer(Modifier.width(4.dp))
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = AppTextTertiary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun AppDivider(startPadding: Dp = 0.dp) {
    HorizontalDivider(
        modifier = Modifier.padding(start = startPadding),
        thickness = 0.6.dp,
        color = AppDividerColor
    )
}

// ── 徽章 / 标签 ──

@Composable
fun AppPill(text: String, fg: Color, bg: Color) {
    Surface(shape = RoundedCornerShape(7.dp), color = bg) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = fg
        )
    }
}

@Composable
fun AppStatusBadge(status: PaymentStatus) {
    when (status) {
        PaymentStatus.PAID -> AppPill("已收费", AppSuccess, AppSuccessBg)
        PaymentStatus.UNPAID -> AppPill("待收费", AppWarning, AppWarningBg)
    }
}

@Composable
fun AppTypeBadge(paymentType: PaymentType) {
    val (label, fg, bg) = when (paymentType) {
        PaymentType.PREPAID -> Triple("课时包", AppSuccess, AppSuccessBg)
        PaymentType.PER_SESSION -> Triple("按次付", AppWarning, AppWarningBg)
        PaymentType.MONTHLY -> Triple("月结算", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
    }
    AppPill(label, fg, bg)
}

// ── 头像 ──

@Composable
fun AppAvatar(name: String, size: Dp = 44.dp, fontSize: Int = 17) {
    val primary = MaterialTheme.colorScheme.primary
    Box(
        Modifier.size(size).clip(CircleShape).background(primary.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            name.take(1).ifEmpty { "?" },
            fontSize = fontSize.sp,
            fontWeight = FontWeight.SemiBold,
            color = primary
        )
    }
}

// ── 日期徽章 ──

private val WEEK_LABELS = arrayOf("日", "一", "二", "三", "四", "五", "六")

@Composable
fun AppDateBadge(date: Long, size: Dp = 42.dp) {
    val primary = MaterialTheme.colorScheme.primary
    val local = Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDate()
    val day = local.dayOfMonth.toString()
    val week = "周${WEEK_LABELS[local.dayOfWeek.value % 7]}"
    Column(
        Modifier.size(size)
            .clip(RoundedCornerShape(11.dp))
            .background(primary.copy(alpha = 0.1f)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(day, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = primary, lineHeight = 18.sp)
        Text(week, fontSize = 9.sp, fontWeight = FontWeight.Medium, color = primary.copy(alpha = 0.7f), lineHeight = 11.sp)
    }
}

@Composable
fun AppMiniDateBadge(date: Long) {
    val primary = MaterialTheme.colorScheme.primary
    val text = DateUtils.formatDateDisplay(date)
    Box(
        Modifier.size(width = 40.dp, height = 36.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(primary.copy(alpha = 0.08f)),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = primary)
    }
}

// ── 空状态 ──

@Composable
fun AppEmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.size(56.dp).clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(Modifier.height(14.dp))
        Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = AppTextSecondary)
        if (subtitle != null) {
            Spacer(Modifier.height(4.dp))
            Text(subtitle, fontSize = 13.sp, color = AppTextTertiary)
        }
    }
}

// ── 按钮 ──

@Composable
fun AppPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(52.dp),
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        )
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

// ── 输入框 ──

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    isError: Boolean = false,
    supportingText: String? = null,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    prefix: String? = null,
    suffix: String? = null,
    readOnly: Boolean = false,
    trailingIcon: (@Composable () -> Unit)? = null,
    enabled: Boolean = true
) {
    val primary = MaterialTheme.colorScheme.primary
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        readOnly = readOnly,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        isError = isError,
        supportingText = supportingText?.let { { Text(it) } },
        singleLine = singleLine,
        maxLines = maxLines,
        prefix = prefix?.let { { Text(it) } },
        suffix = suffix?.let { { Text(it) } },
        trailingIcon = trailingIcon,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = primary,
            focusedLabelColor = primary,
            cursorColor = primary
        )
    )
}

// ── 搜索框 ──

@Composable
fun AppSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AppFill)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Outlined.Search,
            contentDescription = null,
            tint = AppTextSecondary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(placeholder, fontSize = 15.sp, color = AppTextSecondary)
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

// ── 渐变英雄卡 ──

@Composable
fun AppGradientCard(
    modifier: Modifier = Modifier,
    colors: List<Color>,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            Modifier.background(Brush.linearGradient(colors))
        ) {
            Column(Modifier.padding(contentPadding), content = content)
        }
    }
}

private val dateFormatter = DateTimeFormatter.ofPattern("M月d日 EEEE", Locale.CHINA)

@Composable
fun TodayLabel(): String {
    return Instant.now().atZone(ZoneId.systemDefault()).format(dateFormatter)
}
