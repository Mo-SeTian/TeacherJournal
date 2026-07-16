package com.teacher.journal.ui.session

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teacher.journal.data.entity.SessionRecord
import com.teacher.journal.ui.home.PaymentStatusBadge
import com.teacher.journal.ui.theme.*
import com.teacher.journal.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionListScreen(
    viewModel: SessionViewModel = hiltViewModel()
) {
    val uiState by viewModel.listUiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("上课记录", style = MaterialTheme.typography.headlineSmall)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = OnPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 月份切换器
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.previousMonth() }) {
                        Icon(Icons.Filled.ChevronLeft, contentDescription = "上个月")
                    }
                    Text(
                        text = "${uiState.currentYear}年${uiState.currentMonth + 1}月",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    IconButton(onClick = { viewModel.nextMonth() }) {
                        Icon(Icons.Filled.ChevronRight, contentDescription = "下个月")
                    }
                }
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            } else if (uiState.records.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.EventBusy,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = TextTertiary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("本月暂无上课记录", color = TextSecondary)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    // 按日期分组显示
                    val groupedByDate = uiState.records.groupBy { it.date }
                    groupedByDate.forEach { (date, records) ->
                        item {
                            Text(
                                text = "${DateUtils.formatDateDisplay(date)} ${DateUtils.getWeekday(date)}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Primary,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }
                        items(records) { record ->
                            SessionRecordCard(
                                record = record,
                                studentName = uiState.students[record.studentId]?.name ?: "未知",
                                onMarkPaid = {
                                    if (record.paymentStatus == com.teacher.journal.data.entity.PaymentStatus.UNPAID) {
                                        viewModel.markAsPaid(record.id)
                                    }
                                }
                            )
                        }
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun SessionRecordCard(
    record: SessionRecord,
    studentName: String,
    onMarkPaid: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = studentName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${record.startTime}-${record.endTime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                if (record.location.isNotBlank() || record.content.isNotBlank()) {
                    Text(
                        text = listOfNotNull(
                            record.location.takeIf { it.isNotBlank() },
                            record.content.takeIf { it.isNotBlank() }
                        ).joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }
                if (record.amount > 0) {
                    Text(
                        text = "¥${String.format("%.0f", record.amount)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Tertiary
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                PaymentStatusBadge(record.paymentStatus)
                if (record.paymentStatus == com.teacher.journal.data.entity.PaymentStatus.UNPAID) {
                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(
                        onClick = onMarkPaid,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            "标记已收费",
                            style = MaterialTheme.typography.labelSmall,
                            color = StatusPaid
                        )
                    }
                }
            }
        }
    }
}
