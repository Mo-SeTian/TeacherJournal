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
import com.teacher.journal.ui.components.*
import com.teacher.journal.ui.home.PaymentStatusBadge
import com.teacher.journal.ui.theme.*
import com.teacher.journal.util.DateUtils
import java.util.Calendar

private val WEEK_LABELS = listOf("一", "二", "三", "四", "五", "六", "日")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionListScreen(
    viewModel: SessionViewModel = hiltViewModel()
) {
    val uiState by viewModel.listUiState.collectAsStateWithLifecycle()
    var selectedDay by remember { mutableStateOf<CalendarDay?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadRecordsForMonth(uiState.currentYear, uiState.currentMonth)
    }

    // 今日
    val todayCal = remember { Calendar.getInstance() }
    val todayYear = todayCal.get(Calendar.YEAR)
    val todayMonth = todayCal.get(Calendar.MONTH)
    val todayDay = todayCal.get(Calendar.DAY_OF_MONTH)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = {},
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp)
        ) {
            item {
                LargeTitle(
                    title = "${uiState.currentYear}年${uiState.currentMonth + 1}月",
                    subtitle = "${uiState.totalMonthSessions} 次课 · ¥${String.format("%.0f", uiState.totalMonthAmount)} 已收"
                )
                Spacer(Modifier.height(4.dp))
            }

            // Month toggle bar
            item {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.previousMonth() },
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(AppleFill)
                    ) {
                        Icon(Icons.Filled.ChevronLeft, "上月",
                            tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = {
                        val (y, m) = DateUtils.getCurrentYearMonth()
                        viewModel.loadRecordsForMonth(y, m)
                    }) {
                        Text("今天", fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium)
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton(
                        onClick = { viewModel.nextMonth() },
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(AppleFill)
                    ) {
                        Icon(Icons.Filled.ChevronRight, "下月",
                            tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
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
                // Calendar card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                    ) {
                        Column(Modifier.padding(vertical = 12.dp, horizontal = 8.dp)) {
                            // Week labels
                            Row(Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
                                WEEK_LABELS.forEachIndexed { i, label ->
                                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                        Text(
                                            label,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (i == 6) ErrorRed.copy(alpha = 0.7f) else AppleLabelSecondary,
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
                                                val isToday = uiState.currentYear == todayYear &&
                                                        uiState.currentMonth == todayMonth &&
                                                        day.dayOfMonth == todayDay
                                                CalendarDayCell(
                                                    day = day,
                                                    isToday = isToday,
                                                    isSelected = selectedDay == day,
                                                    onClick = {
                                                        selectedDay = if (selectedDay == day) null else day
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

                // Selected day detail sheet-like list
                val sel = selectedDay
                if (sel != null && sel.sessions.isNotEmpty()) {
                    item {
                        SectionHeader(
                            "${DateUtils.formatDateFull(sel.date)} · ${sel.sessionCount} 次课"
                        )
                        GroupedCard {
                            sel.sessions.forEachIndexed { i, s ->
                                ListRow(
                                    title = uiState.students[s.studentId]?.name ?: "未知",
                                    subtitle = buildString {
                                        append("${s.startTime}–${s.endTime}")
                                        if (s.location.isNotBlank()) append(" · ${s.location}")
                                    },
                                    trailing = {
                                        when {
                                            s.amount > 0 -> Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("¥${String.format("%.0f", s.amount)}",
                                                    fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurface)
                                                Spacer(Modifier.width(6.dp))
                                                PaymentStatusBadge(s.paymentStatus)
                                            }
                                            s.coursePackageId > 0 -> TrailingPill("扣课时", Green600, Green50)
                                            s.settlementId > 0 -> TrailingPill("已结算",
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                            else -> Unit
                                        }
                                    }
                                )
                                if (i < sel.sessions.lastIndex) RowDivider(startInset = AppleInset.Full)
                            }
                        }
                    }
                } else if (sel != null) {
                    item {
                        SectionHeader(DateUtils.formatDateFull(sel.date))
                        GroupedCard {
                            Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                Text("当天没有上课记录", fontSize = 15.sp, color = AppleLabelSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: CalendarDay,
    isToday: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val hasSessions = day.sessionCount > 0

    val bgColor = when {
        isSelected -> primary
        isToday -> primary.copy(alpha = 0.1f)
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
                        .background(if (isSelected) Color.White else primary)
                )
            }
        }
    }
}
