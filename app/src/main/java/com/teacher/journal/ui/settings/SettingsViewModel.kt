package com.teacher.journal.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teacher.journal.data.ThemePreferences
import com.teacher.journal.ui.theme.ThemePreset
import com.teacher.journal.ui.theme.ThemePresets
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val currentThemeId: String = "periwinkle",
    val themes: List<ThemePreset> = ThemePresets
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themePreferences: ThemePreferences
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
}
