package com.example.workbench

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.workbench.data.WorkBenchPreferencesRepository
import com.example.workbench.ui.navigation.Screen
import com.example.workbench.ui.navigation.WorkBenchBottomBar
import com.example.workbench.ui.navigation.WorkBenchNavGraph
import com.example.workbench.ui.theme.WorkBenchTheme
import com.example.workbench.viewmodel.WorkBenchViewModel
import com.example.workbench.viewmodel.WorkBenchViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = WorkBenchPreferencesRepository(applicationContext)
        val factory = WorkBenchViewModelFactory(repository)

        setContent {
            val viewModel: WorkBenchViewModel = viewModel(factory = factory)
            val settings by viewModel.settings.collectAsState()

            WorkBenchTheme(
                themeMode = settings.themeMode,
                dynamicColor = settings.dynamicColor
            ) {
                WorkBenchApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun WorkBenchApp(
    viewModel: WorkBenchViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            WorkBenchBottomBar(
                currentRoute = currentRoute,
                onNavigateToRoute = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.Dimensions.route) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { innerPadding ->
        WorkBenchNavGraph(
            navController = navController,
            viewModel = viewModel,
            modifier = Modifier.padding(innerPadding)
        )
    }
}