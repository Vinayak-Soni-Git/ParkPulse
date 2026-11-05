package com.sonisoft.parkpulse.feature.dashboard.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sonisoft.parkpulse.MainViewModel
import com.sonisoft.parkpulse.core.designsystem.theme.ErrorRed
import com.sonisoft.parkpulse.core.designsystem.theme.OffWhite

@Composable
fun DashboardScreen(
    onLogoutSuccess: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
    mainViewModel: MainViewModel
) {
    val isLoggedIn by mainViewModel.isLoggedIn.collectAsState()

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn == false) {
            onLogoutSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Parks Dashboard",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = viewModel::onLogoutClick,
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
            ) {
                Text(text = "Logout", color = OffWhite)
            }
        }
    }
}
