package com.teacher.journal.ui.session

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.teacher.journal.data.entity.PaymentStatus
import com.teacher.journal.data.entity.PaymentType
import com.teacher.journal.data.entity.SessionRecord
import com.teacher.journal.data.entity.Student
import com.teacher.journal.ui.components.*
import com.teacher.journal.ui.theme.*
import com.teacher.journal.util.DateUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionRecordScreen(
    preselectedStudentId: Long?,
    recordId: Long,
    onNavigateBack: () -> Unit,
    viewModel: SessionViewModel = hiltViewModel()
) {
    val students by viewModel.allStudents.collectAsState()
    val isEditing = recordId > 0

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
    var showDeleteDialog by remember { mutableStateOf(false) }
    var loadedRecord by remember { mutableStateOf<SessionRecord?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var startPickerHour by remember { mutableStateOf(14) }
    var endPickerHour by remember { mutableStateOf(16) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            message = null
        }
    }

    LaunchedEffect(recordId) {
        if (isEditing) {
            viewModel.getRecordOnce(recordId) { record ->
                if (record != null) {
                    loadedRecord = record
                    selectedStudent = students.find { it.id == record.studentId } ?: selectedStudent
                    date = record.date
                    startTime = record.startTime
                    endTime = record.endTime
                    location = record.location
                    content = record.content
                    amount = if (record.amount > 0) String.format("%.0f", record.amount) else ""
                    paymentStatus = record.paymentStatus
                }
            }
        }
    }

    LaunchedEffect(preselectedStudentId, students) {
        if (!isEditing && preselectedStudentId != null && selectedStudent == null) {
            selectedStudent = students.find { it.id == preselectedStudentId }
            location = selectedStudent?.location ?: ""
        }
    }

    LaunchedEffect(selectedStudent) {
        if (!isEditing && selectedStudent != null && location.isBlank()) {
            location = selectedStudent?.location ?: ""
        }
    }

    var studentError by remember { mutableStateOf(false) }
    var startTimeError by remember { mutableStateOf(false) }
    var endTimeError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = if (isEditing) "编辑记录" else "记录上课",
                onBack = onNavigateBack
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
            Spacer(Modifier.height(4.dp))

            // 学生选择
            ExposedDropdownMenuBox(
                expanded = studentExpanded,
                onExpandedChange = { if (!isEditing) studentExpanded = it }
            ) {
                AppTextField(
                    value = selectedStudent?.name ?: "",
                    onValueChange = {},
                    label = "选择学生 *",
                    readOnly = true,
                    isError = studentError,
                    supportingText = if (studentError) "请选择学生" else null,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = studentExpanded)
                    },
                    modifier = Modifier.menuAnchor()
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
                                    Spacer(Modifier.width(10.dp))
                                    AppTypeBadge(student.paymentType)
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
            AppTextField(
                value = DateUtils.formatDateFull(date),
                onValueChange = {},
                label = "上课日期",
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Outlined.CalendarMonth, contentDescription = "选择日期",
                            tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )

            // 时间
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                AppTextField(
                    value = startTime,
                    onValueChange = { startTime = it; startTimeError = false },
                    label = "开始时间",
                    placeholder = "HH:mm",
                    isError = startTimeError,
                    supportingText = if (startTimeError) "必填" else null,
                    trailingIcon = {
                        IconButton(onClick = {
                            val h = startTime.split(":").getOrNull(0)?.toIntOrNull()
                            showStartTimePicker = true
                            startPickerHour = h ?: 14
                        }) {
                            Icon(Icons.Outlined.Schedule, contentDescription = "选择时间",
                                tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                AppTextField(
                    value = endTime,
                    onValueChange = { endTime = it; endTimeError = false },
                    label = "结束时间",
                    placeholder = "HH:mm",
                    isError = endTimeError,
                    supportingText = if (endTimeError) "必填" else null,
                    trailingIcon = {
                        IconButton(onClick = {
                            val h = endTime.split(":").getOrNull(0)?.toIntOrNull()
                            showEndTimePicker = true
                            endPickerHour = h ?: 16
                        }) {
                            Icon(Icons.Outlined.Schedule, contentDescription = "选择时间",
                                tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            AppTextField(
                value = location,
                onValueChange = { location = it },
                label = "上课地点",
                singleLine = true
            )

            AppTextField(
                value = content,
                onValueChange = { content = it },
                label = "课程内容 / 备注",
                singleLine = false,
                maxLines = 3
            )

            // 付费模式提示
            selectedStudent?.let { student ->
                if (student.paymentType == PaymentType.PREPAID) {
                    val hint = if (isEditing) "预付费学生 · 编辑不重新扣课时" else "将从课时包中自动扣除 1 次"
                    InfoBanner(Icons.Outlined.EventNote, hint, MaterialTheme.colorScheme.primary)
                } else if (student.paymentType == PaymentType.MONTHLY) {
                    InfoBanner(Icons.Outlined.EventNote, "月结算模式 · 记录将归入对应结算周期", MaterialTheme.colorScheme.primary)
                } else {
                    InfoBanner(Icons.Outlined.Payments, "按次付费", MaterialTheme.colorScheme.primary)
                    AppTextField(
                        value = amount,
                        onValueChange = { amount = it; amountError = false },
                        label = "本次课时费（元）",
                        prefix = "¥",
                        isError = amountError,
                        supportingText = if (amountError) "请输入课时费金额" else null
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        PaymentChip(
                            label = "已收费", icon = Icons.Outlined.CheckCircle,
                            selected = paymentStatus == PaymentStatus.PAID,
                            color = AppSuccess,
                            onClick = { paymentStatus = PaymentStatus.PAID }
                        )
                        PaymentChip(
                            label = "待收费", icon = Icons.Outlined.MoneyOff,
                            selected = paymentStatus == PaymentStatus.UNPAID,
                            color = AppWarning,
                            onClick = { paymentStatus = PaymentStatus.UNPAID }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            AppPrimaryButton(
                text = if (isEditing) "保存修改" else "保存记录",
                onClick = {
                    var hasError = false
                    if (selectedStudent == null && !isEditing) {
                        studentError = true; hasError = true
                    }
                    if (startTime.isBlank()) { startTimeError = true; hasError = true }
                    if (endTime.isBlank()) { endTimeError = true; hasError = true }
                    if (selectedStudent?.paymentType == PaymentType.PER_SESSION && amount.isBlank()) {
                        amountError = true; hasError = true
                    }
                    if (hasError) return@AppPrimaryButton

                    val student = selectedStudent
                    if (isEditing && loadedRecord != null) {
                        viewModel.updateRecord(
                            recordId = recordId,
                            date = date, startTime = startTime.trim(),
                            endTime = endTime.trim(), location = location.trim(),
                            content = content.trim(),
                            amount = amount.toDoubleOrNull() ?: 0.0,
                            paymentStatus = paymentStatus,
                            onComplete = { onNavigateBack() }
                        )
                    } else if (student != null) {
                        viewModel.recordSession(
                            studentId = student.id,
                            date = date, startTime = startTime.trim(),
                            endTime = endTime.trim(), location = location.trim(),
                            content = content.trim(), student = student,
                            amount = amount.toDoubleOrNull() ?: 0.0,
                            paymentStatus = paymentStatus
                        ) { warning ->
                            if (warning != null) {
                                message = warning
                                scope.launch { delay(1800); onNavigateBack() }
                            } else {
                                onNavigateBack()
                            }
                        }
                    }
                }
            )

            if (isEditing) {
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppError.copy(alpha = 0.35f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppError)
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("删除这条记录", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    // 日期选择器
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = date)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { date = it }
                    showDatePicker = false
                }) { Text("确定") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("取消") } }
        ) { DatePicker(state = datePickerState) }
    }

    if (showStartTimePicker) {
        TimePickerDialog(
            onDismiss = { showStartTimePicker = false },
            onConfirm = { hour, minute ->
                startTime = String.format("%02d:%02d", hour, minute)
                showStartTimePicker = false
            },
            initialHour = startPickerHour
        )
    }
    if (showEndTimePicker) {
        TimePickerDialog(
            onDismiss = { showEndTimePicker = false },
            onConfirm = { hour, minute ->
                endTime = String.format("%02d:%02d", hour, minute)
                showEndTimePicker = false
            },
            initialHour = endPickerHour
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除上课记录") },
            text = { Text("删除后关联收入一并清除，已扣课时将自动回补。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteRecord(recordId) { onNavigateBack() }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = AppError)
                ) { Text("删除") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("取消") } }
        )
    }
}

@Composable
private fun InfoBanner(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    color: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.07f)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text(text, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = color)
        }
    }
}

@Composable
private fun PaymentChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(6.dp))
                Text(label, fontSize = 13.sp)
            }
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = color.copy(alpha = 0.12f),
            selectedLabelColor = color
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit,
    initialHour: Int,
    initialMinute: Int = 0
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
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TimePicker(state = state)
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour, state.minute) }) { Text("确定") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}