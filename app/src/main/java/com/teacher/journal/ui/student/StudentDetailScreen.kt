package com.teacher.journal.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
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
import com.teacher.journal.ui.home.PaymentStatusBadge
import com.teacher.journal.ui.theme.*
import com.teacher.journal.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDetailScreen(
    studentId: Long,
    onNavigateToEdit: () -> Unit,
    onNavigateToSessionRecord: () -> Unit,
    onNavigateToPackagePurchase: () -> Unit,
    onNavigateToMonthlySettlement: () -> Unit = {},
    onNavigateBack: () -> Unit,
    viewModel: StudentViewModel = hiltViewModel()
) {
    val uiState by viewModel.detailUiState.collectAsStateWithLifecycle()
    LaunchedEffect(studentId) { viewModel.loadStudentDetail(studentId) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = {},
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, "返回",
                            tint = MaterialTheme.colorScheme.primary)
                    }
                },
                actions = {
                    TextButton(onClick = onNavigateToEdit) {
                        Text("编辑", color = MaterialTheme.colorScheme.primary,
                            fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
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
            contentPadding = PaddingValues(top = 4.dp, bottom = 32.dp)
        ) {
            // Header: big avatar + name
            item {
                Column(
                    Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BigAvatar(student.name)
                    Spacer(Modifier.height(12.dp))
                    Text(student.name, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    PaymentTypeBadge(student.paymentType)
                }
            }

            // 信息分组卡
            val hasPhone = student.phone.isNotBlank()
            val hasSubject = student.subject.isNotBlank()
            val hasLocation = student.location.isNotBlank()
            val hasRate = student.monthlyRate > 0
            val hasNotes = student.notes.isNotBlank()
            val infoCount = listOf(hasPhone, hasSubject, hasLocation, hasRate, hasNotes).count { it }
            if (infoCount > 0) {
                item {
                    SectionHeader("基本信息")
                    var infoIdx = 0
                    GroupedCard {
                        if (hasPhone) {
                            InfoRow(Icons.Outlined.Call, "电话", student.phone)
                            infoIdx++
                            if (infoIdx < infoCount) RowDivider(startInset = 60.dp)
                        }
                        if (hasSubject) {
                            InfoRow(Icons.Outlined.School, "科目", student.subject)
                            infoIdx++
                            if (infoIdx < infoCount) RowDivider(startInset = 60.dp)
                        }
                        if (hasLocation) {
                            InfoRow(Icons.Outlined.LocationOn, "地点", student.location)
                            infoIdx++
                            if (infoIdx < infoCount) RowDivider(startInset = 60.dp)
                        }
                        if (hasRate) {
                            InfoRow(Icons.Outlined.Payments, "月薪", "¥${String.format("%.0f", student.monthlyRate)}")
                            infoIdx++
                            if (infoIdx < infoCount) RowDivider(startInset = 60.dp)
                        }
                        if (hasNotes) {
                            InfoRow(Icons.Outlined.Notes, "备注", student.notes)
                        }
                    }
                }
            }

            // 预付费：课时统计
            if (student.paymentType == PaymentType.PREPAID) {
                item {
                    SectionHeader("课时包")
                    RemainingSessionsCard(uiState.remainingSessions, onNavigateToPackagePurchase)
                }
                if (uiState.coursePackages.isNotEmpty()) {
                    item {
                        SectionHeader("购买记录")
                        GroupedCard {
                            uiState.coursePackages.forEachIndexed { i, pkg ->
                                PackageHistoryRow(pkg)
                                if (i < uiState.coursePackages.lastIndex) RowDivider(startInset = 16.dp)
                            }
                        }
                    }
                }
            }

            // 按次付费待收费提示
            if (student.paymentType == PaymentType.PER_SESSION && uiState.unpaidRecords.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = WarningBg),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Payments, contentDescription = null,
                                tint = WarningOrange, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "${uiState.unpaidRecords.size} 笔待收费 · ¥${String.format("%.0f", uiState.unpaidRecords.sumOf { it.amount })}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = WarningOrange
                            )
                        }
                    }
                }
            }

            // 月结算
            if (student.paymentType == PaymentType.MONTHLY) {
                item {
                    SectionHeader("月结算")
                    MonthlySettlementSummary(uiState.monthlySettlements, student.monthlyRate, onNavigateToMonthlySettlement)
                }
                if (uiState.monthlySettlements.isNotEmpty()) {
                    item {
                        SectionHeader("结算记录")
                        GroupedCard {
                            uiState.monthlySettlements.forEachIndexed { i, s ->
                                SettlementHistoryRow(s)
                                if (i < uiState.monthlySettlements.lastIndex) RowDivider(startInset = 16.dp)
                            }
                        }
                    }
                }
            }

            // 上课记录
            item {
                SectionHeader("上课记录", action = {
                    TextButton(onClick = onNavigateToSessionRecord, contentPadding = PaddingValues(0.dp)) {
                        Text("记录", color = MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                })
            }
            if (uiState.sessionRecords.isEmpty()) {
                item {
                    GroupedCard {
                        Box(Modifier.fillMaxWidth().padding(28.dp), contentAlignment = Alignment.Center) {
                            Text("暂无上课记录", fontSize = 15.sp, color = AppleLabelSecondary)
                        }
                    }
                }
            } else {
                item {
                    GroupedCard {
                        uiState.sessionRecords.forEachIndexed { i, record ->
                            SessionRecordRow(record)
                            if (i < uiState.sessionRecords.lastIndex) RowDivider(startInset = 60.dp)
                        }
                    }
                }
            }

            // Delete
            item {
                Spacer(Modifier.height(24.dp))
                GroupedCard {
                    ListRow(
                        title = "删除学生",
                        titleColor = ErrorRed,
                        onClick = { showDeleteDialog = true }
                    )
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
                    colors = ButtonDefaults.textButtonColors(contentColor = ErrorRed)
                ) { Text("删除") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("取消") } }
        )
    }
}

