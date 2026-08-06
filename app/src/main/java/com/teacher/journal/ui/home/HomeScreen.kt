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
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp)
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

            // ── 待处理提醒 ──
            val hasPendingItems = uiState.unpaidRecords.isNotEmpty()
                    || uiState.unpaidSettlements.isNotEmpty()
                    || uiState.lowSessionStudents.isNotEmpty()

            if (hasPendingItems) {
                item {
                    AppSectionHeader("待处理")
                }

                // 待收费记录
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

                // 未收款结算
                uiState.unpaidSettlements.forEach { item ->
                    this@LazyColumn.item {
                        PendingCard(
                            studentName = item.studentName,
                            amount = item.settlement.totalAmount,
                            date = item.settlement.createdAt,
                            isOverdue = false,
                            type = "月结算 · ${item.settlement.sessionCount}次课",
                            onClick = { onNavigateToStudentDetail(item.settlement.studentId) }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }

                // 课时不足提醒
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