package com.sonisoft.parkpulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sonisoft.parkpulse.core.designsystem.theme.ParkPulseTheme
import com.sonisoft.parkpulse.core.navigation.Screen
import com.sonisoft.parkpulse.feature.auth.presentation.login.LoginScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ParkPulseTheme {
                val navController = rememberNavController()
                val mainViewModel: MainViewModel = hiltViewModel()
                val isLoggedIn by mainViewModel.isLoggedIn.collectAsState()

                val snackbarHostState = remember { SnackbarHostState() }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Splash.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Splash.route) {
                            com.sonisoft.parkpulse.feature.splash.presentation.SplashScreen(
                                onSplashFinished = {
                                    val destination = if (isLoggedIn == true) Screen.Dashboard.route else Screen.Login.route
                                    navController.navigate(destination) {
                                        popUpTo(Screen.Splash.route) { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable(Screen.Login.route) {
                            LoginScreen(
                                snackbarHostState = snackbarHostState,
                                onLoginSuccess = {
                                    navController.navigate(Screen.Dashboard.route) {
                                        popUpTo(Screen.Login.route) { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable(Screen.Dashboard.route) {
                            com.sonisoft.parkpulse.feature.parks.presentation.ParksDashboardScreen(
                                onLogout = {
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                                    }
                                },
                                onParkClick = { parkId ->
                                    navController.navigate(Screen.Survey.createRoute(parkId))
                                },
                                onMapClick = {
                                    navController.navigate(Screen.Map.route)
                                },
                                onDebugCameraClick = {
                                    navController.navigate(Screen.DebugCameraTest.route)
                                }
                            )
                        }
                        composable(Screen.Map.route) {
                            com.sonisoft.parkpulse.feature.parks.presentation.map.MapScreen(
                                onBackClick = { navController.popBackStack() },
                                onStartSurvey = { parkId ->
                                    navController.navigate(Screen.Survey.createRoute(parkId))
                                }
                            )
                        }
                        composable(Screen.Survey.route) {
                            com.sonisoft.parkpulse.feature.parks.presentation.survey.SurveyScreen(
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                        composable(Screen.DebugCameraTest.route) {
                            com.sonisoft.parkpulse.feature.parks.presentation.camera.DebugCameraTestScreen(
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                        // Other screens will be added here
                    }
                }
            }
        }
    }
}