// ── Sub components ──
@Composable
private fun BigAvatar(name: String) {
    val bg = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    Box(
        Modifier.size(76.dp).clip(CircleShape).background(bg),
        contentAlignment = Alignment.Center
    ) {
        Text(name.take(1), fontSize = 32.sp, fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    ListRow(
        title = label,
        leading = {
            Icon(icon, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        },
        trailing = { TrailingValue(value) }
    )
}

@Composable
private fun RemainingSessionsCard(remaining: Int, onBuy: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("剩余课时", fontSize = 13.sp, color = AppleLabelSecondary)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text("$remaining", fontSize = 44.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface, lineHeight = 48.sp)
                Text(" 次", fontSize = 17.sp, color = AppleLabelSecondary,
                    modifier = Modifier.padding(bottom = 8.dp))
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onBuy,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("购买课时", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun MonthlySettlementSummary(
    settlements: List<MonthlySettlement>,
    monthlyRate: Double,
    onNavigate: () -> Unit
) {
    val unpaidCount = settlements.count { !it.isPaid }
    val unpaidAmount = settlements.filter { !it.isPaid }.sumOf { it.totalAmount }
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SummaryStat("累计结算", "${settlements.size}")
                SummaryStat("待收款", "$unpaidCount",
                    if (unpaidCount > 0) WarningOrange else SuccessGreen)
                if (monthlyRate > 0) SummaryStat("月薪", "¥${String.format("%.0f", monthlyRate)}")
            }
            if (unpaidAmount > 0) {
                Spacer(Modifier.height(10.dp))
                Text("待收款 ¥${String.format("%.0f", unpaidAmount)}",
                    fontSize = 13.sp, color = WarningOrange, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(14.dp))
            OutlinedButton(
                onClick = onNavigate,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(),
                border = androidx.compose.foundation.BorderStroke(1.dp, AppleSeparator),
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
        Text(label, fontSize = 12.sp, color = AppleLabelSecondary)
        Spacer(Modifier.height(2.dp))
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
private fun SettlementHistoryRow(s: MonthlySettlement) {
    ListRow(
        title = "${s.year}年${s.month + 1}月",
        subtitle = "${s.sessionCount} 次课 · ¥${String.format("%.0f", s.totalAmount)}",
        trailing = {
            if (s.isPaid) TrailingPill("已收款", SuccessGreen, SuccessBg)
            else TrailingPill("待收款", WarningOrange, WarningBg)
        }
    )
}

@Composable
private fun PackageHistoryRow(pkg: CoursePackage) {
    ListRow(
        title = "购买 ${pkg.sessionsPurchased} 次 · ¥${String.format("%.0f", pkg.amount)}",
        subtitle = "${DateUtils.formatDateFull(pkg.purchaseDate)} · 已用 ${pkg.usedCount} · 剩余 ${pkg.remainingSessions}",
        trailing = {
            if (pkg.isExhausted) TrailingPill("已用完", AppleLabelSecondary, AppleFill)
        }
    )
}

@Composable
private fun SessionRecordRow(record: SessionRecord) {
    ListRow(
        title = "${DateUtils.formatDateDisplay(record.date)} · ${record.startTime}–${record.endTime}",
        subtitle = buildString {
            if (record.location.isNotBlank()) append(record.location)
            if (record.content.isNotBlank()) {
                if (isNotEmpty()) append(" · ")
                append(record.content)
            }
        }.ifBlank { null },
        leading = { MiniDateChip(record.date) },
        trailing = { PaymentStatusBadge(record.paymentStatus) }
    )
}

@Composable
private fun MiniDateChip(date: Long) {
    Box(
        Modifier.size(width = 36.dp, height = 36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            DateUtils.formatDateDisplay(date),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
