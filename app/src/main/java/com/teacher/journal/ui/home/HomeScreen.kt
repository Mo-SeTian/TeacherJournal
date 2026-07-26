package com.teacher.journal.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teacher.journal.data.entity.PaymentStatus
import com.teacher.journal.ui.components.*
import com.teacher.journal.ui.theme.*
import com.teacher.journal.util.DateUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToStudentDetail: (Long) -> Unit,
    onNavigateToSessionRecord: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.loadDashboard() }

    val today = remember {
        val fmt = SimpleDateFormat("M月d日 · EEEE", Locale.CHINA)
        fmt.format(Date())
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = {},
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Filled.Settings, "设置", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToSessionRecord,
                shape = RoundedCornerShape(28.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("记录上课", fontWeight = FontWeight.SemiBold) }
            )
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
            contentPadding = PaddingValues(top = 4.dp, bottom = 100.dp)
        ) {
            // Large title
            item {
                LargeTitle(title = "今日", subtitle = today)
                Spacer(Modifier.height(8.dp))
            }

            // Hero: 本月收入
            item {
                HeroIncomeCard(
                    monthlyIncome = uiState.monthlyIncome,
                    studentCount = uiState.studentCount,
                    remainingSessions = uiState.totalRemainingSessions
                )
            }

            // 待处理提醒（合并三类到一张分组卡）
            val hasAlerts = uiState.unpaidRecords.isNotEmpty() ||
                    uiState.unpaidSettlements.isNotEmpty() ||
                    uiState.lowSessionStudents.isNotEmpty()
            if (hasAlerts) {
                item { SectionHeader("待处理") }
                item {
                    val totalAlerts = uiState.unpaidRecords.size +
                            uiState.unpaidSettlements.size +
                            uiState.lowSessionStudents.size
                    var idx = 0
                    GroupedCard {
                        uiState.unpaidRecords.forEach { alertItem ->
                            UnpaidRow(alertItem) { onNavigateToStudentDetail(alertItem.record.studentId) }
                            idx++
                            if (idx < totalAlerts) RowDivider(startInset = AppleInset.Full)
                        }
                        uiState.unpaidSettlements.forEach { alertItem ->
                            SettlementRow(alertItem) { onNavigateToStudentDetail(alertItem.settlement.studentId) }
                            idx++
                            if (idx < totalAlerts) RowDivider(startInset = AppleInset.Full)
                        }
                        uiState.lowSessionStudents.forEach { alertItem ->
                            LowSessionRow(alertItem) { onNavigateToStudentDetail(alertItem.studentId) }
                            idx++
                            if (idx < totalAlerts) RowDivider(startInset = AppleInset.Full)
                        }
                    }
                }
            }

            // 最近上课
            item { SectionHeader("最近上课") }
            if (uiState.recentRecords.isEmpty()) {
                item {
                    GroupedCard {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("暂无上课记录", fontSize = 15.sp, color = AppleLabelSecondary)
                                Text("点击右下角按钮记录第一堂课",
                                    fontSize = 13.sp, color = AppleLabelTertiary,
                                    modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                }
            } else {
                item {
                    GroupedCard {
                        uiState.recentRecords.forEachIndexed { i, item ->
                            RecentSessionRow(item)
                            if (i < uiState.recentRecords.lastIndex) RowDivider(startInset = AppleInset.Avatar)
                        }
                    }
                }
            }
        }
    }
}

// ── Hero income card ──
@Composable
private fun HeroIncomeCard(monthlyIncome: Double, studentCount: Int, remainingSessions: Int) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(20.dp)) {
            Text("本月收入", fontSize = 13.sp, color = AppleLabelSecondary, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text("¥", fontSize = 22.sp, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 6.dp))
                Text(
                    String.format("%,.0f", monthlyIncome),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 44.sp
                )
            }
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = AppleSeparator, thickness = 0.5.dp)
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                HeroStat("学生", "$studentCount")
                HeroStat("剩余课时", "$remainingSessions")
            }
        }
    }
}

@Composable
private fun HeroStat(label: String, value: String) {
    Column {
        Text(label, fontSize = 12.sp, color = AppleLabelSecondary)
        Spacer(Modifier.height(2.dp))
        Text(value, fontSize = 17.sp, fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface)
    }
}

// ── Row components ──
@Composable
private fun UnpaidRow(item: UnpaidRecordItem, onClick: () -> Unit) {
    ListRow(
        title = item.studentName,
        subtitle = "${DateUtils.formatDateFull(item.record.date)} · 待收 ¥${String.format("%.0f", item.record.amount)}",
        leading = { StatusDot(if (item.isOverdue) ErrorRed else WarningOrange) },
        showChevron = true,
        onClick = onClick
    )
}

@Composable
private fun SettlementRow(item: UnpaidSettlementItem, onClick: () -> Unit) {
    ListRow(
        title = item.studentName,
        subtitle = "${item.settlement.year}年${item.settlement.month + 1}月 · ¥${String.format("%.0f", item.settlement.totalAmount)}",
        leading = { StatusDot(Amber600) },
        trailing = { TrailingPill("月结算", WarningOrange, WarningBg) },
        showChevron = true,
        onClick = onClick
    )
}

@Composable
private fun LowSessionRow(item: LowSessionStudentItem, onClick: () -> Unit) {
    val empty = item.remainingSessions == 0
    ListRow(
        title = item.studentName,
        subtitle = if (empty) "课时已用完" else "剩余 ${item.remainingSessions} 次课时",
        leading = { StatusDot(if (empty) ErrorRed else WarningOrange) },
        showChevron = true,
        onClick = onClick
    )
}

@Composable
private fun StatusDot(color: Color) {
    Box(Modifier.size(10.dp).clip(CircleShape).background(color))
}

@Composable
private fun RecentSessionRow(item: RecentRecordItem) {
    val locSubject = buildString {
        if (item.record.location.isNotBlank()) append(item.record.location)
        if (item.record.content.isNotBlank()) {
            if (isNotEmpty()) append(" · ")
            append(item.record.content)
        }
    }
    ListRow(
        title = item.studentName,
        subtitle = if (locSubject.isBlank()) item.record.startTime
                   else "${item.record.startTime} · $locSubject",
        leading = { DateBadge(item.record.date) },
        trailing = { PaymentStatusBadge(item.record.paymentStatus) }
    )
}

private val WEEK_LABELS_HOME = arrayOf("日", "一", "二", "三", "四", "五", "六")

@Composable
private fun DateBadge(date: Long) {
    val cal = remember(date) { Calendar.getInstance().apply { timeInMillis = date } }
    val day = cal.get(Calendar.DAY_OF_MONTH).toString()
    val weekLabel = "周${WEEK_LABELS_HOME[cal.get(Calendar.DAY_OF_WEEK) - 1]}"
    val primary = MaterialTheme.colorScheme.primary
    Column(
        Modifier.size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(primary.copy(alpha = 0.1f)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            day,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = primary,
            lineHeight = 18.sp
        )
        Text(
            weekLabel,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            color = primary.copy(alpha = 0.75f),
            lineHeight = 11.sp
        )
    }
}

@Composable
fun PaymentStatusBadge(status: PaymentStatus) {
    when (status) {
        PaymentStatus.PAID -> TrailingPill("已收费", SuccessGreen, SuccessBg)
        PaymentStatus.UNPAID -> TrailingPill("待收费", WarningOrange, WarningBg)
    }
}
