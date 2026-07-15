package com.teacher.journal.ui.student

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teacher.journal.data.entity.CoursePackage
import com.teacher.journal.data.entity.PaymentType
import com.teacher.journal.data.entity.SessionRecord
import com.teacher.journal.ui.home.PaymentStatusChip
import com.teacher.journal.ui.theme.*
import com.teacher.journal.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDetailScreen(
    studentId: Long,
    onNavigateToEdit: () -> Unit,
    onNavigateToSessionRecord: () -> Unit,
    onNavigateToPackagePurchase: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: StudentViewModel = hiltViewModel()
) {
    val uiState by viewModel.detailUiState.collectAsStateWithLifecycle()

    LaunchedEffect(studentId) {
        viewModel.loadStudentDetail(studentId)
    }

    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.student?.name ?: "学生详情") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回", tint = OnPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToEdit) {
                        Icon(Icons.Filled.Edit, contentDescription = "编辑", tint = OnPrimary)
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "删除", tint = OnPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = OnPrimary
                )
            )
        }
    ) { padding ->
        if (uiState.isLoading || uiState.student == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            val student = uiState.student!!

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // 学生信息卡片
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = student.name,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                PaymentTypeChip(student.paymentType)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            if (student.phone.isNotBlank()) {
                                InfoRow("📱", student.phone)
                            }
                            if (student.subject.isNotBlank()) {
                                InfoRow("📚", student.subject)
                            }
                            if (student.location.isNotBlank()) {
                                InfoRow("📍", student.location)
                            }
                            if (student.notes.isNotBlank()) {
                                InfoRow("📝", student.notes)
                            }
                        }
                    }
                }

                // 课时包区域（仅预付费学生）
                if (student.paymentType == PaymentType.PREPAID) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Primary.copy(alpha = 0.05f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "剩余课时",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "${uiState.remainingSessions} 次",
                                    style = MaterialTheme.typography.displayLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Primary
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = onNavigateToPackagePurchase,
                                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("购买课时")
                                }
                            }
                        }
                    }

                    // 课时包历史
                    if (uiState.coursePackages.isNotEmpty()) {
                        item {
                            Text(
                                "📦 课时包记录",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        items(uiState.coursePackages) { pkg ->
                            PackageHistoryCard(pkg)
                        }
                    }
                } else {
                    // 按次付费学生 - 待收费提醒
                    if (uiState.unpaidRecords.isNotEmpty()) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = StatusUnpaid.copy(alpha = 0.08f)
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "🔔 待收费 · ${uiState.unpaidRecords.size} 笔 · ¥${
                                            String.format("%.0f", uiState.unpaidRecords.sumOf { it.amount })
                                        }",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = StatusUnpaid
                                    )
                                }
                            }
                        }
                    }
                }

                // 上课记录
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "📋 上课记录",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        TextButton(onClick = onNavigateToSessionRecord) {
                            Text("＋ 记录上课", color = Primary)
                        }
                    }
                }

                if (uiState.sessionRecords.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("暂无上课记录", color = TextTertiary)
                        }
                    }
                } else {
                    items(uiState.sessionRecords) { record ->
                        SessionRecordCard(record)
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }

    // 删除确认对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除") },
            text = { Text("删除学生「${uiState.student?.name}」将同时删除其所有课时包和上课记录，此操作不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteStudent(studentId) { onNavigateBack() }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = StatusOverdue)
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun InfoRow(icon: String, text: String) {
    Row(modifier = Modifier.padding(vertical = 3.dp)) {
        Text(text = icon, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary
        )
    }
}

@Composable
private fun PackageHistoryCard(pkg: CoursePackage) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "购买 ${pkg.sessionsPurchased} 次 · ¥${String.format("%.0f", pkg.amount)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${DateUtils.formatDateFull(pkg.purchaseDate)} · 已用 ${pkg.usedCount} 次 · 剩余 ${pkg.remainingSessions} 次",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            if (pkg.isExhausted) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = TextTertiary.copy(alpha = 0.15f)
                ) {
                    Text(
                        "已用完",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun SessionRecordCard(record: SessionRecord) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.width(48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
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
                    Text(text = record.location, style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                }
                if (record.content.isNotBlank()) {
                    Text(text = record.content, style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                }
                if (record.amount > 0) {
                    Text(
                        text = "¥${String.format("%.0f", record.amount)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Tertiary
                    )
                }
            }
            PaymentStatusChip(record.paymentStatus)
        }
    }
}
