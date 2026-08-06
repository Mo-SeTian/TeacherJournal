package com.teacher.journal.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
                title = uiState.student?.name ?: "学生详情",
                onBack = onNavigateBack,
                actions = {
                    IconButton(onClick = onNavigateToEdit) {
                        Icon(Icons.Outlined.Edit, contentDescription = "编辑",
                            tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Outlined.Delete, contentDescription = "删除",
                            tint = AppError)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToSessionRecord,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = androidx.compose.ui.graphics.Color.White,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 4.dp, pressedElevation = 8.dp
                )
            ) {
                Icon(Icons.Filled.Add, contentDescription = "记录上课")
            }
        }
    ) { padding ->
        val student = uiState.student
        if (uiState.isLoading || student == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 100.dp)
            ) {
                // ── 基本信息卡片 ──
                item {
                    AppCard {
                        Column(Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AppAvatar(name = student.name, size = 56.dp)
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text(
                                        student.name,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    AppTypeBadge(student.paymentType)
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            if (student.subject.isNotBlank()) {
                                DetailInfoRow(Icons.Outlined.School, "科目", student.subject)
                            }
                            if (student.phone.isNotBlank()) {
                                DetailInfoRow(Icons.Outlined.Call, "电话", student.phone)
                            }
                            if (student.location.isNotBlank()) {
                                DetailInfoRow(Icons.Outlined.LocationOn, "地点", student.location)
                            }
                            if (student.paymentType == PaymentType.MONTHLY) {
                                DetailInfoRow(Icons.Outlined.Payments,
                                    "月薪", if (student.monthlyRate > 0) "¥${String.format("%.0f", student.monthlyRate)}" else "未设置")
                                DetailInfoRow(Icons.Outlined.DateRange,
                                    "结算日", "每月 ${student.settlementDay} 日")
                            }
                            if (student.notes.isNotBlank()) {
                                DetailInfoRow(Icons.Outlined.Notes, "备注", student.notes)
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // ── 课时包信息 ──
                if (student.paymentType == PaymentType.PREPAID) {
                    item {
                        AppSectionHeader("课时包")
                    }
                    item {
                        AppCard {
                            if (uiState.coursePackages.isEmpty()) {
                                AppEmptyState(
                                    icon = Icons.Outlined.ShoppingBag,
                                    title = "暂无课时包",
                                    subtitle = "点击下方按钮购买"
                                )
                            } else {
                                uiState.coursePackages.forEachIndexed { i, pkg ->
                                    PackageRow(
                                        pkg = pkg,
                                        totalSessions = pkg.sessionsPurchased,
                                        remainingSessions = pkg.remainingSessions,
                                        amount = pkg.amount,
                                        createdAt = pkg.purchaseDate,
                                        notes = pkg.notes
                                    )
                                    if (i < uiState.coursePackages.lastIndex) AppDivider()
                                }
                            }
                            HorizontalDivider(color = AppDividerColor, thickness = 0.5.dp)
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToPackagePurchase() }
                                    .padding(18.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.Add, contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("购买课时包", fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }

                // ── 月结算信息 ──
                if (student.paymentType == PaymentType.MONTHLY) {
                    item {
                        AppSectionHeader("月结算")
                    }
                    item {
                        AppCard {
                            if (uiState.monthlySettlements.isEmpty()) {
                                AppEmptyState(
                                    icon = Icons.Outlined.Payments,
                                    title = "暂无月结算记录",
                                    subtitle = "点击下方按钮创建结算"
                                )
                            } else {
                                uiState.monthlySettlements.take(3).forEachIndexed { i, settlement ->
                                    SettlementRow(settlement = settlement)
                                    if (i < minOf(uiState.monthlySettlements.size, 3) - 1) AppDivider()
                                }
                            }
                            HorizontalDivider(color = AppDividerColor, thickness = 0.5.dp)
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable(onClick = onNavigateToMonthlySettlement)
                                    .padding(18.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text("查看全部结算", fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }

                // ── 上课记录 ──
                item {
                    AppSectionHeader("最近上课记录")
                }
                item {
                    AppCard {
                        if (uiState.sessionRecords.isEmpty()) {
                            AppEmptyState(
                                icon = Icons.Outlined.EventNote,
                                title = "还没有上课记录",
                                subtitle = "点击右下角 + 记录第一节课"
                            )
                        } else {
                            uiState.sessionRecords.take(10).forEachIndexed { i, record ->
                                RecordRow(
                                    record = record,
                                    onEdit = { onEditRecord(record.id) },
                                    onDelete = {
                                        viewModel.deleteRecord(record.id) { message = "记录已删除" }
                                    }
                                )
                                if (i < minOf(uiState.sessionRecords.size, 10) - 1) AppDivider()
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除学生") },
            text = { Text("删除后将同时删除该学生的所有上课记录、课时包和收入。此操作不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteStudent(studentId) {
                            onNavigateBack()
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = AppError)
                ) { Text("确认删除") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("取消") } }
        )
    }
}

@Composable
private fun DetailInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null,
            tint = AppTextSecondary, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, fontSize = 13.sp, color = AppTextSecondary,
            modifier = Modifier.width(40.dp))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun PackageRow(
    pkg: CoursePackage,
    totalSessions: Int,
    remainingSessions: Int,
    amount: Double,
    createdAt: Long,
    notes: String
) {
    val progress = if (totalSessions > 0) remainingSessions.toFloat() / totalSessions else 0f
    val progressColor = when {
        remainingSessions <= 0 -> AppError
        remainingSessions <= 2 -> AppWarning
        else -> AppSuccess
    }

    Column(Modifier.padding(18.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                "剩余 $remainingSessions / $totalSessions 次",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "¥${String.format("%.0f", amount)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(10.dp))
        AppProgressBar(progress = progress, color = progressColor)
        Spacer(Modifier.height(4.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                "约 ¥${String.format("%.0f", if (totalSessions > 0) amount / totalSessions else 0.0)}/次",
                fontSize = 12.sp,
                color = AppTextSecondary
            )
            Text(
                DateUtils.formatDate(createdAt),
                fontSize = 12.sp,
                color = AppTextSecondary
            )
        }
        if (notes.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(notes, fontSize = 12.sp, color = AppTextSecondary)
        }
    }
}

@Composable
private fun SettlementRow(settlement: MonthlySettlement) {
    AppRow(
        title = "${settlement.year}年${settlement.month}月",
        subtitle = "${settlement.sessionCount}次课 · ${DateUtils.formatDate(settlement.createdAt)}",
        trailing = {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "¥${String.format("%.0f", settlement.totalAmount)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                AppStatusBadge(
                    if (settlement.isPaid) PaymentStatus.PAID else PaymentStatus.UNPAID
                )
            }
        }
    )
}

@Composable
private fun RecordRow(
    record: SessionRecord,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    AppRow(
        title = "${record.startTime}–${record.endTime}",
        subtitle = buildString {
            if (record.content.isNotBlank()) append(record.content)
            if (record.location.isNotBlank()) {
                if (isNotEmpty()) append(" · ")
                append(record.location)
            }
        },
        leading = {
            AppDateBadge(record.date)
        },
        trailing = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                when {
                    record.amount > 0 -> {
                        Text("¥${String.format("%.0f", record.amount)}",
                            fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.width(6.dp))
                        AppStatusBadge(record.paymentStatus)
                    }
                    record.coursePackageId > 0 -> AppPill("扣课时", AppSuccess, AppSuccessLight)
                    record.settlementId > 0 -> AppPill("已结算",
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                }
                Spacer(Modifier.width(6.dp))
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Outlined.Edit, contentDescription = "编辑",
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Outlined.Delete, contentDescription = "删除",
                        tint = AppError, modifier = Modifier.size(16.dp))
                }
            }
        }
    )
}