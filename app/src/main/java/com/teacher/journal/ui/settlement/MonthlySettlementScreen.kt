package com.teacher.journal.ui.settlement

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.Payments
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
    var editTarget by remember { mutableStateOf<MonthlySettlement?>(null) }
    var deleteTarget by remember { mutableStateOf<MonthlySettlement?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(title = "月结算管理", onBack = onNavigateBack)
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    AppCard {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AppAvatar(uiState.student?.name ?: "", size = 40.dp)
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(uiState.student?.name ?: "", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                    if (uiState.student?.monthlyRate?.let { it > 0 } == true) {
                                        Text(
                                            "月薪 ¥${String.format("%.0f", uiState.student!!.monthlyRate)}",
                                            fontSize = 12.sp,
                                            color = AppTextSecondary
                                        )
                                    }
                                }
                                Text(
                                    "结算日 ${uiState.student?.settlementDay ?: 1} 日",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // 账期切换
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { viewModel.previousMonth(studentId) },
                            modifier = Modifier.size(34.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(AppFill)
                        ) {
                            Icon(Icons.Filled.ChevronLeft, "上一账期", modifier = Modifier.size(18.dp))
                        }
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "${uiState.selectedYear}年${uiState.selectedMonth + 1}月 · ${DateUtils.formatWindow(uiState.window)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Text(
                                "账期已结束，可结算",
                                fontSize = 11.sp,
                                color = AppTextTertiary
                            )
                        }
                        IconButton(
                            onClick = { viewModel.nextMonth(studentId) },
                            modifier = Modifier.size(34.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(AppFill)
                        ) {
                            Icon(Icons.Filled.ChevronRight, "下一账期", modifier = Modifier.size(18.dp))
                        }
                    }
                }

                item {
                    Text("本账期未结算记录（${uiState.unsettledRecords.size} 次）",
                        fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AppTextSecondary)
                }
                if (uiState.unsettledRecords.isEmpty()) {
                    item {
                        AppCard {
                            AppEmptyState(
                                icon = Icons.Outlined.EventNote,
                                title = "本账期没有未结算记录"
                            )
                        }
                    }
                } else {
                    items(uiState.unsettledRecords) { record ->
                        UnsettledRecordCard(record)
                    }
                }

                item {
                    AppPrimaryButton(
                        text = "创建月结算",
                        icon = Icons.Outlined.CheckCircle,
                        enabled = uiState.unsettledRecords.isNotEmpty(),
                        onClick = { showCreateDialog = true }
                    )
                }

                if (uiState.settlements.isNotEmpty()) {
                    item {
                        AppSectionHeader("历史结算")
                    }
                    items(uiState.settlements) { settlement ->
                        SettlementCard(
                            settlement = settlement,
                            onMarkPaid = {
                                viewModel.markSettlementAsPaid(settlement) { ok ->
                                    message = if (ok) "已标记收款" else "该结算已收款"
                                }
                            },
                            onEdit = { editTarget = settlement },
                            onDelete = { deleteTarget = settlement }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateSettlementDialog(
            unsettledCount = uiState.unsettledRecords.size,
            defaultAmount = uiState.student?.monthlyRate ?: 0.0,
            window = uiState.window,
            onDismiss = { showCreateDialog = false },
            onConfirm = { amount, isPaid, notes ->
                viewModel.createSettlement(
                    studentId = studentId,
                    year = uiState.selectedYear,
                    month = uiState.selectedMonth,
                    amount = amount,
                    isPaid = isPaid,
                    notes = notes,
                    onComplete = {
                        showCreateDialog = false
                        message = "结算已创建"
                    }
                )
            }
        )
    }

    editTarget?.let { settlement ->
        EditSettlementDialog(
            settlement = settlement,
            onDismiss = { editTarget = null },
            onConfirm = { amount, isPaid, notes ->
                editTarget = null
                viewModel.updateSettlement(settlement, amount, isPaid, notes) {
                    message = "结算已更新"
                }
            }
        )
    }

    deleteTarget?.let { settlement ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("删除结算") },
            text = { Text("删除 ${settlement.year}年${settlement.month + 1}月结算后，关联记录将回到未结算状态，收入记录一并删除。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteTarget = null
                        viewModel.deleteSettlement(settlement) { message = "结算已删除" }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = AppError)
                ) { Text("删除") }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("取消") } }
        )
    }
}

