package com.aven.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aven.app.androidintegration.appdetection.SimulatedAppLaunchDetector
import com.aven.app.data.repository.AvenRepository
import com.aven.app.ui.garden.GardenScreen
import com.aven.app.ui.garden.GardenViewModel
import com.aven.app.ui.home.HomeScreen
import com.aven.app.ui.home.HomeViewModel
import com.aven.app.ui.insights.InsightsScreen
import com.aven.app.ui.insights.InsightsViewModel
import com.aven.app.ui.intervention.InterventionScreen
import com.aven.app.ui.intervention.InterventionViewModel
import com.aven.app.ui.navigation.AvenDestination
import com.aven.app.ui.onboarding.OnboardingScreen
import com.aven.app.ui.onboarding.OnboardingViewModel
import com.aven.app.ui.settings.SettingsScreen
import com.aven.app.ui.settings.SettingsViewModel
import com.aven.app.ui.theme.AvenTheme

@Composable
fun AvenApp() {
    val context = LocalContext.current
    val repository = remember { AvenRepository.getInstance(context) }
    val navController = rememberNavController()

    val isOnboardingCompleted by repository.preferences.isOnboardingCompleted.collectAsState(initial = false)
    val themeMode by repository.preferences.themeMode.collectAsState(initial = "system")

    val isDark = when (themeMode) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }

    // Global launch listener from simulated or system detection
    LaunchedEffect(Unit) {
        SimulatedAppLaunchDetector.instance.launchRequests.collect { request ->
            navController.navigate(
                AvenDestination.Intervention.createRoute(request.packageName, request.appDisplayName)
            )
        }
    }

    AvenTheme(darkTheme = isDark) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val isBottomNavVisible = currentRoute in listOf(
            AvenDestination.Home.route,
            AvenDestination.Garden.route,
            AvenDestination.Insights.route,
            AvenDestination.Settings.route
        )

        val startDestination = if (isOnboardingCompleted) {
            AvenDestination.Home.route
        } else {
            AvenDestination.Onboarding.route
        }

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("aven_app_scaffold"),
            bottomBar = {
                if (isBottomNavVisible) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag("bottom_navigation_bar")
                    ) {
                        AvenDestination.bottomNavDestinations.forEach { screen ->
                            val selected = currentRoute == screen.route
                            NavigationBarItem(
                                icon = {
                                    screen.icon?.let { iconVector ->
                                        Icon(
                                            imageVector = iconVector,
                                            contentDescription = screen.title
                                        )
                                    }
                                },
                                label = { Text(screen.title) },
                                selected = selected,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.testTag(screen.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // 1. Onboarding
                composable(AvenDestination.Onboarding.route) {
                    val vm: OnboardingViewModel = viewModel(
                        factory = OnboardingViewModel.Factory(repository)
                    )
                    OnboardingScreen(
                        viewModel = vm,
                        onComplete = {
                            navController.navigate(AvenDestination.Home.route) {
                                popUpTo(AvenDestination.Onboarding.route) { inclusive = true }
                            }
                        }
                    )
                }

                // 2. Home
                composable(AvenDestination.Home.route) {
                    val vm: HomeViewModel = viewModel(
                        factory = HomeViewModel.Factory(repository)
                    )
                    HomeScreen(
                        viewModel = vm,
                        onNavigateToGarden = {
                            navController.navigate(AvenDestination.Garden.route)
                        },
                        onNavigateToSettings = {
                            navController.navigate(AvenDestination.Settings.route)
                        },
                        onSimulateLaunch = { pkg, name ->
                            navController.navigate(
                                AvenDestination.Intervention.createRoute(pkg, name)
                            )
                        }
                    )
                }

                // 3. Garden
                composable(AvenDestination.Garden.route) {
                    val vm: GardenViewModel = viewModel(
                        factory = GardenViewModel.Factory(repository)
                    )
                    GardenScreen(viewModel = vm)
                }

                // 4. Insights
                composable(AvenDestination.Insights.route) {
                    val vm: InsightsViewModel = viewModel(
                        factory = InsightsViewModel.Factory(repository)
                    )
                    InsightsScreen(viewModel = vm)
                }

                // 5. Settings
                composable(AvenDestination.Settings.route) {
                    val vm: SettingsViewModel = viewModel(
                        factory = SettingsViewModel.Factory(repository)
                    )
                    SettingsScreen(viewModel = vm)
                }

                // 6. Intervention (Pause / Intent Checkpoint)
                composable(
                    route = AvenDestination.Intervention.route,
                    arguments = listOf(
                        navArgument("packageName") { type = NavType.StringType },
                        navArgument("appName") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val pkg = backStackEntry.arguments?.getString("packageName") ?: "com.aven.app"
                    val appName = backStackEntry.arguments?.getString("appName") ?: "App"

                    val vm: InterventionViewModel = viewModel(
                        factory = InterventionViewModel.Factory(pkg, appName, repository)
                    )
                    InterventionScreen(
                        viewModel = vm,
                        onDismiss = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}
