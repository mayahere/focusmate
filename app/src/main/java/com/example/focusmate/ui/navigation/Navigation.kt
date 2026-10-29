package com.example.focusmate.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.focusmate.ui.home.HomeScreen
import com.example.focusmate.ui.theme.*
import com.example.focusmate.ui.timer.FocusScreen
import com.example.focusmate.ui.timer.SetUpPomodoroScreen
import com.example.focusmate.viewmodel.TaskViewModel
import com.example.focusmate.viewmodel.TimerViewModel
import kotlinx.serialization.Serializable

@Serializable object HomeRoute
@Serializable object FocusRoute
@Serializable object SetUpPomodoroRoute

data class BottomNavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: Any
)

val bottomNavItems = listOf(
    BottomNavItem("Trang chủ", Icons.Filled.Home, Icons.Outlined.Home, HomeRoute),
    BottomNavItem("Tập trung", Icons.Filled.Timer, Icons.Outlined.Timer, FocusRoute)
)

@Composable
fun FocusMateApp(
    taskViewModel: TaskViewModel,
    timerViewModel: TimerViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Hide bottom bar on settings screen
    val showBottomBar = currentDestination?.hasRoute(SetUpPomodoroRoute::class) != true

    Scaffold(
        containerColor = CreamBackground,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = CreamSurface,
                    tonalElevation = 0.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hasRoute(item.route::class) == true
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = {
                                Text(
                                    item.label,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(HomeRoute) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = TextPrimary,
                                selectedTextColor = TextPrimary,
                                unselectedIconColor = TextTertiary,
                                unselectedTextColor = TextTertiary,
                                indicatorColor = CreamBackground
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeRoute,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<HomeRoute> {
                HomeScreen(
                    taskViewModel = taskViewModel,
                    onStartFocus = { task ->
                        timerViewModel.selectTask(task)
                        navController.navigate(FocusRoute) {
                            popUpTo(HomeRoute) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable<FocusRoute> {
                FocusScreen(
                    timerViewModel = timerViewModel,
                    taskViewModel = taskViewModel,
                    onNavigateBack = {
                        navController.navigate(HomeRoute) {
                            popUpTo(HomeRoute) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onOpenSettings = {
                        navController.navigate(SetUpPomodoroRoute)
                    }
                )
            }
            composable<SetUpPomodoroRoute> {
                SetUpPomodoroScreen(
                    timerViewModel = timerViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}