@Composable
private fun UnsettledRecordCard(record: SessionRecord) {
    AppCard {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.width(52.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(DateUtils.formatDateDisplay(record.date),
                    fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(DateUtils.getWeekday(record.date), fontSize = 10.sp, color = AppTextTertiary)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("${record.startTime}–${record.endTime}", fontSize = 14.sp, color = AppTextSecondary)
                if (record.location.isNotBlank()) {
                    Text(record.location, fontSize = 12.sp, color = AppTextTertiary)
                }
            }
        }
    }
}

@Composable
private fun SettlementCard(
    settlement: MonthlySettlement,
    onMarkPaid: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    AppCard(
        containerColor = if (settlement.isPaid) AppSuccessBg.copy(alpha = 0.55f) else AppWarningBg.copy(alpha = 0.55f)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "${settlement.year}年${settlement.month + 1}月",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "${settlement.sessionCount} 次课 · ¥${String.format("%.0f", settlement.totalAmount)}",
                        fontSize = 13.sp,
                        color = AppTextSecondary
                    )
                    if (settlement.notes.isNotBlank()) {
                        Text(settlement.notes, fontSize = 12.sp, color = AppTextTertiary)
                    }
                }
                if (settlement.isPaid) {
                    AppPill("已收款", AppSuccess, AppSuccessBg)
                } else {
                    FilledTonalButton(
                        onClick = onMarkPaid,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = AppSuccessBg,
                            contentColor = AppSuccess
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("标记收款", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = AppDividerColor, thickness = 0.6.dp)
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onEdit, contentPadding = PaddingValues(horizontal = 8.dp)) {
                    Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("编辑", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                }
                TextButton(onClick = onDelete, contentPadding = PaddingValues(horizontal = 8.dp)) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, tint = AppError, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("删除", fontSize = 13.sp, color = AppError)
                }
            }
        }
    }
}

@Composable
private fun CreateSettlementDialog(
    unsettledCount: Int,
    defaultAmount: Double,
    window: Pair<Long, Long>,
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
                Surface(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                    color = AppFill
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.DateRange, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "账期 ${DateUtils.formatWindow(window)} · $unsettledCount 次课",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                AppTextField(
                    value = amount,
                    onValueChange = { amount = it; amountError = false },
                    label = "结算金额 *",
                    prefix = "¥",
                    isError = amountError,
                    supportingText = if (amountError) "请输入有效金额" else null
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isPaid,
                        onCheckedChange = { isPaid = it },
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                    )
                    Text("已收款", fontSize = 14.sp)
                }

                AppTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = "备注（可选）",
                    singleLine = false,
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val v = amount.toDoubleOrNull()
                    if (v == null || v <= 0) {
                        amountError = true
                    } else {
                        onConfirm(v, isPaid, notes.trim())
                    }
                },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) { Text("创建结算") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

@Composable
private fun EditSettlementDialog(
    settlement: MonthlySettlement,
    onDismiss: () -> Unit,
    onConfirm: (Double, Boolean, String) -> Unit
) {
    var amount by remember(settlement.id) {
        mutableStateOf(String.format("%.0f", settlement.totalAmount))
    }
    var isPaid by remember(settlement.id) { mutableStateOf(settlement.isPaid) }
    var notes by remember(settlement.id) { mutableStateOf(settlement.notes) }
    var amountError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑结算") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "${settlement.year}年${settlement.month + 1}月 · ${settlement.sessionCount} 次课",
                    fontSize = 13.sp,
                    color = AppTextSecondary
                )
                AppTextField(
                    value = amount,
                    onValueChange = { amount = it; amountError = false },
                    label = "结算金额",
                    prefix = "¥",
                    isError = amountError,
                    supportingText = if (amountError) "请输入有效金额" else null
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isPaid,
                        onCheckedChange = { isPaid = it },
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                    )
                    Text("已收款", fontSize = 14.sp)
                }
                AppTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = "备注（可选）",
                    singleLine = false,
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val v = amount.toDoubleOrNull()
                    if (v == null || v <= 0) {
                        amountError = true
                    } else {
                        onConfirm(v, isPaid, notes.trim())
                    }
                }
            ) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
