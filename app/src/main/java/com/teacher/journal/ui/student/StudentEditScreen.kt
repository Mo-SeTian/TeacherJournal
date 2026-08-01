package com.teacher.journal.ui.student

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.MoneyOff
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.teacher.journal.data.entity.PaymentType
import com.teacher.journal.ui.components.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle

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

    LaunchedEffect(studentId) {
        if (studentId != null) {
            viewModel.loadStudentDetail(studentId)
        }
    }

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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                title = if (isEditing) "编辑学生" else "添加学生",
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(12.dp))

            AppTextField(
                value = name,
                onValueChange = { name = it; nameError = false },
                label = "姓名 *",
                isError = nameError,
                supportingText = if (nameError) "请输入学生姓名" else null
            )

            AppTextField(value = phone, onValueChange = { phone = it }, label = "联系电话")
            AppTextField(value = subject, onValueChange = { subject = it }, label = "所学科目")
            AppTextField(value = location, onValueChange = { location = it }, label = "默认上课地点")

            Text("付费类型", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = AppTextSecondary,
                modifier = Modifier.padding(top = 4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TypeChip(
                    label = "课时包",
                    icon = Icons.Outlined.EventNote,
                    selected = paymentType == PaymentType.PREPAID,
                    color = AppSuccess,
                    onClick = { paymentType = PaymentType.PREPAID }
                )
                TypeChip(
                    label = "按次付",
                    icon = Icons.Outlined.MoneyOff,
                    selected = paymentType == PaymentType.PER_SESSION,
                    color = AppWarning,
                    onClick = { paymentType = PaymentType.PER_SESSION }
                )
                TypeChip(
                    label = "月结算",
                    icon = Icons.Outlined.Payments,
                    selected = paymentType == PaymentType.MONTHLY,
                    color = MaterialTheme.colorScheme.primary,
                    onClick = { paymentType = PaymentType.MONTHLY }
                )
            }

            if (paymentType == PaymentType.MONTHLY) {
                AppTextField(
                    value = monthlyRate,
                    onValueChange = { monthlyRate = it },
                    label = "月薪/月费（元，可选）",
                    prefix = "¥",
                    supportingText = "创建结算时自动填充此金额，可手动修改"
                )
                AppTextField(
                    value = settlementDay,
                    onValueChange = { s ->
                        s.toIntOrNull()?.let { v -> if (v in 1..28) settlementDay = v.toString() }
                    },
                    label = "每月结算日（1-28）",
                    suffix = "日",
                    supportingText = "按结算日切分账期：如 25 日，即每月 25 日结上一账期"
                )
            }

            AppTextField(
                value = notes,
                onValueChange = { notes = it },
                label = "备注",
                singleLine = false,
                maxLines = 3
            )

            Spacer(Modifier.height(6.dp))

            AppPrimaryButton(
                text = if (isEditing) "保存修改" else "添加学生",
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                        return@AppPrimaryButton
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
                }
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TypeChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(5.dp))
                Text(label, fontSize = 13.sp)
            }
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = color.copy(alpha = 0.14f),
            selectedLabelColor = color
        )
    )
}
