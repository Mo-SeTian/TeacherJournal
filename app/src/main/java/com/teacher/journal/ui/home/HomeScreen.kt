package com.teacher.journal.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teacher.journal.data.entity.PaymentStatus
import com.teacher.journal.ui.theme.*
import com.teacher.journal.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToStudentDetail: (Long) -> Unit,
    onNavigateToSessionRecord: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // 切回首页时自动刷新
    LaunchedEffect(Unit) {
        viewModel.loadDashboard()
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(windowInsets = WindowInsets(0,0,0,0),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = OnPrimary, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("授业札记", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary600.copy(alpha = 0.92f), titleContentColor = OnPrimary)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToSessionRecord,
                containerColor = Primary600,
                contentColor = OnPrimary,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("记录上课") }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            Box(modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(IridescentLavender.copy(alpha = 0.5f), IridescentBlue.copy(alpha = 0.3f), Gray50))
            )) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ① Hero 摘要卡片
                item(key = "hero") {
                    HeroSummaryWidget(
                        unpaidCount = uiState.unpaidRecords.size,
                        settlementCount = uiState.unpaidSettlements.size,
                        lowSessionCount = uiState.lowSessionStudents.size,
                        monthlyIncome = uiState.monthlyIncome
                    )
                }

                // ② 统计概览行
                item(key = "stats") {
                    OverviewRow(
                        studentCount = uiState.studentCount,
                        remainingSessions = uiState.totalRemainingSessions,
                        monthlyIncome = uiState.monthlyIncome
                    )
                }

                // ③ 待收费提醒
                if (uiState.unpaidRecords.isNotEmpty()) {
                    item(key = "pending-header") {
                        WidgetHeader("待收费提醒", Icons.Outlined.Payments, WarningOrange, "${uiState.unpaidRecords.size} 笔")
                    }
                    items(uiState.unpaidRecords, key = { "pending-${it.record.id}" }) { item ->
                        PendingPaymentCard(item, { viewModel.markAsPaid(item.record.id) }, { onNavigateToStudentDetail(item.record.studentId) })
                    }
                }

                // ④ 月结算待收款
                if (uiState.unpaidSettlements.isNotEmpty()) {
                    item(key = "settlement-header") {
                        WidgetHeader("月结算待收款", Icons.Outlined.DateRange, Amber600, "${uiState.unpaidSettlements.size} 笔")
                    }
                    items(uiState.unpaidSettlements, key = { "settle-${it.settlement.id}" }) { item ->
                        SettlementWidgetCard(item, { onNavigateToStudentDetail(item.settlement.studentId) })
                    }
                }

                // ⑤ 课时不足
                if (uiState.lowSessionStudents.isNotEmpty()) {
                    item(key = "low-header") {
                        WidgetHeader("课时不足提醒", Icons.Outlined.Warning, ErrorRed, "${uiState.lowSessionStudents.size} 人")
                    }
                    items(uiState.lowSessionStudents, key = { "low-${it.studentId}" }) { item ->
                        LowSessionWidgetCard(item, { onNavigateToStudentDetail(item.studentId) })
                    }
                }

                // ⑥ 最近上课
                item(key = "recent-header") {
                    WidgetHeader("最近上课", Icons.Outlined.Schedule, Primary, null)
                }
                if (uiState.recentRecords.isEmpty()) {
                    item(key = "recent-empty") {
                        EmptyWidget("暂无上课记录", "点击下方按钮记录第一堂课")
                    }
                } else {
                    items(uiState.recentRecords, key = { "recent-${it.record.id}" }) { item ->
                        RecentRecordWidgetCard(item)
                    }
                }
            }
            }
        }
    }
}

