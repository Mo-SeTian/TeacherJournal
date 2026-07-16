package com.teacher.journal.ui.settlement

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teacher.journal.data.entity.MonthlySettlement
import com.teacher.journal.data.entity.PaymentType
import com.teacher.journal.data.entity.SessionRecord
import com.teacher.journal.ui.theme.*
import com.teacher.journal.util.DateUtils
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlySettlementScreen(
    studentId: Long,
    onNavigateBack: () -> Unit,
    viewModel: MonthlySettlementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(studentId) {
        viewModel.load(studentId)
    }

    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(windowInsets = WindowInsets(0,0,0,0),
                title = { Text("月结算管理") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // 学生信息
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = Surface)) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${uiState.student?.name ?: ""}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            if (uiState.student?.monthlyRate?.let { it > 0 } == true) {
                                Text(
                                    "月薪 ¥${String.format("%.0f", uiState.student!!.monthlyRate)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Secondary
                                )
                            }
                        }
                    }
                }

                // 未结算记录
                item {
                    Text(
                        "📋 未结算上课记录（${uiState.unsettledRecords.size} 次）",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (uiState.unsettledRecords.isEmpty()) {
                    item {
                        Text("当前无未结算记录", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                    }
                } else {
                    items(uiState.unsettledRecords) { record ->
                        UnsettledRecordCard(record)
                    }
                }

                // 创建结算按钮
                item {
                    Button(
                        onClick = { showCreateDialog = true },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Secondary),
                        enabled = uiState.unsettledRecords.isNotEmpty()
                    ) {
                        Text("📅 创建月结算", style = MaterialTheme.typography.titleSmall)
                    }
                }

                // 历史结算
                if (uiState.settlements.isNotEmpty()) {
                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Text(
                            "📅 历史结算记录",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    items(uiState.settlements) { settlement ->
                        SettlementCard(
                            settlement = settlement,
                            onMarkPaid = { viewModel.markSettlementAsPaid(settlement.id) }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }

    // 创建结算对话框
    if (showCreateDialog) {
        CreateSettlementDialog(
            unsettledCount = uiState.unsettledRecords.size,
            defaultAmount = uiState.student?.monthlyRate ?: 0.0,
            onDismiss = { showCreateDialog = false },
            onConfirm = { amount, isPaid, notes ->
                val calendar = Calendar.getInstance()
                viewModel.createSettlement(
                    studentId = studentId,
                    year = calendar.get(Calendar.YEAR),
                    month = calendar.get(Calendar.MONTH),
                    amount = amount,
                    isPaid = isPaid,
                    notes = notes,
                    onComplete = { showCreateDialog = false }
                )
            }
        )
    }
}

@Composable
private fun UnsettledRecordCard(record: SessionRecord) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.width(48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = DateUtils.formatDateDisplay(record.date), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Primary)
                Text(text = DateUtils.getWeekday(record.date), style = MaterialTheme.typography.labelSmall, color = TextTertiary)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = "${record.startTime}-${record.endTime}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                if (record.location.isNotBlank()) Text(text = record.location, style = MaterialTheme.typography.bodySmall, color = TextTertiary)
            }
        }
    }
}

@Composable
private fun SettlementCard(settlement: MonthlySettlement, onMarkPaid: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = if (settlement.isPaid) StatusPaid.copy(alpha = 0.04f) else StatusUnpaid.copy(alpha = 0.06f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${settlement.year}年${settlement.month + 1}月",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${settlement.sessionCount} 次课 · ¥${String.format("%.0f", settlement.totalAmount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                if (settlement.notes.isNotBlank()) {
                    Text(text = settlement.notes, style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                }
            }
            if (!settlement.isPaid) {
                FilledTonalButton(
                    onClick = onMarkPaid,
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = StatusPaid.copy(alpha = 0.15f), contentColor = StatusPaid),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("标记收款", style = MaterialTheme.typography.labelSmall)
                }
            } else {
                Surface(shape = MaterialTheme.shapes.small, color = StatusPaid.copy(alpha = 0.1f)) {
                    Text("✅ 已收款", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = StatusPaid)
                }
            }
        }
    }
}

@Composable
private fun CreateSettlementDialog(
    unsettledCount: Int,
    defaultAmount: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double, Boolean, String) -> Unit
) {
    var amount by remember { mutableStateOf(if (defaultAmount > 0) String.format("%.0f", defaultAmount) else "") }
    var isPaid by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("创建月结算") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "本月有 $unsettledCount 次未结算上课记录，将统一归入本次结算。",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it; amountError = false },
                    label = { Text("结算金额 *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = amountError,
                    prefix = { Text("¥") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, focusedLabelColor = Primary)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isPaid, onCheckedChange = { isPaid = it }, colors = CheckboxDefaults.colors(checkedColor = Primary))
                    Text("已收款", style = MaterialTheme.typography.bodyMedium)
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("备注（可选）") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, focusedLabelColor = Primary)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (amount.isBlank() || (amount.toDoubleOrNull() ?: 0.0) <= 0) {
                        amountError = true
                        return@Button
                    }
                    onConfirm(amount.toDouble(), isPaid, notes.trim())
                },
                colors = ButtonDefaults.buttonColors(containerColor = Secondary)
            ) { Text("创建结算") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
