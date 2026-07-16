package com.teacher.journal.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.teacher.journal.data.entity.SessionRecord
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.MenuBook,
                            contentDescription = null,
                            tint = OnPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("授业札记", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Blue600,
                    titleContentColor = OnPrimary
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToSessionRecord,
                containerColor = Blue600,
                contentColor = OnPrimary,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("记录上课") }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Blue600)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // 统计卡片
                item { StatsRow(uiState.studentCount, uiState.totalRemainingSessions, uiState.monthlyIncome) }

                // 待收费提醒
                if (uiState.unpaidRecords.isNotEmpty()) {
                    item { SectionHeader("待收费提醒", uiState.unpaidRecords.size.toString(), WarningOrange) }
                    items(uiState.unpaidRecords) { item ->
                        UnpaidRecordCard(item, { viewModel.markAsPaid(item.record.id) }, { onNavigateToStudentDetail(item.record.studentId) })
                    }
                }

                // 月结算待收款
                if (uiState.unpaidSettlements.isNotEmpty()) {
                    item { SectionHeader("月结算待收款", uiState.unpaidSettlements.size.toString(), Amber600) }
                    items(uiState.unpaidSettlements) { item ->
                        UnpaidSettlementHomeCard(item, { onNavigateToStudentDetail(item.settlement.studentId) })
                    }
                }

                // 课时不足
                if (uiState.lowSessionStudents.isNotEmpty()) {
                    item { SectionHeader("课时不足提醒", uiState.lowSessionStudents.size.toString(), ErrorRed) }
                    items(uiState.lowSessionStudents) { item ->
                        LowSessionCard(item, { onNavigateToStudentDetail(item.studentId) })
                    }
                }

                // 最近记录
                item { SectionHeader("最近上课", null, Blue600) }
                if (uiState.recentRecords.isEmpty()) {
                    item { EmptyStateCard("暂无上课记录", "点击下方按钮记录第一堂课") }
                } else {
                    items(uiState.recentRecords) { item -> RecentRecordCard(item) }
                }

                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

// ── 统计卡片行 ──
@Composable
private fun StatsRow(studentCount: Int, remainingSessions: Int, monthlyIncome: Double) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatCard(Modifier.weight(1f), "学生总数", "$studentCount", Blue50 to Blue100, Blue600, Icons.Outlined.People)
        StatCard(Modifier.weight(1f), "剩余课时", "$remainingSessions", Green50 to Green100, Green600, Icons.Outlined.Book)
        StatCard(Modifier.weight(1f), "本月收入", "¥${String.format("%.0f", monthlyIncome)}", Amber50 to Amber100, Amber600, Icons.Outlined.Payments)
    }
}

@Composable
private fun StatCard(modifier: Modifier, label: String, value: String, gradient: Pair<Color, Color>, accent: Color, icon: ImageVector) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.size(36.dp).background(gradient.first, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Gray900)
            Text(label, style = MaterialTheme.typography.labelSmall, color = Gray500)
        }
    }
}

// ── Section Header ──
@Composable
private fun SectionHeader(title: String, count: String?, accent: Color) {
    Row(Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = Gray900)
        if (count != null) {
            Spacer(Modifier.width(6.dp))
            Surface(shape = RoundedCornerShape(10.dp), color = accent.copy(alpha = 0.12f)) {
                Text(count, Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = accent)
            }
        }
    }
}

// ── 空状态 ──
@Composable
private fun EmptyStateCard(title: String, subtitle: String) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = SurfaceWhite)) {
        Column(Modifier.fillMaxWidth().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Outlined.EventNote, contentDescription = null, tint = Gray300, modifier = Modifier.size(44.dp))
            Spacer(Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.bodyLarge, color = Gray600)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Gray400)
        }
    }
}

// ── 待收费卡片 ──
@Composable
private fun UnpaidRecordCard(item: UnpaidRecordItem, onMarkPaid: () -> Unit, onClick: () -> Unit) {
    val overdue = item.isOverdue
    Card(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(if (overdue) ErrorRed else WarningOrange))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("${item.studentName}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = Gray400, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(DateUtils.formatDateFull(item.record.date), style = MaterialTheme.typography.bodySmall, color = Gray500)
                    if (item.record.location.isNotBlank()) {
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = Gray400, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(2.dp))
                        Text(item.record.location, style = MaterialTheme.typography.bodySmall, color = Gray500, maxLines = 1)
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

// ── 月结算待收款卡片 ──
@Composable
private fun UnpaidSettlementHomeCard(item: UnpaidSettlementItem, onClick: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.DateRange, contentDescription = null, tint = Amber500, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("${item.studentName}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                Text("${item.settlement.year}年${item.settlement.month + 1}月 · ${item.settlement.sessionCount} 次课 · ¥${String.format("%.0f", item.settlement.totalAmount)}", style = MaterialTheme.typography.bodySmall, color = Gray500)
            }
            Surface(shape = RoundedCornerShape(8.dp), color = WarningBg) {
                Text("待收款", Modifier.padding(horizontal = 10.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = WarningOrange)
            }
        }
    }
}

// ── 课时不足卡片 ──
@Composable
private fun LowSessionCard(item: LowSessionStudentItem, onClick: () -> Unit) {
    val empty = item.remainingSessions == 0
    Card(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (empty) ErrorBg else WarningBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (empty) Icons.Filled.PriorityHigh else Icons.Filled.Info,
                contentDescription = null,
                tint = if (empty) ErrorRed else WarningOrange,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("${item.studentName}", Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(
                if (empty) "已用完" else "剩余 ${item.remainingSessions} 次",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (empty) ErrorRed else WarningOrange
            )
        }
    }
}

// ── 最近记录卡片 ──
@Composable
private fun RecentRecordCard(item: RecentRecordItem) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(Modifier.size(44.dp), shape = RoundedCornerShape(12.dp), color = Blue50) {
                Box(contentAlignment = Alignment.Center) {
                    Text(DateUtils.formatDateDisplay(item.record.date), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Blue600)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.studentName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = Gray900)
                    Spacer(Modifier.width(6.dp))
                    Text("${item.record.startTime} – ${item.record.endTime}", style = MaterialTheme.typography.bodySmall, color = Gray500)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (item.record.location.isNotBlank()) {
                        Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = Gray400, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(2.dp))
                        Text(item.record.location, style = MaterialTheme.typography.bodySmall, color = Gray500, maxLines = 1)
                    }
                    if (item.record.content.isNotBlank()) {
                        if (item.record.location.isNotBlank()) { Spacer(Modifier.width(8.dp)) }
                        Text(item.record.content, style = MaterialTheme.typography.bodySmall, color = Gray400, maxLines = 1)
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
