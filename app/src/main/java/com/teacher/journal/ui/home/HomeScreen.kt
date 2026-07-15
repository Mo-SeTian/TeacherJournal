package com.teacher.journal.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
                    Text(
                        "授业札记",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = OnPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToSessionRecord,
                containerColor = Primary,
                contentColor = OnPrimary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "记录上课")
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
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
                item {
                    StatsRow(
                        studentCount = uiState.studentCount,
                        remainingSessions = uiState.totalRemainingSessions,
                        monthlyIncome = uiState.monthlyIncome
                    )
                }

                // 待收费提醒
                if (uiState.unpaidRecords.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "🔔 待收费提醒（${uiState.unpaidRecords.size}笔）",
                            icon = Icons.Filled.Warning
                        )
                    }
                    items(uiState.unpaidRecords) { item ->
                        UnpaidRecordCard(
                            item = item,
                            onMarkPaid = { viewModel.markAsPaid(item.record.id) },
                            onClick = { onNavigateToStudentDetail(item.record.studentId) }
                        )
                    }
                }

                // 课时不足提醒
                if (uiState.lowSessionStudents.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "⚠️ 课时不足提醒",
                            icon = Icons.Filled.Notifications
                        )
                    }
                    items(uiState.lowSessionStudents) { item ->
                        LowSessionCard(
                            item = item,
                            onClick = { onNavigateToStudentDetail(item.studentId) }
                        )
                    }
                }

                // 最近上课记录
                item {
                    SectionHeader(
                        title = "📅 最近上课",
                        icon = Icons.Filled.History
                    )
                }
                if (uiState.recentRecords.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Surface)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "暂无上课记录\n点击右下角 + 开始记录",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                } else {
                    items(uiState.recentRecords) { record ->
                        RecentRecordCard(
                            record = record
                        )
                    }
                }

                // 底部留白
                item { Spacer(modifier = Modifier.height(72.dp)) }
            }
        }
    }
}

@Composable
private fun StatsRow(
    studentCount: Int,
    remainingSessions: Int,
    monthlyIncome: Double
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            label = "学生数",
            value = "$studentCount人",
            icon = Icons.Filled.People,
            color = Primary
        )
        StatCard(
            modifier = Modifier.weight(1f),
            label = "剩余课时",
            value = "${remainingSessions}次",
            icon = Icons.Filled.Book,
            color = Secondary
        )
        StatCard(
            modifier = Modifier.weight(1f),
            label = "月收入",
            value = "¥${String.format("%.0f", monthlyIncome)}",
            icon = Icons.Filled.Payments,
            color = Tertiary
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}

@Composable
private fun UnpaidRecordCard(
    item: UnpaidRecordItem,
    onMarkPaid: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isOverdue) StatusOverdue.copy(alpha = 0.08f) else StatusUnpaid.copy(alpha = 0.08f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Circle,
                contentDescription = null,
                tint = if (item.isOverdue) StatusOverdue else StatusUnpaid,
                modifier = Modifier.size(10.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${item.studentName} · ${DateUtils.formatDateDisplay(item.record.date)}",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )
                Text(
                    text = "${item.record.location} · ¥${String.format("%.0f", item.record.amount)} 待收费",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            FilledTonalButton(
                onClick = onMarkPaid,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = StatusPaid.copy(alpha = 0.15f),
                    contentColor = StatusPaid
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("标记已收费", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun LowSessionCard(
    item: LowSessionStudentItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (item.remainingSessions == 0)
                StatusOverdue.copy(alpha = 0.06f) else StatusUnpaid.copy(alpha = 0.06f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "· ${item.studentName} — 剩余 ${item.remainingSessions} 次",
                style = MaterialTheme.typography.bodyMedium,
                color = if (item.remainingSessions == 0) StatusOverdue else TextPrimary,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = if (item.remainingSessions == 0) "需续费" else "即将用完",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary
            )
        }
    }
}

@Composable
private fun RecentRecordCard(record: SessionRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.width(48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = DateUtils.formatDateDisplay(record.date),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
                Text(
                    text = DateUtils.getWeekday(record.date),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${record.startTime}-${record.endTime}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                if (record.location.isNotBlank()) {
                    Text(
                        text = record.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }
                if (record.content.isNotBlank()) {
                    Text(
                        text = record.content,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }
            }
            PaymentStatusChip(record.paymentStatus)
        }
    }
}

@Composable
fun PaymentStatusChip(status: PaymentStatus) {
    val (text, color) = when (status) {
        PaymentStatus.PAID -> "已收费 ✅" to StatusPaid
        PaymentStatus.UNPAID -> "待收费 🟡" to StatusUnpaid
    }
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = color
    )
}
