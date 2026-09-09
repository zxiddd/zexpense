package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.AddExpenseScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.CameraScannerScreen
import com.example.ui.screens.CategoriesScreen
import com.example.ui.screens.ExpenseDetailScreen
import com.example.ui.screens.ExpensesListScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MoreScreen
import com.example.ui.screens.PdfPreviewScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.theme.GlassBorderLight
import com.example.ui.theme.Slate400
import com.example.ui.theme.TealLight
import com.example.ui.theme.TealPrimary
import com.example.ui.viewmodel.ExpenseViewModel

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Home : Screen("home", "Home", Icons.Filled.GridView, Icons.Outlined.GridView)
    data object Expenses : Screen("expenses", "Expenses", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong)
    data object Reports : Screen("reports", "Reports", Icons.Filled.Description, Icons.Outlined.Description)
    data object More : Screen("more", "More", Icons.Filled.MoreHoriz, Icons.Outlined.MoreHoriz)
}

val bottomNavMainItems = listOf(
    Screen.Home,
    Screen.Expenses,
    Screen.Reports,
    Screen.More
)

@Composable
fun ZExpenseApp(
    viewModel: ExpenseViewModel,
    navController: NavHostController = rememberNavController()
) {
    val authState by viewModel.authState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // React to sign out: redirect to auth if logged out and not already on auth screen
    LaunchedEffect(authState.isLoggedIn) {
        if (!authState.isLoggedIn && currentRoute != null && currentRoute != "auth") {
            navController.navigate("auth") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    val shouldShowBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Expenses.route,
        Screen.Reports.route,
        Screen.More.route,
        "calendar"
    )

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                NavigationBar(
                    containerColor = Color.White.copy(alpha = 0.95f),
                    tonalElevation = 6.dp,
                    modifier = Modifier
                        .border(1.dp, GlassBorderLight)
                        .testTag("bottom_nav_bar")
                ) {
                    // Item 1: Home
                    NavItem(
                        screen = Screen.Home,
                        selected = currentRoute == Screen.Home.route,
                        onClick = {
                            if (currentRoute != Screen.Home.route) {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )

                    // Item 2: Expenses
                    NavItem(
                        screen = Screen.Expenses,
                        selected = currentRoute == Screen.Expenses.route,
                        onClick = {
                            if (currentRoute != Screen.Expenses.route) {
                                navController.navigate(Screen.Expenses.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )

                    // CENTER ITEM: Quick Camera / Scan Receipt Button
                    NavigationBarItem(
                        selected = currentRoute == "camera_scanner",
                        onClick = {
                            navController.navigate("camera_scanner")
                        },
                        icon = {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .shadow(4.dp, CircleShape)
                                    .clip(CircleShape)
                                    .background(TealPrimary)
                                    .testTag("bottom_bar_camera_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Scan Receipt",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        },
                        label = {
                            Text(
                                text = "Scan",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = TealPrimary,
                            unselectedIconColor = Color.White,
                            unselectedTextColor = TealPrimary,
                            indicatorColor = Color.Transparent
                        )
                    )

                    // Item 3: Reports
                    NavItem(
                        screen = Screen.Reports,
                        selected = currentRoute == Screen.Reports.route,
                        onClick = {
                            if (currentRoute != Screen.Reports.route) {
                                navController.navigate(Screen.Reports.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )

                    // Item 4: More / Settings
                    NavItem(
                        screen = Screen.More,
                        selected = currentRoute == Screen.More.route,
                        onClick = {
                            if (currentRoute != Screen.More.route) {
                                navController.navigate(Screen.More.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (authState.isLoggedIn) Screen.Home.route else "auth",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable("auth") {
                AuthScreen(
                    viewModel = viewModel,
                    onAuthSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo("auth") { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToAddExpense = { navController.navigate("add_expense") },
                    onNavigateToExpenses = { navController.navigate(Screen.Expenses.route) },
                    onNavigateToExpenseDetail = { id -> navController.navigate("expense_detail/$id") },
                    onNavigateToCategories = { navController.navigate("categories") },
                    onNavigateToProfile = { navController.navigate(Screen.More.route) }
                )
            }

            composable(Screen.Expenses.route) {
                ExpensesListScreen(
                    viewModel = viewModel,
                    onNavigateToAddExpense = { navController.navigate("add_expense") },
                    onNavigateToExpenseDetail = { id -> navController.navigate("expense_detail/$id") }
                )
            }

            composable("calendar") {
                CalendarScreen(
                    viewModel = viewModel,
                    onNavigateToExpenseDetail = { id -> navController.navigate("expense_detail/$id") }
                )
            }

            composable(Screen.Reports.route) {
                ReportsScreen(
                    viewModel = viewModel,
                    onNavigateToPdfPreview = { navController.navigate("pdf_preview") }
                )
            }

            composable(Screen.More.route) {
                MoreScreen(
                    viewModel = viewModel,
                    onNavigateToCategories = { navController.navigate("categories") }
                )
            }

            composable("add_expense") {
                AddExpenseScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToCamera = { navController.navigate("camera_scanner") }
                )
            }

            composable("camera_scanner") {
                CameraScannerScreen(
                    viewModel = viewModel,
                    onBillCaptured = {
                        navController.popBackStack()
                    },
                    onClose = { navController.popBackStack() }
                )
            }

            composable(
                route = "expense_detail/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("id") ?: 0L
                ExpenseDetailScreen(
                    expenseId = id,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("pdf_preview") {
                PdfPreviewScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("categories") {
                CategoriesScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun RowScope.NavItem(
    screen: Screen,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                contentDescription = screen.title,
                modifier = Modifier.size(24.dp)
            )
        },
        label = {
            Text(
                text = screen.title,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = TealPrimary,
            selectedTextColor = TealPrimary,
            unselectedIconColor = Slate400,
            unselectedTextColor = Slate400,
            indicatorColor = TealLight
        ),
        modifier = Modifier.testTag("nav_item_${screen.route}")
    )
}
