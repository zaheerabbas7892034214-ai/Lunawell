package com.zaheer.lunawell.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.zaheer.lunawell.presentation.pregnancy.PregnancyDashboardScreen
import com.zaheer.lunawell.presentation.pregnancy.PregnancyLogsScreen
import com.zaheer.lunawell.presentation.pregnancy.KickCounterScreen
import com.zaheer.lunawell.presentation.pregnancy.ContractionTimerScreen

sealed class Route(val route: String) {
    object Splash : Route("splash")
    object Onboarding : Route("onboarding")
    object Home : Route("home")
    object Calendar : Route("calendar")
    object Log : Route("log/{selectedDate}") {
        fun createRoute(selectedDate: String) = "log/$selectedDate"
    }
    object Insights : Route("insights")
    object PregnancyDashboard : Route("pregnancy_dashboard")
    object PregnancyLogs : Route("pregnancy_logs")
    object KickCounter : Route("kick_counter")
    object ContractionTimer : Route("contraction_timer")
    object BreastHealthDashboard : Route("breast_health_dashboard")
    object BreastLog : Route("breast_log")
    object Reminders : Route("reminders")
    object ExportBackup : Route("export_backup")
    object Paywall : Route("paywall")
    object Settings : Route("settings")
    object Privacy : Route("privacy")
    object DeleteData : Route("delete_data")
    object AppLock : Route("app_lock")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Route.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(route = Route.Splash.route) {
            SplashScreen(navController = navController)
        }

        composable(route = Route.Onboarding.route) {
            OnboardingScreen(navController = navController)
        }

        composable(route = Route.Home.route) {
            HomeScreen(navController = navController)
        }

        composable(route = Route.Calendar.route) {
            CalendarScreen(navController = navController)
        }

        composable(
            route = Route.Log.route,
            arguments = listOf(
                navArgument("selectedDate") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val selectedDate = backStackEntry.arguments?.getString("selectedDate") ?: ""
            LogScreen(
                navController = navController,
                selectedDate = selectedDate
            )
        }

        composable(route = Route.Insights.route) {
            InsightsScreen(navController = navController)
        }

        composable(route = Route.PregnancyDashboard.route) {
            PregnancyDashboardScreen(navController = navController)
        }

        composable(route = Route.PregnancyLogs.route) {
            PregnancyLogsScreen(navController = navController)
        }

        composable(route = Route.KickCounter.route) {
            KickCounterScreen(navController = navController)
        }

        composable(route = Route.ContractionTimer.route) {
            ContractionTimerScreen(navController = navController)
        }

        composable(route = Route.BreastHealthDashboard.route) {
            BreastHealthDashboardScreen(navController = navController)
        }

        composable(route = Route.BreastLog.route) {
            BreastLogScreen(navController = navController)
        }

        composable(route = Route.Reminders.route) {
            RemindersScreen(navController = navController)
        }

        composable(route = Route.ExportBackup.route) {
            ExportBackupScreen(navController = navController)
        }

        composable(route = Route.Paywall.route) {
            PaywallScreen(navController = navController)
        }

        composable(route = Route.Settings.route) {
            SettingsScreen(navController = navController)
        }

        composable(route = Route.Privacy.route) {
            PrivacyScreen(navController = navController)
        }

        composable(route = Route.DeleteData.route) {
            DeleteDataScreen(navController = navController)
        }

        composable(route = Route.AppLock.route) {
            AppLockScreen(navController = navController)
        }
    }
}

// Placeholder screens - These should be replaced with actual screen implementations
@Composable
private fun SplashScreen(navController: NavHostController) {
    // TODO: Implement SplashScreen
}

@Composable
private fun OnboardingScreen(navController: NavHostController) {
    // TODO: Implement OnboardingScreen
}

@Composable
private fun HomeScreen(navController: NavHostController) {
    // TODO: Implement HomeScreen
}

@Composable
private fun CalendarScreen(navController: NavHostController) {
    // TODO: Implement CalendarScreen
}

@Composable
private fun LogScreen(navController: NavHostController, selectedDate: String) {
    // TODO: Implement LogScreen
}

@Composable
private fun InsightsScreen(navController: NavHostController) {
    // TODO: Implement InsightsScreen
}



@Composable
private fun BreastHealthDashboardScreen(navController: NavHostController) {
    // TODO: Implement BreastHealthDashboardScreen
}

@Composable
private fun BreastLogScreen(navController: NavHostController) {
    // TODO: Implement BreastLogScreen
}

@Composable
private fun RemindersScreen(navController: NavHostController) {
    // TODO: Implement RemindersScreen
}

@Composable
private fun ExportBackupScreen(navController: NavHostController) {
    // TODO: Implement ExportBackupScreen
}

@Composable
private fun PaywallScreen(navController: NavHostController) {
    // TODO: Implement PaywallScreen
}

@Composable
private fun SettingsScreen(navController: NavHostController) {
    // TODO: Implement SettingsScreen
}

@Composable
private fun PrivacyScreen(navController: NavHostController) {
    // TODO: Implement PrivacyScreen
}

@Composable
private fun DeleteDataScreen(navController: NavHostController) {
    // TODO: Implement DeleteDataScreen
}

@Composable
private fun AppLockScreen(navController: NavHostController) {
    // TODO: Implement AppLockScreen
}
