package com.teacher.journal.ui.student

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teacher.journal.data.entity.Student
import com.teacher.journal.ui.components.*

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
            AppTopBar(
                title = "学生",
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
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp)
        ) {
            item {
                AppScreenTitle(title = "学生", subtitle = "共 ${uiState.students.size} 位")
                Spacer(Modifier.height(10.dp))
            }

            item {
                AppSearchField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.searchStudents(it) },
                    placeholder = "搜索姓名或电话"
                )
                Spacer(Modifier.height(16.dp))
            }

            if (uiState.isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().padding(top = 60.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            } else if (uiState.students.isEmpty()) {
                item {
                    AppCard {
                        AppEmptyState(
                            icon = Icons.Outlined.People,
                            title = "暂无学生",
                            subtitle = "点击右上角 + 添加第一位学生"
                        )
                    }
                }
            } else {
                item {
                    AppCard {
                        uiState.students.forEachIndexed { i, student ->
                            StudentRow(student) { onNavigateToDetail(student.id) }
                            if (i < uiState.students.lastIndex) AppDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentRow(student: Student, onClick: () -> Unit) {
    AppRow(
        title = student.name,
        subtitle = buildString {
            if (student.subject.isNotBlank()) append(student.subject)
            if (student.location.isNotBlank()) {
                if (isNotEmpty()) append(" · ")
                append(student.location)
            }
            if (isEmpty() && student.phone.isNotBlank()) append(student.phone)
        }.ifEmpty { null },
        leading = { AppAvatar(student.name) },
        trailing = { AppTypeBadge(student.paymentType) },
        showChevron = true,
        onClick = onClick
    )
}
