package com.teacher.journal.ui.settlement

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teacher.journal.data.entity.MonthlySettlement
import com.teacher.journal.data.entity.SessionRecord
import com.teacher.journal.ui.components.*
import com.teacher.journal.ui.theme.*
import com.teacher.journal.util.DateUtils

@Composable
fun MonthlySettlementScreen(
    studentId: Long,
    onNavigateBack: () -> Unit,
    viewModel: MonthlySettlementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var message by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            message = null
        }
    }

    LaunchedEffect(studentId) {
        viewModel.load(studentId)
    }

    var showCreateDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<MonthlySettlement?>(null) }
    var showDeleteDialog by remember { mutableStateOf<MonthlySettlement?>(null) }
    var createAmount by remember { mutableStateOf("") }
    var createNotes by remember { mutableStateOf("") }
    var createYear by remember { mutableStateOf(uiState.selectedYear) }
    var createMonth by remember { mutableStateOf(uiState.selectedMonth) }
    var editAmount by remember { mutableStateOf("") }
    var editNotes by remember { mutableStateOf("") }

    // 打开创建弹窗时同步当前选中月份
    LaunchedEffect(showCreateDialog) {
        if (showCreateDialog) {
            createYear = uiState.selectedYear
            createMonth = uiState.selectedMonth
            createAmount = if (uiState.student?.monthlyRate ?: 0.0 > 0) {
                String.format("%.0f", uiState.student?.monthlyRate ?: 0.0)
            } else ""
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = uiState.student?.name ?: "月结算",
                onBack = onNavigateBack
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp)
        ) {
            // 月份切换
            item {
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.previousMonth(studentId) },
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(Neutral100)
                    ) {
                        Icon(Icons.Filled.ChevronLeft, "上月", tint = Neutral700, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        "${uiState.selectedYear}年${uiState.selectedMonth + 1}月",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.weight(1f))
                    IconButton(
                        onClick = { viewModel.nextMonth(studentId) },
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(Neutral100)
                    ) {
                        Icon(Icons.Filled.ChevronRight, "下月", tint = Neutral700, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // 未结算记录 + 创建结算按钮（始终显示）
            item {
                AppSectionHeader("未结算记录（${uiState.unsettledRecords.size} 次）")
            }
            item {
                AppCard {
                    if (uiState.unsettledRecords.isNotEmpty()) {
                        uiState.unsettledRecords.forEachIndexed { i, record ->
                            UnsettledRecordRow(record = record)
                            if (i < uiState.unsettledRecords.lastIndex) AppDivider()
                        }
                        HorizontalDivider(color = AppDividerColor, thickness = 0.5.dp)
                    }
                    Row(
                        Modifier.fillMaxWidth().padding(18.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TextButton(onClick = { showCreateDialog = true }) {
                            Icon(Icons.Outlined.Add, contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("创建结算", fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // 已有结算
            item {
                AppSectionHeader("结算记录")
            }
            if (uiState.settlements.isEmpty()) {
                item {
                    AppCard {
                        AppEmptyState(
                            icon = Icons.Outlined.Payments,
                            title = "暂无结算记录"
                        )
                    }
                }
            } else {
                items(uiState.settlements, key = { it.id }) { settlement ->
                    SettlementCard(
                        settlement = settlement,
                        onMarkPaid = {
                            viewModel.markSettlementAsPaid(settlement) { message = "已标记为已收款" }
                        },
                        onEdit = {
                            editAmount = String.format("%.0f", settlement.totalAmount)
                            editNotes = settlement.notes
                            showEditDialog = settlement
                        },
                        onDelete = { showDeleteDialog = settlement }
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }

    // 创建结算对话框
    if (showCreateDialog) {
        val settlementDay = uiState.student?.settlementDay?.coerceIn(1, 28) ?: 1
        val window = DateUtils.getSettlementWindow(createYear, createMonth, settlementDay)
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("创建结算") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // 年月选择器
                    Text("结算月份", fontSize = 13.sp, color = AppTextSecondary)
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                var m = createMonth - 1
                                var y = createYear
                                if (m < 0) { m = 11; y -= 1 }
                                createMonth = m
                                createYear = y
                            },
                            modifier = Modifier.size(36.dp).clip(CircleShape).background(Neutral100)
                        ) {
                            Icon(Icons.Filled.ChevronLeft, "上月",
                                tint = Neutral700, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.weight(1f))
                        Text(
                            "${createYear}年${createMonth + 1}月",
                            fontSize = 18.sp, fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.weight(1f))
                        IconButton(
                            onClick = {
                                var m = createMonth + 1
                                var y = createYear
                                if (m > 11) { m = 0; y += 1 }
                                createMonth = m
                                createYear = y
                            },
                            modifier = Modifier.size(36.dp).clip(CircleShape).background(Neutral100)
                        ) {
                            Icon(Icons.Filled.ChevronRight, "下月",
                                tint = Neutral700, modifier = Modifier.size(18.dp))
                        }
                    }
                    // 结算周期提示
                    Text(
                        "结算周期：${DateUtils.formatWindow(window)}",
                        fontSize = 12.sp, color = AppTextSecondary
                    )
                    AppTextField(
                        value = createAmount,
                        onValueChange = { createAmount = it },
                        label = "结算金额（元）",
                        prefix = "¥"
                    )
                    AppTextField(
                        value = createNotes,
                        onValueChange = { createNotes = it },
                        label = "备注（可选）",
                        singleLine = false,
                        maxLines = 2
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showCreateDialog = false
                    viewModel.createSettlement(
                        studentId = studentId,
                        year = createYear,
                        month = createMonth,
                        amount = createAmount.toDoubleOrNull() ?: 0.0,
                        isPaid = false,
                        notes = createNotes.trim()
                    ) { message = "结算已创建" }
                    createAmount = ""
                    createNotes = ""
                }) { Text("创建") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("取消") }
            }
        )
    }

    // 编辑结算对话框
    showEditDialog?.let { settlement ->
        AlertDialog(
            onDismissRequest = { showEditDialog = null },
            title = { Text("编辑结算") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AppTextField(
                        value = editAmount,
                        onValueChange = { editAmount = it },
                        label = "结算金额（元）",
                        prefix = "¥"
                    )
                    AppTextField(
                        value = editNotes,
                        onValueChange = { editNotes = it },
                        label = "备注（可选）",
                        singleLine = false,
                        maxLines = 2
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showEditDialog = null
                    viewModel.updateSettlement(
                        settlement = settlement,
                        amount = editAmount.toDoubleOrNull() ?: settlement.totalAmount,
                        isPaid = settlement.isPaid,
                        notes = editNotes.trim()
                    ) { message = "结算已更新" }
                }) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = null }) { Text("取消") }
            }
        )
    }

    // 删除结算对话框
    showDeleteDialog?.let { settlement ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("删除结算") },
            text = { Text("删除后将解除关联记录并清除对应收入。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = null
                        viewModel.deleteSettlement(settlement) { message = "结算已删除" }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = AppError)
                ) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("取消") }
            }
        )
    }
}

