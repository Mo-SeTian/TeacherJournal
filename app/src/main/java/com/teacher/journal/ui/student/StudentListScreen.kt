package com.teacher.journal.ui.student

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teacher.journal.data.entity.PaymentType
import com.teacher.journal.data.entity.Student
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
        topBar = {
            TopAppBar(windowInsets = WindowInsets(0,0,0,0),
                title = { Text("学生", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = MaterialTheme.colorScheme.onPrimary),
                actions = {
                    IconButton(onClick = onNavigateToAdd) {
                        Icon(Icons.Filled.PersonAdd, contentDescription = "添加学生", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.searchStudents(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("搜索姓名或电话") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = Gray400) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.searchStudents("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "清除", tint = Gray400)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Blue500,
                    unfocusedBorderColor = Gray200,
                    focusedContainerColor = SurfaceWhite,
                    unfocusedContainerColor = SurfaceWhite
                )
            )
            Spacer(Modifier.height(12.dp))

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }
            } else if (uiState.students.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.People, contentDescription = null, modifier = Modifier.size(56.dp), tint = Gray300)
                        Spacer(Modifier.height(12.dp))
                        Text("还没有添加学生", style = MaterialTheme.typography.bodyLarge, color = Gray600)
                        Text("点击右上角 + 添加第一个学生", style = MaterialTheme.typography.bodySmall, color = Gray400)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(uiState.students) { student -> StudentCard(student, { onNavigateToDetail(student.id) }) }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun StudentCard(student: Student, onClick: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            // 头像
            Surface(Modifier.size(46.dp), shape = RoundedCornerShape(14.dp), color = Blue50) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = Blue500, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(student.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Gray900)
                    if (student.subject.isNotBlank()) {
                        Spacer(Modifier.width(8.dp))
                        Surface(shape = RoundedCornerShape(6.dp), color = Blue50) {
                            Text(student.subject, Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = Blue600)
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (student.phone.isNotBlank()) {
                        Icon(Icons.Outlined.Call, contentDescription = null, tint = Gray400, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(3.dp))
                        Text(student.phone, style = MaterialTheme.typography.bodySmall, color = Gray500)
                    }
                    if (student.location.isNotBlank()) {
                        if (student.phone.isNotBlank()) { Spacer(Modifier.width(12.dp)) }
                        Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = Gray400, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(3.dp))
                        Text(student.location, style = MaterialTheme.typography.bodySmall, color = Gray500)
                    }
                }
            }
            PaymentTypeBadge(student.paymentType)
        }
    }
}

@Composable
fun PaymentTypeBadge(paymentType: PaymentType) {
    val (label, bg, fg) = when (paymentType) {
        PaymentType.PREPAID -> Triple("课时包", Green50, Green600)
        PaymentType.PER_SESSION -> Triple("按次付", Amber50, Amber600)
        PaymentType.MONTHLY -> Triple("月结算", Blue50, Blue600)
    }
    Surface(shape = RoundedCornerShape(8.dp), color = bg) {
        Text(label, Modifier.padding(horizontal = 10.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = fg)
    }
}
