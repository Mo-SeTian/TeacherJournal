package com.teacher.journal.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.Payments
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
import com.teacher.journal.data.entity.PaymentStatus
import com.teacher.journal.ui.components.*
import com.teacher.journal.ui.theme.*
import com.teacher.journal.util.DateUtils

@Composable
fun HomeScreen(
    onNavigateToStudentDetail: (Long) -> Unit,
    onNavigateToSessionRecord: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.loadDashboard() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                title = "首页",
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Filled.Settings, "设置", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToSessionRecord,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
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

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 100.dp)
        ) {
            item {
                AppScreenTitle(title = "今日", subtitle = TodayLabel())
                Spacer(Modifier.height(10.dp))
            }

            item {
                HeroIncomeCard(
                    monthlyIncome = uiState.monthlyIncome,
                    studentCount = uiState.studentCount,
                    remainingSessions = uiState.totalRemainingSessions,
                    alertCount = uiState.unpaidRecords.size + uiState.unpaidSettlements.size
                )
            }

            val hasAlerts = uiState.unpaidRecords.isNotEmpty() ||
                    uiState.unpaidSettlements.isNotEmpty() ||
                    uiState.lowSessionStudents.isNotEmpty()
            if (hasAlerts) {
                item { AppSectionHeader("待处理") }
                item {
                    AppCard {
                        uiState.unpaidRecords.forEachIndexed { i, item ->
                            AlertRow(
                                title = item.studentName,
                                subtitle = "${DateUtils.formatDateFull(item.record.date)} · 待收 ¥${fmt(item.record.amount)}",
                                icon = Icons.Outlined.Payments,
                                color = if (item.isOverdue) AppError else AppWarning,
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
                                onClick = { onNavigateToStudentDetail(item.studentId) }
                            )
                            if (i < uiState.lowSessionStudents.lastIndex) AppDivider()
                        }
                    }
                }
            }

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
                            AppRow(
                                title = item.studentName,
                                subtitle = buildString {
                                    append("${DateUtils.formatDayMonth(item.record.date)} ${item.record.startTime}–${item.record.endTime}")
                                    if (item.record.location.isNotBlank()) append(" · ${item.record.location}")
                                },
                                leading = { AppDateBadge(item.record.date) },
                                trailing = { AppStatusBadge(item.record.paymentStatus) },
                                onClick = { onNavigateToStudentDetail(item.record.studentId) }
                            )
                            if (i < uiState.recentRecords.lastIndex) AppDivider()
                        }
                    }
                }
            }
        }
    }
}

private fun fmt(v: Double): String = String.format("%.0f", v)

@Composable
private fun HeroIncomeCard(
    monthlyIncome: Double,
    studentCount: Int,
    remainingSessions: Int,
    alertCount: Int
) {
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    AppGradientCard(colors = listOf(primary, tertiary.copy(alpha = 0.85f))) {
        Text("本月收入", fontSize = 13.sp, color = Color.White.copy(alpha = 0.85f), fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text("¥", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Color.White,
                modifier = Modifier.padding(bottom = 7.dp))
            Text(
                String.format("%,.0f", monthlyIncome),
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                lineHeight = 46.sp
            )
        }
        Spacer(Modifier.height(18.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.22f), thickness = 0.7.dp)
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            HeroStat("学生", "$studentCount")
            HeroStat("剩余课时", "$remainingSessions")
            HeroStat("待收款", "$alertCount")
        }
    }
}

@Composable
private fun HeroStat(label: String, value: String) {
    Column {
        Text(label, fontSize = 12.sp, color = Color.White.copy(alpha = 0.75f))
        Spacer(Modifier.height(2.dp))
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
private fun AlertRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    AppRow(
        title = title,
        subtitle = subtitle,
        leading = {
            Box(
                Modifier.size(34.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(17.dp))
            }
        },
        showChevron = true,
        onClick = onClick
    )
}
