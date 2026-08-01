package com.teacher.journal.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teacher.journal.data.BackupManager
import com.teacher.journal.data.ThemePreferences
import com.teacher.journal.ui.theme.ThemePreset
import com.teacher.journal.ui.theme.ThemePresets
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val currentThemeId: String = "periwinkle",
    val themes: List<ThemePreset> = ThemePresets,
    val isImporting: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themePreferences: ThemePreferences,
    private val backupManager: BackupManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            themePreferences.themePreset.collect { id ->
                _uiState.update { it.copy(currentThemeId = id) }
            }
        }
    }

    fun selectTheme(id: String) {
        viewModelScope.launch {
            themePreferences.setThemePreset(id)
        }
    }

    fun exportData(uri: Uri, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            val result = runCatching { backupManager.exportTo(uri) }
            onComplete(
                result.fold(
                    onSuccess = { "已导出备份（$it 位学生）" },
                    onFailure = { "导出失败：${it.message}" }
                )
            )
        }
    }

    fun importData(uri: Uri, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true) }
            val result = runCatching { backupManager.importFrom(uri) }
            _uiState.update { it.copy(isImporting = false) }
            onComplete(
                result.fold(
                    onSuccess = {
                        "导入完成：${it.students} 位学生 · ${it.records} 条记录 · ${it.earnings} 笔收入"
                    },
                    onFailure = { "导入失败：${it.message}" }
                )
            )
        }
    }
}
