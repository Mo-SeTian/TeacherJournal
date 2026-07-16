package com.teacher.journal.ui.session

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.teacher.journal.data.entity.PaymentStatus
import com.teacher.journal.data.entity.PaymentType
import com.teacher.journal.data.entity.Student
import com.teacher.journal.ui.theme.*
import com.teacher.journal.util.DateUtils
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionRecordScreen(
    preselectedStudentId: Long?,
    onNavigateBack: () -> Unit,
    viewModel: SessionViewModel = hiltViewModel()
) {
    val students by viewModel.allStudents.collectAsState()

    var selectedStudent by remember { mutableStateOf<Student?>(null) }
    var date by remember { mutableStateOf(DateUtils.getTodayStart()) }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var paymentStatus by remember { mutableStateOf(PaymentStatus.PAID) }

    var studentExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    // 预选学生
    LaunchedEffect(preselectedStudentId, students) {
        if (preselectedStudentId != null && selectedStudent == null) {
            selectedStudent = students.find { it.id == preselectedStudentId }
            // 预填默认地点
            location = selectedStudent?.location ?: ""
        }
    }

    // 学生变化时预填地点
    LaunchedEffect(selectedStudent) {
        if (selectedStudent != null && location.isBlank()) {
            location = selectedStudent?.location ?: ""
        }
    }

    var dateError by remember { mutableStateOf(false) }
    var startTimeError by remember { mutableStateOf(false) }
    var endTimeError by remember { mutableStateOf(false) }
    var studentError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(windowInsets = WindowInsets(0,0,0,0),
                title = { Text("记录上课") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回", tint = OnPrimary)
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 选择学生
            ExposedDropdownMenuBox(
                expanded = studentExpanded,
                onExpandedChange = { studentExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedStudent?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("选择学生 *") },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = studentExpanded) },
                    isError = studentError,
                    supportingText = if (studentError) {{ Text("请选择学生") }} else null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        focusedLabelColor = Primary
                    )
                )
                ExposedDropdownMenu(
                    expanded = studentExpanded,
                    onDismissRequest = { studentExpanded = false }
                ) {
                    students.forEach { student ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(student.name)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (student.paymentType == PaymentType.PREPAID) "预付费" else "按次付",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextTertiary
                                    )
                                }
                            },
                            onClick = {
                                selectedStudent = student
                                studentExpanded = false
                                studentError = false
                            }
                        )
                    }
                }
            }

            // 日期
            OutlinedTextField(
                value = DateUtils.formatDateFull(date),
                onValueChange = {},
                readOnly = true,
                label = { Text("上课日期") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "选择日期")
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    focusedLabelColor = Primary
                )
            )

            // 时间
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = startTime,
                    onValueChange = { startTime = it; startTimeError = false },
                    label = { Text("开始时间") },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("HH:mm") },
                    singleLine = true,
                    isError = startTimeError,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        focusedLabelColor = Primary
                    )
                )
                OutlinedTextField(
                    value = endTime,
                    onValueChange = { endTime = it; endTimeError = false },
                    label = { Text("结束时间") },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("HH:mm") },
                    singleLine = true,
                    isError = endTimeError,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        focusedLabelColor = Primary
                    )
                )
            }

            // 地点
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("上课地点") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    focusedLabelColor = Primary
                )
            )

            // 课程内容
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("课程内容 / 备注") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    focusedLabelColor = Primary
                )
            )

            // 扣除方式 / 收费（根据学生类型）
            selectedStudent?.let { student ->
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                if (student.paymentType == PaymentType.PREPAID) {
                    Text(
                        "📦 将从课时包中扣除 1 次",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Secondary
                    )
                } else if (student.paymentType == PaymentType.MONTHLY) {
                    Text(
                        "📅 月结算模式 — 记录将归入本月结算",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Secondary
                    )
                } else {
                    // 按次付费：金额 + 收费状态
                    Text(
                        "💰 按次付费",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                    )

                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it; amountError = false },
                        label = { Text("本次课时费（元）") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = amountError,
                        supportingText = if (amountError) {{ Text("请输入课时费金额") }} else null,
                        prefix = { Text("¥") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            focusedLabelColor = Primary
                        )
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FilterChip(
                            selected = paymentStatus == PaymentStatus.PAID,
                            onClick = { paymentStatus = PaymentStatus.PAID },
                            label = { Text("✅ 已收费") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StatusPaid.copy(alpha = 0.15f),
                                selectedLabelColor = StatusPaid
                            )
                        )
                        FilterChip(
                            selected = paymentStatus == PaymentStatus.UNPAID,
                            onClick = { paymentStatus = PaymentStatus.UNPAID },
                            label = { Text("🟡 待收费") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StatusUnpaid.copy(alpha = 0.15f),
                                selectedLabelColor = StatusUnpaid
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 保存
            Button(
                onClick = {
                    var hasError = false
                    if (selectedStudent == null) {
                        studentError = true
                        hasError = true
                    }
                    if (startTime.isBlank()) {
                        startTimeError = true
                        hasError = true
                    }
                    if (endTime.isBlank()) {
                        endTimeError = true
                        hasError = true
                    }
                    if (selectedStudent?.paymentType == PaymentType.PER_SESSION && amount.isBlank()) {
                        amountError = true
                        hasError = true
                    }
                    if (hasError) return@Button

                    viewModel.recordSession(
                        studentId = selectedStudent!!.id,
                        date = date,
                        startTime = startTime.trim(),
                        endTime = endTime.trim(),
                        location = location.trim(),
                        content = content.trim(),
                        student = selectedStudent!!,
                        amount = amount.toDoubleOrNull() ?: 0.0,
                        paymentStatus = paymentStatus,
                        onComplete = { onNavigateBack() }
                    )
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("保存记录", style = MaterialTheme.typography.titleSmall)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // 日期选择器
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = date
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { date = it }
                    showDatePicker = false
                }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // 时间选择器
    if (showStartTimePicker) {
        TimePickerDialog(
            onDismiss = { showStartTimePicker = false },
            onConfirm = { hour, minute ->
                startTime = String.format("%02d:%02d", hour, minute)
                showStartTimePicker = false
            },
            initialHour = 14,
            initialMinute = 0
        )
    }
    if (showEndTimePicker) {
        TimePickerDialog(
            onDismiss = { showEndTimePicker = false },
            onConfirm = { hour, minute ->
                endTime = String.format("%02d:%02d", hour, minute)
                showEndTimePicker = false
            },
            initialHour = 16,
            initialMinute = 0
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit,
    initialHour: Int,
    initialMinute: Int
) {
    val state = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择时间") },
        text = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TimePicker(state = state)
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour, state.minute) }) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
