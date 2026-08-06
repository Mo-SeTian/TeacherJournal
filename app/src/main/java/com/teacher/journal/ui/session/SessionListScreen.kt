package com.teacher.journal.ui.session

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teacher.journal.data.entity.SessionRecord
import com.teacher.journal.ui.components.*
import com.teacher.journal.ui.theme.*
import com.teacher.journal.util.DateUtils
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

private val WEEK_LABELS = listOf("一", "二", "三", "四", "五", "六", "日")

@Composable
fun SessionListScreen(
    onEditRecord: (Long, Long) -> Unit = { _, _ -> },
    viewModel: SessionViewModel = hiltViewModel()
) {
    val uiState by viewModel.listUiState.collectAsStateWithLifecycle()
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    var recordToDelete by remember { mutableStateOf<SessionRecord?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            message = null
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadRecordsForMonth(uiState.currentYear, uiState.currentMonth)
    }

    val today = LocalDate.now()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp)
        ) {
            item {
                AppScreenTitle(
                    title = "${uiState.currentYear}年${uiState.currentMonth + 1}月",
                    subtitle = "${uiState.totalMonthSessions} 次课 · 已收 ¥${String.format("%.0f", uiState.totalMonthAmount)}"
                )
                Spacer(Modifier.height(8.dp))
            }

            // 月份切换
            item {
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            selectedDate = null
                            viewModel.previousMonth()
                        },
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(Neutral100)
                    ) {
                        Icon(Icons.Filled.ChevronLeft, "上月",
                            tint = Neutral700, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = {
                        val (y, m) = DateUtils.getCurrentYearMonth()
                        selectedDate = DateUtils.getTodayStart()
                        viewModel.loadRecordsForMonth(y, m)
                    }) {
                        Text("回到今天", fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium)
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton(
                        onClick = {
                            selectedDate = null
                            viewModel.nextMonth()
                        },
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(Neutral100)
                    ) {
                        Icon(Icons.Filled.ChevronRight, "下月",
                            tint = Neutral700, modifier = Modifier.size(20.dp))
                    }
                }
            }

            if (uiState.isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().padding(top = 60.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            } else {
                // 日历卡片
                item {
                    AppCard {
                        Column(Modifier.padding(vertical = 14.dp, horizontal = 8.dp)) {
                            // 星期标题
                            Row(Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                                WEEK_LABELS.forEachIndexed { i, label ->
                                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                        Text(
                                            label,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (i >= 5) AppError.copy(alpha = 0.5f) else AppTextSecondary,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }

                            uiState.calendarDays.chunked(7).forEach { week ->
                                Row(Modifier.fillMaxWidth()) {
                                    week.forEach { day ->
                                        Box(
                                            Modifier.weight(1f).aspectRatio(1f).padding(2.dp)
                                        ) {
                                            if (day.dayOfMonth > 0) {
                                                val isToday = uiState.currentYear == today.year &&
                                                        uiState.currentMonth == today.monthValue - 1 &&
                                                        day.dayOfMonth == today.dayOfMonth
                                                CalendarDayCell(
                                                    day = day,
                                                    isToday = isToday,
                                                    isSelected = day.date == selectedDate,
                                                    onClick = {
                                                        selectedDate = if (selectedDate == day.date) null else day.date
                                                    },
                                                    onAdjacentClick = {
                                                        val local = Instant.ofEpochMilli(day.date)
                                                            .atZone(ZoneId.systemDefault()).toLocalDate()
                                                        selectedDate = day.date
                                                        viewModel.loadRecordsForMonth(local.year, local.monthValue - 1)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 选中日期的记录
                val sel = uiState.calendarDays.find { it.date == selectedDate }
                if (sel != null) {
                    item {
                        AppSectionHeader(
                            "${DateUtils.formatDateFull(sel.date)} · ${sel.sessionCount} 次课"
                        )
                    }
                    if (sel.sessions.isEmpty()) {
                        item {
                            AppCard {
                                AppEmptyState(
                                    icon = Icons.Outlined.EventNote,
                                    title = "当天没有上课记录"
                                )
                            }
                        }
                    } else {
                        item {
                            AppCard {
                                sel.sessions.forEachIndexed { i, s ->
                                    DaySessionRow(
                                        record = s,
                                        studentName = uiState.students[s.studentId]?.name ?: "未知",
                                        onEdit = { onEditRecord(s.studentId, s.id) },
                                        onDelete = { recordToDelete = s }
                                    )
                                    if (i < sel.sessions.lastIndex) AppDivider()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    recordToDelete?.let { record ->
        AlertDialog(
            onDismissRequest = { recordToDelete = null },
            title = { Text("删除上课记录") },
            text = { Text("删除后关联收入一并清除，已扣课时自动回补。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        recordToDelete = null
                        viewModel.deleteRecord(record.id) { message = "记录已删除" }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = AppError)
                ) { Text("删除") }
            },
            dismissButton = { TextButton(onClick = { recordToDelete = null }) { Text("取消") } }
        )
    }
}

@Composable
private fun DaySessionRow(
    record: SessionRecord,
    studentName: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    AppRow(
        title = studentName,
        subtitle = buildString {
            append("${record.startTime}–${record.endTime}")
            if (record.location.isNotBlank()) append(" · ${record.location}")
            if (record.content.isNotBlank()) append(" · ${record.content}")
        },
        trailing = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                when {
                    record.amount > 0 -> {
                        Text("¥${String.format("%.0f", record.amount)}",
                            fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.width(6.dp))
                        AppStatusBadge(record.paymentStatus)
                    }
                    record.coursePackageId > 0 -> AppPill("扣课时", AppSuccess, AppSuccessLight)
                    record.settlementId > 0 -> AppPill("已结算",
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                }
                Spacer(Modifier.width(6.dp))
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Outlined.Edit, contentDescription = "编辑",
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Outlined.Delete, contentDescription = "删除",
                        tint = AppError, modifier = Modifier.size(16.dp))
                }
            }
        }
    )
}

@Composable
private fun CalendarDayCell(
    day: CalendarDay,
    isToday: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onAdjacentClick: () -> Unit = {}
) {
    val primary = MaterialTheme.colorScheme.primary
    val hasSessions = day.sessionCount > 0

    if (day.isAdjacentMonth) {
        Box(
            Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .clickable(onClick = onAdjacentClick),
            contentAlignment = Alignment.Center
        ) {
            Text("${day.dayOfMonth}", fontSize = 13.sp, color = AppTextTertiary)
        }
        return
    }

    val bgColor = when {
        isSelected -> primary
        isToday -> primary.copy(alpha = 0.08f)
        else -> Color.Transparent
    }
    val textColor = when {
        isSelected -> Color.White
        isToday -> primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        Modifier.fillMaxSize()
            .clip(CircleShape)
            .background(bgColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "${day.dayOfMonth}",
                fontSize = 15.sp,
                fontWeight = if (isToday || isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = textColor
            )
            if (hasSessions) {
                Spacer(Modifier.height(2.dp))
                Box(
                    Modifier.size(4.dp).clip(CircleShape)
                        .background(if (isSelected) Color.White.copy(alpha = 0.8f) else primary)
                )
            }
        }
    }
}