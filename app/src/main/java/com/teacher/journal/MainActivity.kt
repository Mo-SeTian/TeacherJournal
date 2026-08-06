package com.teacher.journal

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teacher.journal.data.ThemePreferences
import com.teacher.journal.ui.navigation.AppNavigation
import com.teacher.journal.ui.theme.TeacherJournalTheme
import com.teacher.journal.ui.theme.getThemePreset
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themePreferences: ThemePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // 导航栏彻底透明，消除白色底条
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        setContent {
            val themeId by themePreferences.themePreset.collectAsStateWithLifecycle(initialValue = "periwinkle")
            TeacherJournalTheme(themePreset = getThemePreset(themeId)) {
                AppNavigation()
            }
        }
    }
}
