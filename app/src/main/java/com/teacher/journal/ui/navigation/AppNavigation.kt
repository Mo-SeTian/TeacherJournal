package com.teacher.journal.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.teacher.journal.ui.components.AppTopBar
import com.teacher.journal.ui.home.HomeScreen
import com.teacher.journal.ui.coursepackage.PackagePurchaseScreen
import com.teacher.journal.ui.session.SessionListScreen
import com.teacher.journal.ui.session.SessionRecordScreen
import com.teacher.journal.ui.settlement.MonthlySettlementScreen
import com.teacher.journal.ui.settings.SettingsScreen
import com.teacher.journal.ui.student.StudentDetailScreen
import com.teacher.journal.ui.student.StudentEditScreen
import com.teacher.journal.ui.student.StudentListScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomNavRoutes = listOf(
        Screen.Home.route,
        Screen.StudentList.route,
        Screen.RecordList.route,
        Screen.Settings.route
    )
    val showBottomBar = currentDestination?.route in bottomNavRoutes

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                FloatingBottomBar(
                    currentRoute = currentDestination?.route,
                    onItemClick = { item ->
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToStudentDetail = { id ->
                        navController.navigate(Screen.StudentDetail.createRoute(id))
                    },
                    onNavigateToSessionRecord = {
                        navController.navigate(Screen.SessionRecord.createRoute())
                    }
                )
            }

            composable(Screen.StudentList.route) {
                StudentListScreen(
                    onNavigateToDetail = { id ->
                        navController.navigate(Screen.StudentDetail.createRoute(id))
                    },
                    onNavigateToAdd = {
                        navController.navigate(Screen.StudentEdit.createRoute())
                    }
                )
            }

            composable(Screen.RecordList.route) {
                SessionListScreen(
                    onEditRecord = { studentId, recordId ->
                        navController.navigate(Screen.SessionRecord.createRoute(studentId, recordId))
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen()
            }

            composable(
                Screen.StudentDetail.route,
                arguments = listOf(navArgument("studentId") { type = NavType.LongType })
            ) { backStackEntry ->
                val studentId = backStackEntry.arguments?.getLong("studentId") ?: 0L
                StudentDetailScreen(
                    studentId = studentId,
                    onNavigateToEdit = {
                        navController.navigate(Screen.StudentEdit.createRoute(studentId))
                    },
                    onNavigateToSessionRecord = {
                        navController.navigate(Screen.SessionRecord.createRoute(studentId))
                    },
                    onNavigateToPackagePurchase = {
                        navController.navigate(Screen.PackagePurchase.createRoute(studentId))
                    },
                    onNavigateToMonthlySettlement = {
                        navController.navigate(Screen.MonthlySettlement.createRoute(studentId))
                    },
                    onEditRecord = { recordId ->
                        navController.navigate(Screen.SessionRecord.createRoute(studentId, recordId))
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                Screen.StudentEdit.route,
                arguments = listOf(navArgument("studentId") { type = NavType.LongType })
            ) { backStackEntry ->
                val studentId = backStackEntry.arguments?.getLong("studentId")?.takeIf { it > 0 }
                StudentEditScreen(
                    studentId = studentId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                Screen.SessionRecord.route,
                arguments = listOf(
                    navArgument("studentId") { type = NavType.LongType },
                    navArgument("recordId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val studentId = backStackEntry.arguments?.getLong("studentId")?.takeIf { it > 0 }
                val recordId = backStackEntry.arguments?.getLong("recordId") ?: 0L
                SessionRecordScreen(
                    preselectedStudentId = studentId,
                    recordId = recordId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                Screen.PackagePurchase.route,
                arguments = listOf(navArgument("studentId") { type = NavType.LongType })
            ) { backStackEntry ->
                val studentId = backStackEntry.arguments?.getLong("studentId") ?: 0L
                PackagePurchaseScreen(
                    studentId = studentId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                Screen.MonthlySettlement.route,
                arguments = listOf(navArgument("studentId") { type = NavType.LongType })
            ) { backStackEntry ->
                val studentId = backStackEntry.arguments?.getLong("studentId") ?: 0L
                MonthlySettlementScreen(
                    studentId = studentId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun FloatingBottomBar(
    currentRoute: String?,
    onItemClick: (BottomNavItem) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomNavItems.forEach { item ->
                val selected = currentRoute == item.route
                val color by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                    animationSpec = tween(300),
                    label = "navColor"
                )
                val bgColor by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent,
                    animationSpec = tween(300),
                    label = "navBg"
                )

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(bgColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onItemClick(item) }
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                        tint = color,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        item.label,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        color = color
                    )
                }
            }
        }
    }
}