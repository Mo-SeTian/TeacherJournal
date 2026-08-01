package com.teacher.journal.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teacher.journal.ui.components.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var message by remember { mutableStateOf<String?>(null) }
    var confirmImport by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            message = null
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.exportData(uri) { message = it }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pendingImportUri = uri
            confirmImport = true
        }
    }

    val fileName = remember {
        "授业札记备份-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".json"
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(title = "设置", onBack = onNavigateBack)
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 32.dp)
        ) {
            item {
                AppScreenTitle(title = "设置")
                Spacer(Modifier.height(8.dp))
            }

            item { AppSectionHeader("外观") }
            item {
                AppCard {
                    uiState.themes.forEachIndexed { i, theme ->
                        val selected = theme.id == uiState.currentThemeId
                        ThemeRow(theme.name, theme.primary, theme.secondary, theme.tertiary, selected) {
                            viewModel.selectTheme(theme.id)
                        }
                        if (i < uiState.themes.lastIndex) AppDivider()
                    }
                }
            }

            item { AppSectionHeader("数据管理") }
            item {
                AppCard {
                    AppRow(
                        title = "导出备份",
                        subtitle = "保存为 JSON 文件，换机/重装后可恢复",
                        leading = {
                            Icon(Icons.Outlined.FileDownload, contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        },
                        showChevron = true,
                        onClick = { exportLauncher.launch(fileName) }
                    )
                    AppDivider()
                    AppRow(
                        title = "导入备份",
                        subtitle = "从备份文件恢复全部数据（会替换当前数据）",
                        leading = {
                            Icon(Icons.Outlined.FileUpload, contentDescription = null,
                                tint = AppWarning, modifier = Modifier.size(20.dp))
                        },
                        showChevron = true,
                        onClick = { importLauncher.launch(arrayOf("application/json")) }
                    )
                }
            }

            item { AppSectionHeader("关于") }
            item {
                AppCard {
                    AppRow(title = "版本", trailing = { Text("2.0.0", color = AppTextSecondary, fontSize = 15.sp) })
                    AppDivider()
                    AppRow(
                        title = "授业札记",
                        subtitle = "com.teacher.journal · 数据仅保存在本机",
                        leading = {
                            Icon(Icons.Outlined.Info, contentDescription = null,
                                tint = AppTextSecondary, modifier = Modifier.size(20.dp))
                        }
                    )
                }
            }

            if (uiState.isImporting) {
                item {
                    Spacer(Modifier.height(16.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("正在导入…", fontSize = 13.sp, color = AppTextSecondary)
                    }
                }
            }
        }
    }

    if (confirmImport) {
        AlertDialog(
            onDismissRequest = { confirmImport = false },
            title = { Text("导入备份") },
            text = { Text("导入将替换当前全部数据（学生、记录、收入、结算）。建议先导出备份，再执行导入。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmImport = false
                        val uri = pendingImportUri ?: return@TextButton
                        viewModel.importData(uri) { message = it }
                        pendingImportUri = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = AppWarning)
                ) { Text("开始导入") }
            },
            dismissButton = { TextButton(onClick = { confirmImport = false }) { Text("取消") } }
        )
    }
}

@Composable
private fun ThemeRow(
    name: String,
    primary: Color,
    secondary: Color,
    tertiary: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    AppRow(
        title = name,
        leading = { ThreeDotSwatch(primary, secondary, tertiary) },
        trailing = {
            if (selected) {
                Icon(Icons.Filled.Check, contentDescription = "已选",
                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
        },
        onClick = onClick
    )
}

@Composable
private fun ThreeDotSwatch(a: Color, b: Color, c: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Swatch(a)
        Swatch(b)
        Swatch(c)
    }
}

@Composable
private fun Swatch(color: Color) {
    Box(
        Modifier.size(16.dp).clip(CircleShape).background(color)
            .border(0.5.dp, AppDividerColor, CircleShape)
    )
}
