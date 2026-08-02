package com.teacher.journal.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teacher.journal.ui.components.*
import com.teacher.journal.util.DateUtils

@Composable
fun HomeScreen(
    onNavigateToStudentDetail: (Long) -> Unit,
    onNavigateToSessionRecord: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.loadDashboard() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToSessionRecord,
                shape = RoundedCornerShape(18.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "记录上课")
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Scaffold
        }

        val alertCount = uiState.unpaidRecords.size + uiState.unpaidSettlements.size

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(top = 32.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 100.dp)
        ) {
            item {
                AppScreenTitle(title = "今日", subtitle = TodayLabel())
                Spacer(Modifier.height(14.dp))
            }

            // 收入主卡
            item {
                HeroIncomeCard(monthlyIncome = uiState.monthlyIncome)
            }

            // 统计小部件
            item {
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile(
                        icon = Icons.Outlined.People,
                        color = MaterialTheme.colorScheme.primary,
                        label = "学生",
                        value = "${uiState.studentCount}",
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        icon = Icons.Outlined.EventNote,
                        color = AppSuccess,
                        label = "剩余课时",
                        value = "${uiState.totalRemainingSessions}",
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        icon = Icons.Outlined.Payments,
                        color = if (alertCount > 0) AppWarning else AppSuccess,
                        label = "待收款",
                        value = "$alertCount",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 待处理
            val hasAlerts = uiState.unpaidRecords.isNotEmpty() ||
                    uiState.unpaidSettlements.isNotEmpty() ||
                    uiState.lowSessionStudents.isNotEmpty()
            if (hasAlerts) {
                item {
                    AppSectionHeader(
                        "待处理",
                        action = {
                            val total = uiState.unpaidRecords.size +
                                    uiState.unpaidSettlements.size +
                                    uiState.lowSessionStudents.size
                            AppPill("$total 项", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
                        }
                    )
                }
                item {
                    AppCard {
                        uiState.unpaidRecords.forEachIndexed { i, item ->
                            AlertRow(
                                title = item.studentName,
                                subtitle = "${DateUtils.formatDateFull(item.record.date)} · 待收 ¥${fmt(item.record.amount)}",
                                icon = Icons.Outlined.Payments,
                                color = if (item.isOverdue) AppError else AppWarning,
                                tag = if (item.isOverdue) "逾期" else null,
                                onClick = { onNavigateToStudentDetail(item.record.studentId) }
                            )
                            if (i < uiState.unpaidRecords.lastIndex) AppDivider()
                        }
                        uiState.unpaidSettlements.forEachIndexed { i, item ->
                            AlertRow(
                                title = item.studentName,
                                subtitle = "${item.settlement.year}年${item.settlement.month + 1}月 · 待收 ¥${fmt(item.settlement.totalAmount)}",
                                icon = Icons.Outlined.DateRange,
                                color = AppWarning,
                                tag = "月结算",
                                onClick = { onNavigateToStudentDetail(item.settlement.studentId) }
                            )
                            if (i < uiState.unpaidSettlements.lastIndex) AppDivider()
                        }
                        uiState.lowSessionStudents.forEachIndexed { i, item ->
                            val empty = item.remainingSessions == 0
                            AlertRow(
                                title = item.studentName,
                                subtitle = if (empty) "课时已用完，续购后继续上课" else "剩余 ${item.remainingSessions} 次课时",
                                icon = Icons.Outlined.School,
                                color = if (empty) AppError else AppWarning,
                                tag = if (empty) "已用完" else "补课时",
                                onClick = { onNavigateToStudentDetail(item.studentId) }
                            )
                            if (i < uiState.lowSessionStudents.lastIndex) AppDivider()
                        }
                    }
                }
            }

            // 最近上课
            item { AppSectionHeader("最近上课") }
            if (uiState.recentRecords.isEmpty()) {
                item {
                    AppCard {
                        AppEmptyState(
                            icon = Icons.Outlined.EventNote,
                            title = "暂无上课记录",
                            subtitle = "点击右下角 + 记录第一堂课"
                        )
                    }
                }
            } else {
                item {
                    AppCard {
                        uiState.recentRecords.forEachIndexed { i, item ->
                            RecentSessionRow(item) {
                                onNavigateToStudentDetail(item.record.studentId)
                            }
                            if (i < uiState.recentRecords.lastIndex) AppDivider()
                        }
                    }
                }
            }
        }
    }
}

private fun fmt(v: Double): String = String.format("%.0f", v)

// ── 收入主卡 ──

@Composable
private fun HeroIncomeCard(monthlyIncome: Double) {
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    AppGradientCard(colors = listOf(primary, tertiary.copy(alpha = 0.85f))) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    "本月收入",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("¥", fontSize = 20.sp, fontWeight = FontWeight.SemiBold,
                        color = Color.White, modifier = Modifier.padding(bottom = 7.dp))
                    Text(
                        String.format("%,.0f", monthlyIncome),
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = 46.sp
                    )
                }
            }
            Box(
                Modifier.size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Payments,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.22f), thickness = 0.7.dp)
        Spacer(Modifier.height(12.dp))
        Text(
            "包含课时包购买、课时费与月结算收入",
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.75f)
        )
    }
}

// ── 统计小部件 ──

@Composable
private fun StatTile(
    icon: ImageVector,
    color: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    AppCard(modifier = modifier) {
        Column(Modifier.padding(14.dp)) {
            Box(
                Modifier.size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(2.dp))
            Text(label, fontSize = 11.sp, color = AppTextSecondary)
        }
    }
}

// ── 待处理行 ──

@Composable
private fun AlertRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    tag: String?,
    onClick: () -> Unit
) {
    AppRow(
        title = title,
        subtitle = subtitle,
        leading = {
            Box(
                Modifier.size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(17.dp))
            }
        },
        trailing = {
            tag?.let { AppPill(it, color, color.copy(alpha = 0.12f)) }
        },
        showChevron = true,
        onClick = onClick
    )
}

// ── 最近上课行 ──

@Composable
private fun RecentSessionRow(item: RecentRecordItem, onClick: () -> Unit) {
    AppRow(
        title = item.studentName,
        subtitle = buildString {
            append("${DateUtils.formatDayMonth(item.record.date)} ${item.record.startTime}–${item.record.endTime}")
            if (item.record.location.isNotBlank()) append(" · ${item.record.location}")
        },
        leading = { AppDateBadge(item.record.date) },
        trailing = { AppStatusBadge(item.record.paymentStatus) },
        onClick = onClick
    )
}
