package com.teacher.journal.ui.student

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teacher.journal.data.entity.*
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
        topBar = {
            TopAppBar(
                title = { Text(uiState.student?.name ?: "学生详情", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "返回", tint = OnPrimary) } },
                actions = {
                    IconButton(onClick = onNavigateToEdit) { Icon(Icons.Outlined.Edit, contentDescription = "编辑", tint = OnPrimary) }
                    IconButton(onClick = { showDeleteDialog = true }) { Icon(Icons.Outlined.Delete, contentDescription = "删除", tint = OnPrimary) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Blue600, titleContentColor = OnPrimary)
            )
        }
    ) { padding ->
        if (uiState.isLoading || uiState.student == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Blue500) }
        } else {
            val student = uiState.student!!
            LazyColumn(
                Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // 学生信息卡
                item {
                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = SurfaceWhite), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(Modifier.size(48.dp), shape = RoundedCornerShape(14.dp), color = Blue50) {
                                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.Person, contentDescription = null, tint = Blue500, modifier = Modifier.size(28.dp)) }
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(student.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                    PaymentTypeBadge(student.paymentType)
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            if (student.phone.isNotBlank()) InfoRow(Icons.Outlined.Call, student.phone)
                            if (student.subject.isNotBlank()) InfoRow(Icons.Outlined.School, student.subject)
                            if (student.location.isNotBlank()) InfoRow(Icons.Outlined.LocationOn, student.location)
                            if (student.monthlyRate > 0) InfoRow(Icons.Outlined.Payments, "月薪 ¥${String.format("%.0f", student.monthlyRate)}")
                            if (student.notes.isNotBlank()) InfoRow(Icons.Outlined.Notes, student.notes)
                        }
                    }
                }

                // 预付费：课时包
                if (student.paymentType == PaymentType.PREPAID) {
                    item { RemainingSessionsCard(uiState.remainingSessions, onNavigateToPackagePurchase) }
                    if (uiState.coursePackages.isNotEmpty()) {
                        item { Text("课时包记录", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = Gray900) }
                        items(uiState.coursePackages) { pkg -> PackageHistoryCard(pkg) }
                    }
                }

                // 按次付费：待收费
                if (student.paymentType == PaymentType.PER_SESSION && uiState.unpaidRecords.isNotEmpty()) {
                    item {
                        Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = WarningBg)) {
                            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.PriorityHigh, contentDescription = null, tint = WarningOrange, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("${uiState.unpaidRecords.size} 笔待收费 · ¥${String.format("%.0f", uiState.unpaidRecords.sumOf { it.amount })}", fontWeight = FontWeight.Medium, color = WarningOrange)
                            }
                        }
                    }
                }

                // 月结算
                if (student.paymentType == PaymentType.MONTHLY) {
                    item { MonthlySettlementCard(uiState.monthlySettlements, student.monthlyRate, onNavigateToMonthlySettlement) }
                    if (uiState.monthlySettlements.isNotEmpty()) {
                        item { Text("月结算记录", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = Gray900) }
                        items(uiState.monthlySettlements) { s -> SettlementHistoryCard(s) }
                    }
                }

                // 上课记录
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("上课记录", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = Gray900)
                        TextButton(onClick = onNavigateToSessionRecord) { Text("记录上课", color = Blue600, fontWeight = FontWeight.Medium) }
                    }
                }
                if (uiState.sessionRecords.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("暂无上课记录", color = Gray400)
                        }
                    }
                } else {
                    items(uiState.sessionRecords) { record -> SessionRecordCard(record) }
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除") },
            text = { Text("删除学生「${uiState.student?.name}」将同时删除其所有相关数据，此操作不可撤销。") },
            confirmButton = { TextButton(onClick = { showDeleteDialog = false; viewModel.deleteStudent(studentId) { onNavigateBack() } }, colors = ButtonDefaults.textButtonColors(contentColor = ErrorRed)) { Text("删除") } },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("取消") } }
        )
    }
}

// ── 子组件 ──

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Gray400, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = Gray700)
    }
}

@Composable
private fun RemainingSessionsCard(remaining: Int, onBuy: () -> Unit) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Blue50)) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("剩余课时", style = MaterialTheme.typography.labelMedium, color = Gray600)
            Text("$remaining 次", style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Bold, color = Blue700)
            Spacer(Modifier.height(12.dp))
            Button(onClick = onBuy, colors = ButtonDefaults.buttonColors(containerColor = Blue600), shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("购买课时")
            }
        }
    }
}

@Composable
private fun MonthlySettlementCard(settlements: List<MonthlySettlement>, monthlyRate: Double, onNavigate: () -> Unit) {
    val unpaidCount = settlements.count { !it.isPaid }
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Blue50)) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatItem("累计结算", "${settlements.size}", Green600)
                StatItem("待收款", "$unpaidCount", if (unpaidCount > 0) WarningOrange else SuccessGreen)
                if (monthlyRate > 0) StatItem("月薪", "¥${String.format("%.0f", monthlyRate)}", Blue600)
            }
            if (unpaidCount > 0) {
                Spacer(Modifier.height(8.dp))
                Text("待收款 ¥${String.format("%.0f", settlements.filter { !it.isPaid }.sumOf { it.totalAmount })}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = WarningOrange)
            }
            Spacer(Modifier.height(12.dp))
            Button(onClick = onNavigate, colors = ButtonDefaults.buttonColors(containerColor = Green600), shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Outlined.DateRange, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("创建月结算")
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Gray500)
    }
}

@Composable
private fun SettlementHistoryCard(s: MonthlySettlement) {
    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = SurfaceWhite), elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("${s.year}年${s.month + 1}月", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text("${s.sessionCount} 次课 · ¥${String.format("%.0f", s.totalAmount)}", style = MaterialTheme.typography.bodySmall, color = Gray500)
            }
            Surface(shape = RoundedCornerShape(8.dp), color = if (s.isPaid) SuccessBg else WarningBg) {
                Text(if (s.isPaid) "已收款" else "待收款", Modifier.padding(horizontal = 10.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = if (s.isPaid) SuccessGreen else WarningOrange)
            }
        }
    }
}

@Composable
private fun PackageHistoryCard(pkg: CoursePackage) {
    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = SurfaceWhite), elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("购买 ${pkg.sessionsPurchased} 次 · ¥${String.format("%.0f", pkg.amount)}", style = MaterialTheme.typography.bodyMedium)
                Text("${DateUtils.formatDateFull(pkg.purchaseDate)} · 已用 ${pkg.usedCount} 次 · 剩余 ${pkg.remainingSessions} 次", style = MaterialTheme.typography.bodySmall, color = Gray500)
            }
            if (pkg.isExhausted) {
                Surface(shape = RoundedCornerShape(8.dp), color = Gray200) {
                    Text("已用完", Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = Gray500)
                }
            }
        }
    }
}

@Composable
private fun SessionRecordCard(record: SessionRecord) {
    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = SurfaceWhite), elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(Modifier.size(44.dp), shape = RoundedCornerShape(12.dp), color = Blue50) {
                Box(contentAlignment = Alignment.Center) { Text(DateUtils.formatDateDisplay(record.date), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Blue600) }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("${record.startTime} – ${record.endTime}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = Gray900)
                if (record.location.isNotBlank()) Text(record.location, style = MaterialTheme.typography.bodySmall, color = Gray500)
                if (record.content.isNotBlank()) Text(record.content, style = MaterialTheme.typography.bodySmall, color = Gray400, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            PaymentStatusBadge(record.paymentStatus)
        }
    }
}
