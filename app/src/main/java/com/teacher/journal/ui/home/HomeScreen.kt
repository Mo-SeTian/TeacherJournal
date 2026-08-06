package com.teacher.journal.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teacher.journal.ui.components.*
import com.teacher.journal.ui.home.ReminderItem
import com.teacher.journal.ui.home.ReminderType
import com.teacher.journal.ui.theme.*
import com.teacher.journal.util.DateUtils

@Composable
fun HomeScreen(
    onNavigateToStudentDetail: (Long) -> Unit,
    onNavigateToSessionRecord: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.loadDashboard() }

    val themePreset = com.teacher.journal.ui.theme.ThemePresets[0]
    // 尝试从主题偏好获取当前主题
    val gradientStart = themePreset.gradientStart
    val gradientEnd = themePreset.gradientEnd

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToSessionRecord,
                modifier = Modifier.padding(bottom = 76.dp),
                shape = RoundedCornerShape(18.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp
                )
            ) {
                Icon(Icons.Filled.Add, contentDescription = "记录上课")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 100.dp)
        ) {
            // ── 标题区 ──
            item {
                AppScreenTitle(
                    title = "授业札记",
                    subtitle = "今天 ${DateUtils.formatToday()}"
                )
                Spacer(Modifier.height(16.dp))
            }

            // ── 收入渐变卡片 ──
            item {
                AppGradientCard(
                    gradientStart = gradientStart,
                    gradientEnd = gradientEnd
                ) {
                    Text(
                        "本月收入",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "¥${String.format("%.2f", uiState.monthlyIncome)}",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = (-1).sp
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            // ── 统计卡片 ──
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AppStatCard(
                        icon = Icons.Outlined.People,
                        label = "学生总数",
                        value = "${uiState.studentCount}",
                        accentColor = AppInfo,
                        modifier = Modifier.weight(1f)
                    )
                    AppStatCard(
                        icon = Icons.Outlined.School,
                        label = "剩余课时",
                        value = "${uiState.totalRemainingSessions}",
                        accentColor = AppSuccess,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            // ── 提醒中心 ──
            val hasPendingItems = uiState.monthlyReminders.isNotEmpty()
                    || uiState.unpaidRecords.isNotEmpty()
                    || uiState.lowSessionStudents.isNotEmpty()
                    || uiState.unfinishedSessions.isNotEmpty()

            if (hasPendingItems) {
                item {
                    AppSectionHeader("提醒中心")
                }

                // 月结算 — 未结算 / 未收款
                uiState.monthlyReminders.forEach { reminder ->
                    this@LazyColumn.item {
                        MonthlyReminderCard(
                            reminder = reminder,
                            onClick = { onNavigateToStudentDetail(reminder.studentId) },
                            onMarkPaid = {
                                if (reminder.settlementId > 0) {
                                    viewModel.markSettlementAsPaid(reminder.settlementId)
                                }
                            }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }

                // 按次收费 — 待收费
                uiState.unpaidRecords.forEach { item ->
                    this@LazyColumn.item {
                        PendingCard(
                            studentName = item.studentName,
                            amount = item.record.amount,
                            date = item.record.date,
                            isOverdue = item.isOverdue,
                            type = "按次收费",
                            onClick = {
                                viewModel.markAsPaid(item.record.id)
                            }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }

                // 课时不足（正常学生）
                uiState.lowSessionStudents.forEach { item ->
                    this@LazyColumn.item {
                        LowSessionCard(
                            studentName = item.studentName,
                            remainingSessions = item.remainingSessions,
                            onClick = { onNavigateToStudentDetail(item.studentId) }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }

                // 课时待处理（冻结/不带了仍有剩余课时）
                uiState.unfinishedSessions.forEach { item ->
                    this@LazyColumn.item {
                        UnfinishedSessionsCard(
                            item = item,
                            onRefund = { amount -> viewModel.refundAllSessions(item.studentId, amount) {} },
                            onFreeze = { viewModel.setStudentFrozen(item.studentId) },
                            onResume = { viewModel.setStudentActive(item.studentId) },
                            onClick = { onNavigateToStudentDetail(item.studentId) }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            // ── 最近上课 ──
            item {
                AppSectionHeader("最近上课")
            }

            if (uiState.recentRecords.isEmpty()) {
                item {
                    AppCard {
                        AppEmptyState(
                            icon = Icons.Outlined.EventNote,
                            title = "还没有上课记录",
                            subtitle = "点击右下角 + 记录第一节课"
                        )
                    }
                }
            } else {
                item {
                    AppCard {
                        uiState.recentRecords.forEachIndexed { i, recordItem ->
                            RecentRecordRow(
                                record = recordItem.record,
                                studentName = recordItem.studentName,
                                onClick = { onNavigateToStudentDetail(recordItem.record.studentId) }
                            )
                            if (i < uiState.recentRecords.lastIndex) AppDivider()
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun PendingCard(
    studentName: String,
    amount: Double,
    date: Long,
    isOverdue: Boolean,
    type: String,
    onClick: () -> Unit
) {
    AppCard {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧图标
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isOverdue) AppErrorLight else AppWarningLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Payments,
                    contentDescription = null,
                    tint = if (isOverdue) AppError else AppWarning,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    studentName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "$type · ${DateUtils.formatDateFull(date)}",
                    fontSize = 12.sp,
                    color = if (isOverdue) AppError else AppTextSecondary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "¥${String.format("%.0f", amount)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isOverdue) AppError else AppWarning
                )
                if (isOverdue) {
                    Text(
                        "逾期",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppError
                    )
                }
            }
        }
    }
}

@Composable
private fun LowSessionCard(
    studentName: String,
    remainingSessions: Int,
    onClick: () -> Unit
) {
    AppCard {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (remainingSessions == 0) AppErrorLight else AppWarningLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.School,
                    contentDescription = null,
                    tint = if (remainingSessions == 0) AppError else AppWarning,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    studentName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    if (remainingSessions == 0) "课时已用完" else "仅剩 $remainingSessions 课时",
                    fontSize = 12.sp,
                    color = if (remainingSessions == 0) AppError else AppTextSecondary
                )
            }
            AppPill(
                if (remainingSessions == 0) "已用完" else "余${remainingSessions}次",
                if (remainingSessions == 0) AppError else AppWarning,
                if (remainingSessions == 0) AppErrorLight else AppWarningLight
            )
        }
    }
}

@Composable
private fun RecentRecordRow(
    record: com.teacher.journal.data.entity.SessionRecord,
    studentName: String,
    onClick: () -> Unit
) {
    AppRow(
        title = studentName,
        subtitle = buildString {
            append("${record.startTime}–${record.endTime}")
            if (record.content.isNotBlank()) append(" · ${record.content}")
        },
        leading = {
            AppAvatar(name = studentName, size = 36.dp)
        },
        trailing = {
            when {
                record.amount > 0 -> {
                    Text(
                        "¥${String.format("%.0f", record.amount)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.width(6.dp))
                    AppStatusBadge(record.paymentStatus)
                }
                record.coursePackageId > 0 -> AppPill("扣课时", AppSuccess, AppSuccessLight)
                record.settlementId > 0 -> AppPill("已结算",
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            }
        },
        showChevron = true,
        onClick = onClick
    )
}

@Composable
private fun MonthlyReminderCard(
    reminder: ReminderItem,
    onClick: () -> Unit,
    onMarkPaid: () -> Unit
) {
    val isUnsettled = reminder.type == ReminderType.MONTHLY_UNSETTLED
    val icon = if (isUnsettled) Icons.Outlined.DateRange else Icons.Outlined.Payments
    val bgColor = if (isUnsettled) AppWarningLight else AppErrorLight
    val iconTint = if (isUnsettled) AppWarning else AppError

    AppCard {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    reminder.studentName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    reminder.detail,
                    fontSize = 13.sp,
                    color = AppTextSecondary
                )
            }
            if (isUnsettled) {
                AppPill("未结算", iconTint, bgColor)
            } else {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "¥${String.format("%.0f", reminder.amount)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppError
                    )
                    TextButton(
                        onClick = onMarkPaid,
                        modifier = Modifier.height(28.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp)
                    ) {
                        Text("已收款", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun UnfinishedSessionsCard(
    item: UnfinishedSessionsItem,
    onRefund: (Double) -> Unit,
    onFreeze: () -> Unit,
    onResume: () -> Unit,
    onClick: () -> Unit
) {
    var showRefundDialog by remember { mutableStateOf(false) }
    var refundAmount by remember { mutableStateOf("") }

    AppCard {
        Column(
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AppInfoLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.School,
                        contentDescription = null,
                        tint = AppInfo,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        item.studentName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        if (item.isFrozen) "已冻结 · 还剩 ${item.remainingSessions} 课时"
                        else "不带了 · 还剩 ${item.remainingSessions} 课时",
                        fontSize = 12.sp,
                        color = AppTextSecondary
                    )
                }
                AppPill(
                    if (item.isFrozen) "已冻结" else "已停止",
                    if (item.isFrozen) AppInfo else AppWarning,
                    if (item.isFrozen) AppInfoLight else AppWarningLight
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { showRefundDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppError)
                ) {
                    Icon(Icons.Outlined.Payments, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("退款清零", fontSize = 13.sp)
                }
                if (item.isFrozen) {
                    OutlinedButton(
                        onClick = onResume,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppSuccess)
                    ) {
                        Icon(Icons.Outlined.PlayCircle, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("恢复上课", fontSize = 13.sp)
                    }
                } else {
                    OutlinedButton(
                        onClick = onFreeze,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppInfo)
                    ) {
                        Icon(Icons.Outlined.PauseCircle, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("先冻结", fontSize = 13.sp)
                    }
                }
            }
        }
    }

    if (showRefundDialog) {
        AlertDialog(
            onDismissRequest = { showRefundDialog = false },
            title = { Text("退款清零") },
            text = {
                Column {
                    Text(
                        "${item.studentName} 还剩 ${item.remainingSessions} 课时，退款后将清零且不再提示。",
                        fontSize = 13.sp,
                        color = AppTextSecondary
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = refundAmount,
                        onValueChange = { refundAmount = it },
                        label = { Text("退款金额（元）") },
                        prefix = { Text("¥") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                        ),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val amount = refundAmount.toDoubleOrNull() ?: 0.0
                        showRefundDialog = false
                        onRefund(amount)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = AppError)
                ) { Text("确认退款") }
            },
            dismissButton = { TextButton(onClick = { showRefundDialog = false }) { Text("取消") } }
        )
    }
}