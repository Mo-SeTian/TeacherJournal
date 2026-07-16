package com.teacher.journal.ui.coursepackage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.graphics.Color
import com.teacher.journal.ui.theme.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
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

    LaunchedEffect(studentId) {
        viewModel.loadStudent(studentId)
    }

    Scaffold(
        topBar = {
            TopAppBar(windowInsets = WindowInsets(0,0,0,0),
                title = { Text("购买课时") },
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

            // 学生名
            Card(
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text(
                        text = "学生：",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                    Text(
                        text = uiState.studentName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // 购买次数
            OutlinedTextField(
                value = sessionCount,
                onValueChange = { sessionCount = it; countError = false },
                label = { Text("购买次数 *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = countError,
                supportingText = if (countError) {{ Text("请输入购买次数") }} else null,
                suffix = { Text("次") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    focusedLabelColor = Primary
                )
            )

            // 金额
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it; amountError = false },
                label = { Text("金额 *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = amountError,
                supportingText = if (amountError) {{ Text("请输入金额") }} else null,
                prefix = { Text("¥") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    focusedLabelColor = Primary
                )
            )

            // 购买日期
            OutlinedTextField(
                value = "今天（系统自动记录）",
                onValueChange = {},
                readOnly = true,
                label = { Text("购买日期") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = TextTertiary.copy(alpha = 0.2f),
                    disabledTextColor = TextSecondary
                )
            )

            // 备注
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("备注") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    focusedLabelColor = Primary
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 单价预览
            if (sessionCount.isNotBlank() && amount.isNotBlank()) {
                val count = sessionCount.toIntOrNull()
                val total = amount.toDoubleOrNull()
                if (count != null && total != null && count > 0) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.05f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Text("💡 约合 ")
                            Text(
                                text = "¥${String.format("%.0f", total / count)}/次",
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    var hasError = false
                    if (sessionCount.isBlank() || sessionCount.toIntOrNull() == null || sessionCount.toInt() <= 0) {
                        countError = true
                        hasError = true
                    }
                    if (amount.isBlank() || amount.toDoubleOrNull() == null || amount.toDouble() <= 0) {
                        amountError = true
                        hasError = true
                    }
                    if (hasError) return@Button

                    viewModel.purchasePackage(
                        studentId = studentId,
                        sessionCount = sessionCount.toInt(),
                        amount = amount.toDouble(),
                        notes = notes.trim()
                    ) { onNavigateBack() }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text("确认购买", style = MaterialTheme.typography.titleSmall)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