@Composable
private fun UnsettledRecordRow(record: SessionRecord) {
    AppRow(
        title = "${record.startTime}–${record.endTime}",
        subtitle = if (record.content.isNotBlank()) record.content else null,
        leading = { AppDateBadge(record.date) }
    )
}

@Composable
private fun SettlementCard(
    settlement: MonthlySettlement,
    onMarkPaid: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    AppCard {
        Column(Modifier.padding(18.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "${settlement.year}年${settlement.month}月",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                AppStatusBadge(
                    if (settlement.isPaid) com.teacher.journal.data.entity.PaymentStatus.PAID
                    else com.teacher.journal.data.entity.PaymentStatus.UNPAID
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "${settlement.sessionCount}次课 · ¥${String.format("%.0f", settlement.totalAmount)}",
                fontSize = 14.sp,
                color = AppTextSecondary
            )
            if (settlement.notes.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(settlement.notes, fontSize = 13.sp, color = AppTextSecondary)
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (!settlement.isPaid) {
                    TextButton(onClick = onMarkPaid) {
                        Text("标记已收款", color = AppSuccess)
                    }
                }
                TextButton(onClick = onEdit) {
                    Text("编辑", color = MaterialTheme.colorScheme.primary)
                }
                TextButton(onClick = onDelete) {
                    Text("删除", color = AppError)
                }
            }
        }
    }
}