package com.teacher.journal.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teacher.journal.ui.components.*
import com.teacher.journal.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = {},
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, "返回",
                            tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(top = 4.dp, bottom = 32.dp)
        ) {
            item {
                LargeTitle(title = "设置")
                Spacer(Modifier.height(8.dp))
            }

            item {
                SectionHeader("外观")
                GroupedCard {
                    uiState.themes.forEachIndexed { i, theme ->
                        val selected = theme.id == uiState.currentThemeId
                        ThemeRow(theme.name, theme.primary, theme.secondary, theme.tertiary, selected) {
                            viewModel.selectTheme(theme.id)
                        }
                        if (i < uiState.themes.lastIndex) RowDivider(startInset = AppleInset.ThreeSwatch)
                    }
                }
            }

            item {
                Spacer(Modifier.height(24.dp))
                SectionHeader("关于")
                GroupedCard {
                    ListRow(title = "版本", trailing = { TrailingValue("1.0.0") })
                    RowDivider(startInset = AppleInset.Full)
                    ListRow(title = "授业札记", subtitle = "com.teacher.journal")
                }
            }
        }
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
    ListRow(
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
            .border(0.5.dp, AppleSeparator, CircleShape)
    )
}
