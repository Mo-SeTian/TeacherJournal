package com.teacher.journal.ui.student

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    var searchQuery by remember { mutableStateOf("") }

    val filteredStudents = remember(uiState.students, searchQuery) {
        if (searchQuery.isBlank()) uiState.students
        else uiState.students.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.subject.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = androidx.compose.ui.graphics.Color.White,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp
                )
            ) {
                Icon(Icons.Filled.Add, contentDescription = "添加学生")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp)
        ) {
            item {
                AppScreenTitle(
                    title = "学生",
                    subtitle = "共 ${uiState.students.size} 位学生"
                )
                Spacer(Modifier.height(12.dp))
                AppSearchField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = "搜索姓名或科目"
                )
                Spacer(Modifier.height(16.dp))
            }

            if (uiState.isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            } else if (filteredStudents.isEmpty()) {
                item {
                    AppCard {
                        AppEmptyState(
                            icon = Icons.Outlined.People,
                            title = if (searchQuery.isBlank()) "还没有添加学生" else "没有找到匹配的学生",
                            subtitle = if (searchQuery.isBlank()) "点击右下角 + 添加第一位学生" else null
                        )
                    }
                }
            } else {
                items(filteredStudents, key = { it.id }) { student ->
                    StudentCard(
                        student = student,
                        onClick = { onNavigateToDetail(student.id) }
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun StudentCard(
    student: Student,
    onClick: () -> Unit
) {
    AppCard {
        AppRow(
            title = student.name,
            subtitle = buildString {
                if (student.subject.isNotBlank()) append(student.subject)
                if (student.location.isNotBlank()) {
                    if (isNotEmpty()) append(" · ")
                    append(student.location)
                }
            },
            leading = {
                AppAvatar(name = student.name, size = 48.dp)
            },
            trailing = {
                AppTypeBadge(student.paymentType)
            },
            showChevron = true,
            onClick = onClick
        )
    }
}