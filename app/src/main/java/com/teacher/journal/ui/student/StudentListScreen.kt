package com.teacher.journal.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teacher.journal.data.entity.PaymentType
import com.teacher.journal.data.entity.Student
import com.teacher.journal.ui.components.*
import com.teacher.journal.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentListScreen(
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToAdd: () -> Unit,
    viewModel: StudentViewModel = hiltViewModel()
) {
    val uiState by viewModel.listUiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = {},
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                actions = {
                    IconButton(onClick = onNavigateToAdd) {
                        Icon(Icons.Filled.PersonAdd, "添加学生", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp)
        ) {
            item {
                LargeTitle(title = "学生", subtitle = "共 ${uiState.students.size} 位")
                Spacer(Modifier.height(8.dp))
            }

            item {
                AppleSearchField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.searchStudents(it) },
                    placeholder = "搜索姓名或电话"
                )
                Spacer(Modifier.height(16.dp))
            }

            if (uiState.isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().padding(top = 80.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            } else if (uiState.students.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(top = 80.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("暂无学生", fontSize = 17.sp, color = AppleLabelSecondary,
                                fontWeight = FontWeight.Medium)
                            Text("点击右上角 + 添加第一位学生",
                                fontSize = 13.sp, color = AppleLabelTertiary,
                                modifier = Modifier.padding(top = 6.dp))
                        }
                    }
                }
            } else {
                item {
                    GroupedCard {
                        uiState.students.forEachIndexed { i, student ->
                            StudentRow(student) { onNavigateToDetail(student.id) }
                            if (i < uiState.students.lastIndex) RowDivider(startInset = 66.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentRow(student: Student, onClick: () -> Unit) {
    ListRow(
        title = student.name,
        subtitle = buildString {
            if (student.subject.isNotBlank()) append(student.subject)
            if (student.location.isNotBlank()) {
                if (isNotEmpty()) append(" · ")
                append(student.location)
            }
            if (isEmpty() && student.phone.isNotBlank()) append(student.phone)
        }.ifEmpty { null },
        leading = { Avatar(student.name) },
        trailing = { PaymentTypeBadge(student.paymentType) },
        showChevron = true,
        onClick = onClick
    )
}

@Composable
fun Avatar(name: String) {
    val initial = name.take(1)
    val bg = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    Box(
        Modifier.size(40.dp).clip(CircleShape).background(bg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            initial,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun PaymentTypeBadge(paymentType: PaymentType) {
    val (label, bg, fg) = when (paymentType) {
        PaymentType.PREPAID -> Triple("课时包", Green50, Green600)
        PaymentType.PER_SESSION -> Triple("按次付", Amber50, Amber600)
        PaymentType.MONTHLY -> Triple("月结算", Blue50, Blue600)
    }
    TrailingPill(label, fg, bg)
}

/** iOS-style search field: filled gray pill, no border. */
@Composable
fun AppleSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(AppleFill)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.Search, contentDescription = null,
            tint = AppleLabelSecondary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(placeholder, fontSize = 15.sp, color = AppleLabelSecondary)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (value.isNotEmpty()) {
            IconButton(onClick = { onValueChange("") }, modifier = Modifier.size(22.dp)) {
                Icon(Icons.Filled.Clear, contentDescription = "清除",
                    tint = AppleLabelSecondary, modifier = Modifier.size(16.dp))
            }
        }
    }
}
