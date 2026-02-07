package com.zaheer.lunawell.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.zaheer.lunawell.presentation.breasthealth.BreastHealthDashboardScreen
import com.zaheer.lunawell.presentation.breasthealth.BreastHealthViewModel
import com.zaheer.lunawell.presentation.breasthealth.BreastHealthViewModelFactory
import com.zaheer.lunawell.presentation.breasthealth.BreastLogScreen
import com.zaheer.lunawell.presentation.export.ExportBackupScreen
import com.zaheer.lunawell.presentation.export.ExportViewModel
import com.zaheer.lunawell.presentation.export.ExportViewModelFactory
import com.zaheer.lunawell.presentation.pregnancy.ContractionTimerScreen
import com.zaheer.lunawell.presentation.pregnancy.KickCounterScreen
import com.zaheer.lunawell.presentation.pregnancy.PregnancyDashboardScreen
import com.zaheer.lunawell.presentation.pregnancy.PregnancyLogsScreen
import com.zaheer.lunawell.presentation.reminders.RemindersScreen
import com.zaheer.lunawell.presentation.reminders.RemindersViewModel
import com.zaheer.lunawell.presentation.reminders.RemindersViewModelFactory
import com.zaheer.lunawell.presentation.paywall.PaywallScreen
import com.zaheer.lunawell.presentation.paywall.PaywallViewModel
import com.zaheer.lunawell.presentation.paywall.PaywallViewModelFactory
import com.zaheer.lunawell.presentation.settings.SettingsScreen
import com.zaheer.lunawell.presentation.settings.SettingsViewModel
import com.zaheer.lunawell.presentation.settings.SettingsViewModelFactory
import com.zaheer.lunawell.presentation.settings.PrivacyScreen
import com.zaheer.lunawell.presentation.settings.DeleteDataScreen
import com.zaheer.lunawell.presentation.applock.AppLockScreen
import com.zaheer.lunawell.presentation.applock.AppLockViewModel
import com.zaheer.lunawell.presentation.applock.AppLockViewModelFactory

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
            val context = LocalContext.current
            val viewModel: BreastHealthViewModel = viewModel(
                factory = BreastHealthViewModelFactory(context.applicationContext as android.app.Application)
            )
            BreastHealthDashboardScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(route = Route.BreastLog.route) {
            val context = LocalContext.current
            val viewModel: BreastHealthViewModel = viewModel(
                factory = BreastHealthViewModelFactory(context.applicationContext as android.app.Application)
            )
            BreastLogScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(route = Route.Reminders.route) {
            val context = LocalContext.current
            val viewModel: RemindersViewModel = viewModel(
                factory = RemindersViewModelFactory(context.applicationContext as android.app.Application)
            )
            RemindersScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(route = Route.ExportBackup.route) {
            val context = LocalContext.current
            val viewModel: ExportViewModel = viewModel(
                factory = ExportViewModelFactory(context.applicationContext as android.app.Application)
            )
            ExportBackupScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(route = Route.Paywall.route) {
            val context = LocalContext.current
            val viewModel: PaywallViewModel = viewModel(
                factory = PaywallViewModelFactory(context.applicationContext as android.app.Application)
            )
            PaywallScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(route = Route.Settings.route) {
            val context = LocalContext.current
            val viewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(context.applicationContext as android.app.Application)
            )
            SettingsScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(route = Route.Privacy.route) {
            PrivacyScreen(navController = navController)
        }

        composable(route = Route.DeleteData.route) {
            val context = LocalContext.current
            val viewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(context.applicationContext as android.app.Application)
            )
            DeleteDataScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(route = Route.AppLock.route) {
            val context = LocalContext.current
            val viewModel: AppLockViewModel = viewModel(
                factory = AppLockViewModelFactory(context.applicationContext as android.app.Application)
            )
            AppLockScreen(
                navController = navController,
                viewModel = viewModel
            )
        }
    }
}
