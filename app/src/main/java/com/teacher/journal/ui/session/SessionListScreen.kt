package com.teacher.journal.ui.session

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teacher.journal.data.entity.PaymentStatus
import com.teacher.journal.ui.home.PaymentStatusBadge
import com.teacher.journal.ui.theme.*
import com.teacher.journal.util.DateUtils

private val WEEK_LABELS = listOf("一", "二", "三", "四", "五", "六", "日")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionListScreen(
    viewModel: SessionViewModel = hiltViewModel()
) {
    val uiState by viewModel.listUiState.collectAsStateWithLifecycle()
    var selectedDay by remember { mutableStateOf<CalendarDay?>(null) }

    // 切回月视图时自动刷新
    LaunchedEffect(Unit) {
        viewModel.loadRecordsForMonth(uiState.currentYear, uiState.currentMonth)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text("上课记录", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = MaterialTheme.colorScheme.onSurface)
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // 月份切换 + 统计
            Card(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.previousMonth() }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Filled.ChevronLeft, "上月", modifier = Modifier.size(22.dp))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${uiState.currentYear}年${uiState.currentMonth + 1}月", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("${uiState.totalMonthSessions} 次课 · ¥${String.format("%.0f", uiState.totalMonthAmount)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { viewModel.nextMonth() }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Filled.ChevronRight, "下月", modifier = Modifier.size(22.dp))
                    }
                }
            }

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                LazyColumn(
                    Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    // 星期头
                    item {
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            WEEK_LABELS.forEachIndexed { i, label ->
                                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                    Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium,
                                        color = if (i == 6) ErrorRed else MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center)
                                }
                            }
                        }
                    }

                    // 日历网格
                    val days = uiState.calendarDays
                    val rows = days.chunked(7)
                    items(rows) { week ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                            week.forEach { day ->
                                Box(Modifier.weight(1f).aspectRatio(1.15f).padding(1.dp)) {
                                    if (day.dayOfMonth > 0) {
                                        CalendarDayCell(day, selectedDay == day) {
                                            selectedDay = if (selectedDay == day) null else day
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 点击日期弹出详情
    selectedDay?.let { day ->
        AlertDialog(
            onDismissRequest = { selectedDay = null },
            title = { Text(DateUtils.formatDateFull(day.date), fontWeight = FontWeight.Bold) },
            text = {
                if (day.sessions.isEmpty()) {
                    Text("当天没有上课记录", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("${day.sessionCount} 次课 · ¥${String.format("%.0f", day.totalAmount)}", fontWeight = FontWeight.Medium, color = Primary)
                        day.sessions.forEach { session ->
                            Card(
                                Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(Modifier.padding(10.dp)) {
                                    Text("${uiState.students[session.studentId]?.name ?: "未知"} · ${session.startTime} – ${session.endTime}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                    if (session.location.isNotBlank()) Text(session.location, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    if (session.amount > 0) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("¥${String.format("%.0f", session.amount)}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Tertiary)
                                            Spacer(Modifier.width(8.dp))
                                            PaymentStatusBadge(session.paymentStatus)
                                        }
                                    } else if (session.coursePackageId > 0) {
                                        Text("📦 课时包扣除", style = MaterialTheme.typography.labelSmall, color = Green600)
                                    } else if (session.settlementId > 0) {
                                        Text("📅 已结算", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { selectedDay = null }) { Text("关闭") } }
        )
    }
}

@Composable
private fun CalendarDayCell(day: CalendarDay, selected: Boolean, onClick: () -> Unit) {
    val hasSessions = day.sessionCount > 0
    val hasAmount = day.totalAmount > 0
    val bgColor = when {
        selected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        hasSessions -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        else -> Color.Transparent
    }
    val textColor = when {
        selected -> MaterialTheme.colorScheme.primary
        hasSessions -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    }

    Box(
        Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)).background(bgColor).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("${day.dayOfMonth}", fontSize = 12.sp, fontWeight = if (hasSessions) FontWeight.Bold else FontWeight.Normal, color = textColor)
            if (hasSessions) {
                Text("${day.sessionCount}次", fontSize = 9.sp, fontWeight = FontWeight.Medium, color = textColor.copy(alpha = 0.8f))
                if (hasAmount) {
                    Text("¥${String.format("%.0f", day.totalAmount)}", fontSize = 8.sp, color = Tertiary, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}
