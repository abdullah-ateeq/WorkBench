package com.example.workbench.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.workbench.ui.screens.AboutScreen
import com.example.workbench.ui.screens.CalculatorScreen
import com.example.workbench.ui.screens.DimensionsScreen
import com.example.workbench.ui.screens.HistoryScreen
import com.example.workbench.ui.screens.SettingsScreen
import com.example.workbench.ui.screens.SplashScreen
import com.example.workbench.viewmodel.WorkBenchViewModel

@Composable
fun WorkBenchNavGraph(
    navController: NavHostController,
    viewModel: WorkBenchViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Screen.Dimensions.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dimensions.route) {
            DimensionsScreen(
                viewModel = viewModel,
                onNavigateToCalculator = {
                    navController.navigate(Screen.Calculator.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.Calculator.route) {
            CalculatorScreen(
                viewModel = viewModel
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                viewModel = viewModel,
                onNavigateToCalculator = {
                    navController.navigate(Screen.Calculator.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = viewModel
            )
        }

        composable(Screen.About.route) {
            AboutScreen()
        }
    }
}