// ── ① HeroSummaryWidget ──
@Composable
private fun HeroSummaryWidget(unpaidCount: Int, settlementCount: Int, lowSessionCount: Int, monthlyIncome: Double) {
    val colorScheme = MaterialTheme.colorScheme
    val gradient = remember {
        Brush.verticalGradient(listOf(IridescentLavender, IridescentBlue, Color.White))
    }
    val hasAlert = unpaidCount > 0 || settlementCount > 0 || lowSessionCount > 0
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(Modifier.fillMaxWidth().background(gradient)) {
            Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(42.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.AutoGraph, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("教学仪表盘", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            if (hasAlert) "有 ${unpaidCount + settlementCount + lowSessionCount} 项待处理" else "一切就绪",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 本月收入大字
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("¥${String.format("%.0f", monthlyIncome)}", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("本月收入", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // 三项指标行
                if (hasAlert) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        if (unpaidCount > 0) HeroMetric("待收费", "$unpaidCount", WarningOrange)
                        if (settlementCount > 0) HeroMetric("月结算", "$settlementCount", Amber600)
                        if (lowSessionCount > 0) HeroMetric("课时不足", "$lowSessionCount", ErrorRed)
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroMetric(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ── ② OverviewRow ──
@Composable
private fun OverviewRow(studentCount: Int, remainingSessions: Int, monthlyIncome: Double) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatWidgetCard(Modifier.weight(1f), "学生总数", "$studentCount", Icons.Outlined.People, MaterialTheme.colorScheme.primary)
        StatWidgetCard(Modifier.weight(1f), "剩余课时", "$remainingSessions", Icons.Outlined.Book, Green600)
        StatWidgetCard(Modifier.weight(1f), "月收入", "¥${String.format("%.0f", monthlyIncome)}", Icons.Outlined.Payments, Amber600)
    }
}

@Composable
private fun StatWidgetCard(modifier: Modifier, label: String, value: String, icon: ImageVector, accent: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.size(42.dp).clip(CircleShape).background(accent.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
            }
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = accent)
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ── WidgetHeader ──
@Composable
private fun WidgetHeader(title: String, icon: ImageVector, accent: Color, count: String?) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (count != null) {
            Spacer(Modifier.width(8.dp))
            Surface(shape = RoundedCornerShape(10.dp), color = accent.copy(alpha = 0.12f)) {
                Text(count, Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = accent)
            }
        }
    }
}

// ── EmptyWidget ──
@Composable
private fun EmptyWidget(title: String, subtitle: String) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(Modifier.fillMaxWidth().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Outlined.EventNote, contentDescription = null, tint = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.size(44.dp))
            Spacer(Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}

// ── ③ PendingPaymentCard ──
@Composable
private fun PendingPaymentCard(item: UnpaidRecordItem, onMarkPaid: () -> Unit, onClick: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(if (item.isOverdue) ErrorRed else WarningOrange))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(item.studentName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(DateUtils.formatDateFull(item.record.date), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (item.record.location.isNotBlank()) {
                        Spacer(Modifier.width(8.dp))
                        Text(item.record.location, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline, maxLines = 1)
                    }
                }
                Text("¥${String.format("%.0f", item.record.amount)} 待收", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = WarningOrange)
            }
            FilledTonalButton(onClick = onMarkPaid, colors = ButtonDefaults.filledTonalButtonColors(containerColor = SuccessBg, contentColor = SuccessGreen), contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)) {
                Text("确认收款", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ── ④ SettlementWidgetCard ──
@Composable
private fun SettlementWidgetCard(item: UnpaidSettlementItem, onClick: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.DateRange, contentDescription = null, tint = Amber500, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(item.studentName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                Text("${item.settlement.year}年${item.settlement.month + 1}月 · ${item.settlement.sessionCount} 次课 · ¥${String.format("%.0f", item.settlement.totalAmount)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(shape = RoundedCornerShape(8.dp), color = WarningBg) {
                Text("待收款", Modifier.padding(horizontal = 10.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = WarningOrange)
            }
        }
    }
}

// ── ⑤ LowSessionWidgetCard ──
@Composable
private fun LowSessionWidgetCard(item: LowSessionStudentItem, onClick: () -> Unit) {
    val empty = item.remainingSessions == 0
    Card(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (empty) ErrorBg else WarningBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(if (empty) Icons.Filled.PriorityHigh else Icons.Filled.Info, contentDescription = null, tint = if (empty) ErrorRed else WarningOrange, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(item.studentName, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(if (empty) "已用完" else "剩余 ${item.remainingSessions} 次", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = if (empty) ErrorRed else WarningOrange)
        }
    }
}

// ── ⑥ RecentRecordWidgetCard ──
@Composable
private fun RecentRecordWidgetCard(item: RecentRecordItem) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(Modifier.size(44.dp), shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                Box(contentAlignment = Alignment.Center) {
                    Text(DateUtils.formatDateDisplay(item.record.date), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.studentName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(6.dp))
                    Text("${item.record.startTime} – ${item.record.endTime}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (item.record.location.isNotBlank()) {
                        Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(2.dp))
                        Text(item.record.location, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                    }
                    if (item.record.content.isNotBlank()) {
                        if (item.record.location.isNotBlank()) { Spacer(Modifier.width(8.dp)) }
                        Text(item.record.content, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline, maxLines = 1)
                    }
                }
            }
            PaymentStatusBadge(item.record.paymentStatus)
        }
    }
}

@Composable
fun PaymentStatusBadge(status: PaymentStatus) {
    val (text, bg, fg) = when (status) {
        PaymentStatus.PAID -> Triple("已收费", SuccessBg, SuccessGreen)
        PaymentStatus.UNPAID -> Triple("待收费", WarningBg, WarningOrange)
    }
    Surface(shape = RoundedCornerShape(8.dp), color = bg) {
        Text(text, Modifier.padding(horizontal = 10.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = fg)
    }
}
