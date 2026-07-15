package com.teacher.journal.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 应用导航路由
 */
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object StudentList : Screen("student_list")
    data object RecordList : Screen("record_list")
    data object StudentDetail : Screen("student_detail/{studentId}") {
        fun createRoute(studentId: Long) = "student_detail/$studentId"
    }
    data object StudentEdit : Screen("student_edit/{studentId}") {
        fun createRoute(studentId: Long = -1) = "student_edit/$studentId"
    }
    data object SessionRecord : Screen("session_record/{studentId}") {
        fun createRoute(studentId: Long = -1) = "session_record/$studentId"
    }
    data object PackagePurchase : Screen("package_purchase/{studentId}") {
        fun createRoute(studentId: Long) = "package_purchase/$studentId"
    }
}

/**
 * 底部导航栏项目
 */
data class BottomNavItem(
    val label: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(
        label = "首页",
        route = Screen.Home.route,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    BottomNavItem(
        label = "学生",
        route = Screen.StudentList.route,
        selectedIcon = Icons.Filled.People,
        unselectedIcon = Icons.Outlined.People
    ),
    BottomNavItem(
        label = "记录",
        route = Screen.RecordList.route,
        selectedIcon = Icons.Filled.ListAlt,
        unselectedIcon = Icons.Outlined.ListAlt
    )
)
