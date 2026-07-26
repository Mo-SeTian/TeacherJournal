package com.teacher.journal.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
import com.teacher.journal.ui.home.HomeScreen
import com.teacher.journal.ui.coursepackage.PackagePurchaseScreen
import com.teacher.journal.ui.session.SessionListScreen
import com.teacher.journal.ui.session.SessionRecordScreen
import com.teacher.journal.ui.settlement.MonthlySettlementScreen
import com.teacher.journal.ui.student.StudentDetailScreen
import com.teacher.journal.ui.student.StudentEditScreen
import com.teacher.journal.ui.student.StudentListScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // 判断是否显示底部导航栏
    val bottomNavRoutes = listOf(
        Screen.Home.route,
        Screen.StudentList.route,
        Screen.RecordList.route
    )
    val showBottomBar = currentDestination?.route in bottomNavRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 0.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.route == item.route
                        } == true

                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = {
                                Text(item.label, fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
                            },
                            selected = selected,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = Color(0xFF8E8E93),
                                unselectedTextColor = Color(0xFF8E8E93),
                                indicatorColor = Color.Transparent
                            ),
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToStudentDetail = { studentId ->
                        navController.navigate(Screen.StudentDetail.createRoute(studentId))
                    },
                    onNavigateToSessionRecord = {
                        navController.navigate(Screen.SessionRecord.createRoute())
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }

            composable(Screen.StudentList.route) {
                StudentListScreen(
                    onNavigateToDetail = { studentId ->
                        navController.navigate(Screen.StudentDetail.createRoute(studentId))
                    },
                    onNavigateToAdd = {
                        navController.navigate(Screen.StudentEdit.createRoute())
                    }
                )
            }

            composable(Screen.RecordList.route) {
                SessionListScreen()
            }

            composable(
                route = Screen.StudentDetail.route,
                arguments = listOf(navArgument("studentId") { type = NavType.LongType })
            ) { backStackEntry ->
                val studentId = backStackEntry.arguments?.getLong("studentId") ?: return@composable
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
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.StudentEdit.route,
                arguments = listOf(navArgument("studentId") { type = NavType.LongType })
            ) { backStackEntry ->
                val studentId = backStackEntry.arguments?.getLong("studentId") ?: -1L
                StudentEditScreen(
                    studentId = if (studentId == -1L) null else studentId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.SessionRecord.route,
                arguments = listOf(navArgument("studentId") { type = NavType.LongType })
            ) { backStackEntry ->
                val studentId = backStackEntry.arguments?.getLong("studentId") ?: -1L
                SessionRecordScreen(
                    preselectedStudentId = if (studentId == -1L) null else studentId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.PackagePurchase.route,
                arguments = listOf(navArgument("studentId") { type = NavType.LongType })
            ) { backStackEntry ->
                val studentId = backStackEntry.arguments?.getLong("studentId") ?: return@composable
                PackagePurchaseScreen(
                    studentId = studentId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.MonthlySettlement.route,
                arguments = listOf(navArgument("studentId") { type = NavType.LongType })
            ) { backStackEntry ->
                val studentId = backStackEntry.arguments?.getLong("studentId") ?: return@composable
                MonthlySettlementScreen(
                    studentId = studentId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Settings.route) {
                com.teacher.journal.ui.settings.SettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
