package com.teacher.journal.ui.student

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.teacher.journal.data.entity.PaymentType
import com.teacher.journal.data.entity.Student
import androidx.compose.ui.graphics.Color
import com.teacher.journal.ui.theme.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentEditScreen(
    studentId: Long?,
    onNavigateBack: () -> Unit,
    viewModel: StudentViewModel = hiltViewModel()
) {
    val uiState by viewModel.detailUiState.collectAsStateWithLifecycle()
    val isEditing = studentId != null

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var paymentType by remember { mutableStateOf(PaymentType.PREPAID) }
    var monthlyRate by remember { mutableStateOf("") }
    var settlementDay by remember { mutableStateOf("1") }
    var notes by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }

    // 加载现有数据
    LaunchedEffect(studentId) {
        if (studentId != null) {
            viewModel.loadStudentDetail(studentId)
        }
    }

    // 填充表单
    LaunchedEffect(uiState.student) {
        val s = uiState.student
        if (isEditing && s != null && name.isEmpty()) {
            name = s.name
            phone = s.phone
            subject = s.subject
            location = s.location
            paymentType = s.paymentType
            monthlyRate = if (s.monthlyRate > 0) String.format("%.0f", s.monthlyRate) else ""
            settlementDay = s.settlementDay.toString()
            notes = s.notes
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(windowInsets = WindowInsets(0,0,0,0),
                title = { Text(if (isEditing) "编辑学生" else "添加学生") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
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

            // 姓名
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; nameError = false },
                label = { Text("姓名 *") },
                modifier = Modifier.fillMaxWidth(),
                isError = nameError,
                supportingText = if (nameError) {{ Text("请输入学生姓名") }} else null,
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    focusedLabelColor = Primary
                )
            )

            // 电话
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("联系电话") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    focusedLabelColor = Primary
                )
            )

            // 科目
            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text("所学科目") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    focusedLabelColor = Primary
                )
            )

            // 默认地点
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("默认上课地点") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    focusedLabelColor = Primary
                )
            )

            // 付费类型
            Text(
                "付费类型",
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = paymentType == PaymentType.PREPAID,
                    onClick = { paymentType = PaymentType.PREPAID },
                    label = { Text("课时包") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Primary.copy(alpha = 0.15f),
                        selectedLabelColor = Primary
                    )
                )
                FilterChip(
                    selected = paymentType == PaymentType.PER_SESSION,
                    onClick = { paymentType = PaymentType.PER_SESSION },
                    label = { Text("按次付") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Tertiary.copy(alpha = 0.15f),
                        selectedLabelColor = Tertiary
                    )
                )
                FilterChip(
                    selected = paymentType == PaymentType.MONTHLY,
                    onClick = { paymentType = PaymentType.MONTHLY },
                    label = { Text("月结算") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Secondary.copy(alpha = 0.15f),
                        selectedLabelColor = Secondary
                    )
                )
            }

            // 月薪字段（仅月结算模式显示）
            if (paymentType == PaymentType.MONTHLY) {
                OutlinedTextField(
                    value = monthlyRate,
                    onValueChange = { monthlyRate = it },
                    label = { Text("月薪/月费（元，可选）") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    prefix = { Text("¥") },
                    supportingText = { Text("创建结算时自动填充此金额，可手动修改") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        focusedLabelColor = Primary
                    )
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = settlementDay,
                    onValueChange = { s -> s.toIntOrNull()?.let { v -> if (v in 1..28) settlementDay = v.toString() } },
                    label = { Text("每月结算日（1-28）") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    suffix = { Text("日") },
                    supportingText = { Text("每月此日自动提醒创建月结算") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        focusedLabelColor = Primary
                    )
                )
            }

            // 备注
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("备注") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    focusedLabelColor = Primary
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 保存按钮
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                        return@Button
                    }
                    if (isEditing && studentId != null) {
                        viewModel.updateStudent(
                            id = studentId,
                            name = name.trim(),
                            phone = phone.trim(),
                            subject = subject.trim(),
                            location = location.trim(),
                            paymentType = paymentType,
                            monthlyRate = monthlyRate.toDoubleOrNull() ?: 0.0,
                            settlementDay = settlementDay.toIntOrNull() ?: 1,
                            notes = notes.trim()
                        ) { onNavigateBack() }
                    } else {
                        viewModel.insertStudent(
                            name = name.trim(),
                            phone = phone.trim(),
                            subject = subject.trim(),
                            location = location.trim(),
                            paymentType = paymentType,
                            monthlyRate = monthlyRate.toDoubleOrNull() ?: 0.0,
                            settlementDay = settlementDay.toIntOrNull() ?: 1,
                            notes = notes.trim()
                        ) { onNavigateBack() }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text(
                    if (isEditing) "保存修改" else "添加学生",
                    style = MaterialTheme.typography.titleSmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
