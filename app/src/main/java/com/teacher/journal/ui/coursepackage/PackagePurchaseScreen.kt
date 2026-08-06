package com.teacher.journal.ui.coursepackage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.teacher.journal.ui.components.*
import com.teacher.journal.ui.theme.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun PackagePurchaseScreen(
    studentId: Long,
    onNavigateBack: () -> Unit,
    viewModel: PackageViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var sessionCount by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var countError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }

    LaunchedEffect(studentId) { viewModel.loadStudent(studentId) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { AppTopBar(title = "购买课时", onBack = onNavigateBack) }
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

            // 学生信息卡片
            AppCard {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    AppAvatar(uiState.studentName, size = 44.dp)
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text("学生", fontSize = 12.sp, color = AppTextSecondary)
                        Spacer(Modifier.height(2.dp))
                        Text(uiState.studentName, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            AppTextField(
                value = sessionCount,
                onValueChange = { sessionCount = it; countError = false },
                label = "购买次数 *",
                suffix = "次",
                isError = countError,
                supportingText = if (countError) "请输入有效购买次数" else null
            )

            AppTextField(
                value = amount,
                onValueChange = { amount = it; amountError = false },
                label = "金额 *",
                prefix = "¥",
                isError = amountError,
                supportingText = if (amountError) "请输入有效金额" else null
            )

            AppTextField(
                value = notes,
                onValueChange = { notes = it },
                label = "备注（可选）",
                singleLine = false,
                maxLines = 2
            )

            val count = sessionCount.toIntOrNull()
            val total = amount.toDoubleOrNull()
            if (count != null && total != null && count > 0 && total > 0) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                ) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.ShoppingBag, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "约合 ¥${String.format("%.0f", total / count)} / 次",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            AppPrimaryButton(
                text = "确认购买",
                onClick = {
                    var hasError = false
                    if (sessionCount.isBlank() || sessionCount.toIntOrNull() == null || sessionCount.toInt() <= 0) {
                        countError = true; hasError = true
                    }
                    if (amount.isBlank() || amount.toDoubleOrNull() == null || amount.toDouble() <= 0) {
                        amountError = true; hasError = true
                    }
                    if (hasError) return@AppPrimaryButton

                    viewModel.purchasePackage(
                        studentId = studentId,
                        sessionCount = sessionCount.toInt(),
                        amount = amount.toDouble(),
                        notes = notes.trim()
                    ) { onNavigateBack() }
                }
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}