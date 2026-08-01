package com.teacher.journal.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notes
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
import com.teacher.journal.data.entity.*
import com.teacher.journal.ui.components.*
import com.teacher.journal.ui.theme.*
import com.teacher.journal.util.DateUtils

@Composable
fun StudentDetailScreen(
    studentId: Long,
    onNavigateToEdit: () -> Unit,
    onNavigateToSessionRecord: () -> Unit,
    onNavigateToPackagePurchase: () -> Unit,
    onNavigateToMonthlySettlement: () -> Unit = {},
    onEditRecord: (Long) -> Unit = {},
    onNavigateBack: () -> Unit,
    viewModel: StudentViewModel = hiltViewModel()
) {
    val uiState by viewModel.detailUiState.collectAsStateWithLifecycle()
    LaunchedEffect(studentId) { viewModel.loadStudentDetail(studentId) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var refundTarget by remember { mutableStateOf<CoursePackage?>(null) }
    var recordToDelete by remember { mutableStateOf<SessionRecord?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            message = null
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = "学生详情",
                onBack = onNavigateBack,
                actions = {
                    TextButton(onClick = onNavigateToEdit) {
                        Text("编辑", color = MaterialTheme.colorScheme.primary, fontSize = 15.sp)
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading || uiState.student == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Scaffold
        }

        val student = uiState.student!!
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 32.dp)
        ) {
            item {
                Column(
                    Modifier.fillMaxWidth().padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AppAvatar(student.name, size = 72.dp, fontSize = 28)
                    Spacer(Modifier.height(12.dp))
                    Text(student.name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    AppTypeBadge(student.paymentType)
                }
            }

            // 基本信息
            val infoRows = buildList {
                if (student.phone.isNotBlank()) add(Triple(Icons.Outlined.Call, "电话", student.phone))
                if (student.subject.isNotBlank()) add(Triple(Icons.Outlined.School, "科目", student.subject))
                if (student.location.isNotBlank()) add(Triple(Icons.Outlined.LocationOn, "地点", student.location))
                if (student.monthlyRate > 0) add(Triple(Icons.Outlined.Payments, "月薪", "¥${fmt(student.monthlyRate)}"))
                if (student.notes.isNotBlank()) add(Triple(Icons.Outlined.Notes, "备注", student.notes))
            }
            if (infoRows.isNotEmpty()) {
                item {
                    AppSectionHeader("基本信息")
                    AppCard {
                        infoRows.forEachIndexed { i, (icon, label, value) ->
                            AppRow(
                                title = label,
                                leading = {
                                    Icon(icon, contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                },
                                trailing = {
                                    Text(value, fontSize = 15.sp, color = AppTextSecondary)
                                }
                            )
                            if (i < infoRows.lastIndex) AppDivider()
                        }
                    }
                }
            }

            // 预付费：课时包
            if (student.paymentType == PaymentType.PREPAID) {
                item {
                    AppSectionHeader("课时包")
                    RemainingSessionsCard(uiState.remainingSessions, onNavigateToPackagePurchase)
                }
                if (uiState.coursePackages.isNotEmpty()) {
                    item {
                        AppSectionHeader("购买记录")
                        AppCard {
                            uiState.coursePackages.forEachIndexed { i, pkg ->
                                PackageHistoryRow(pkg) {
                                    refundTarget = pkg
                                }
                                if (i < uiState.coursePackages.lastIndex) AppDivider()
                            }
                        }
                    }
                }
            }

            // 按次付费：待收费提示
            if (student.paymentType == PaymentType.PER_SESSION && uiState.unpaidRecords.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                        color = AppWarningBg
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Payments, contentDescription = null,
                                tint = AppWarning, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "${uiState.unpaidRecords.size} 笔待收费 · ¥${fmt(uiState.unpaidRecords.sumOf { it.amount })}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = AppWarning
                            )
                        }
                    }
                }
            }

            // 月结算
            if (student.paymentType == PaymentType.MONTHLY) {
                item {
                    AppSectionHeader("月结算")
                    MonthlySettlementSummary(
                        settlements = uiState.monthlySettlements,
                        monthlyRate = student.monthlyRate,
                        onManage = onNavigateToMonthlySettlement
                    )
                }
            }

            // 上课记录
            item {
                AppSectionHeader("上课记录", action = {
                    TextButton(onClick = onNavigateToSessionRecord, contentPadding = PaddingValues(0.dp)) {
                        Text("记录", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                })
            }
            if (uiState.sessionRecords.isEmpty()) {
                item {
                    AppCard {
                        AppEmptyState(
                            icon = Icons.Outlined.DateRange,
                            title = "暂无上课记录",
                            subtitle = "点击右上角「记录」开始"
                        )
                    }
                }
            } else {
                item {
                    AppCard {
                        uiState.sessionRecords.forEachIndexed { i, record ->
                            SessionRecordRow(
                                record = record,
                                onEdit = { onEditRecord(record.id) },
                                onDelete = { recordToDelete = record }
                            )
                            if (i < uiState.sessionRecords.lastIndex) AppDivider()
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(20.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                    color = AppErrorBg
                ) {
                    Row(
                        Modifier.clip(androidx.compose.foundation.shape.RoundedCornerShape(14.dp))
                            .background(AppErrorBg)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Delete, contentDescription = null, tint = AppError, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("删除学生", color = AppError, fontSize = 15.sp, fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f))
                        TextButton(onClick = { showDeleteDialog = true }) {
                            Text("删除", color = AppError)
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除") },
            text = { Text("删除学生「${uiState.student?.name}」将同时删除其所有相关数据，此操作不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteStudent(studentId) { onNavigateBack() }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = AppError)
                ) { Text("删除") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("取消") } }
        )
    }

    refundTarget?.let { pkg ->
        RefundDialog(
            pkg = pkg,
            onDismiss = { refundTarget = null },
            onConfirm = { amount ->
                refundTarget = null
                viewModel.refundPackage(pkg.id, amount) { ok ->
                    message = if (ok) "已退款 ${fmt(amount)} 元" else "退款失败：课时包已退款或已用完"
                }
            }
        )
    }

    recordToDelete?.let { record ->
        AlertDialog(
            onDismissRequest = { recordToDelete = null },
            title = { Text("删除上课记录") },
            text = { Text("将删除 ${DateUtils.formatDateFull(record.date)} ${record.startTime}–${record.endTime} 的记录，关联收入一并删除，已扣课时自动回补。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        recordToDelete = null
                        viewModel.deleteRecord(record.id) { message = "记录已删除" }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = AppError)
                ) { Text("删除") }
            },
            dismissButton = { TextButton(onClick = { recordToDelete = null }) { Text("取消") } }
        )
    }
}

private fun fmt(v: Double): String = String.format("%.0f", v)

@Composable
private fun RemainingSessionsCard(remaining: Int, onBuy: () -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    AppGradientCard(colors = listOf(primary, tertiary.copy(alpha = 0.8f))) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("剩余课时", fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("$remaining", fontSize = 44.sp, fontWeight = FontWeight.Bold,
                        color = Color.White, lineHeight = 48.sp)
                    Text(" 次", fontSize = 16.sp, color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(bottom = 7.dp))
                }
            }
            Button(
                onClick = onBuy,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = primary),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("购买课时", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun MonthlySettlementSummary(
    settlements: List<MonthlySettlement>,
    monthlyRate: Double,
    onManage: () -> Unit
) {
    val unpaidCount = settlements.count { !it.isPaid }
    val unpaidAmount = settlements.filter { !it.isPaid }.sumOf { it.totalAmount }
    AppCard {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SummaryStat("累计结算", "${settlements.size}")
                SummaryStat("待收款", "$unpaidCount",
                    if (unpaidCount > 0) AppWarning else AppSuccess)
                if (monthlyRate > 0) SummaryStat("月薪", "¥${fmt(monthlyRate)}")
            }
            if (unpaidAmount > 0) {
                Spacer(Modifier.height(10.dp))
                Text("待收款 ¥${fmt(unpaidAmount)}", fontSize = 13.sp, color = AppWarning, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(14.dp))
            OutlinedButton(
                onClick = onManage,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                border = androidx.compose.foundation.BorderStroke(1.dp, AppDividerColor),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("管理月结算", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun SummaryStat(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Column {
        Text(label, fontSize = 12.sp, color = AppTextSecondary)
        Spacer(Modifier.height(2.dp))
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
private fun PackageHistoryRow(pkg: CoursePackage, onRefund: () -> Unit) {
    val canRefund = pkg.remainingSessions > 0 && !pkg.isRefunded
    AppRow(
        title = "购买 ${pkg.sessionsPurchased} 次 · ¥${fmt(pkg.amount)}",
        subtitle = when {
            pkg.isRefunded -> "${DateUtils.formatDateFull(pkg.refundDate)} · 已退 ${pkg.refundedSessions} 次 ¥${fmt(pkg.refundAmount)}"
            pkg.isExhausted -> "${DateUtils.formatDateFull(pkg.purchaseDate)} · 已用完"
            else -> "${DateUtils.formatDateFull(pkg.purchaseDate)} · 已用 ${pkg.usedCount} · 剩余 ${pkg.remainingSessions}"
        },
        trailing = {
            when {
                pkg.isRefunded -> AppPill("已退款", AppTextSecondary, AppFill)
                pkg.isExhausted -> AppPill("已用完", AppTextSecondary, AppFill)
                canRefund -> TextButton(onClick = onRefund, contentPadding = PaddingValues(0.dp)) {
                    Text("退款", color = AppWarning, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    )
}

@Composable
private fun SessionRecordRow(record: SessionRecord, onEdit: () -> Unit, onDelete: () -> Unit) {
    AppRow(
        title = "${DateUtils.formatDateDisplay(record.date)} · ${record.startTime}–${record.endTime}",
        subtitle = buildString {
            if (record.location.isNotBlank()) append(record.location)
            if (record.content.isNotBlank()) {
                if (isNotEmpty()) append(" · ")
                append(record.content)
            }
        }.ifBlank { null },
        leading = { AppMiniDateBadge(record.date) },
        trailing = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppStatusBadge(record.paymentStatus)
                Spacer(Modifier.width(6.dp))
                IconButton(onClick = onEdit, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Outlined.Edit, contentDescription = "编辑", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Outlined.Delete, contentDescription = "删除", tint = AppError, modifier = Modifier.size(16.dp))
                }
            }
        }
    )
}

@Composable
private fun RefundDialog(
    pkg: CoursePackage,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    val unitPrice = if (pkg.sessionsPurchased > 0) pkg.amount / pkg.sessionsPurchased else 0.0
    val default = unitPrice * pkg.remainingSessions
    var amount by remember(pkg.id) {
        mutableStateOf(if (default > 0) String.format("%.0f", default) else "")
    }
    var amountError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("退课时包") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "剩余 ${pkg.remainingSessions} 次，按购买单价约合 ¥${String.format("%.0f", default)}。退款将计入负收入。",
                    fontSize = 14.sp,
                    color = AppTextSecondary
                )
                AppTextField(
                    value = amount,
                    onValueChange = { amount = it; amountError = false },
                    label = "退款金额（元）",
                    prefix = "¥",
                    isError = amountError,
                    supportingText = if (amountError) "请输入有效金额" else null
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
                        onConfirm(v)
                    }
                },
                colors = ButtonDefaults.textButtonColors(contentColor = AppWarning)
            ) { Text("确认退款") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
