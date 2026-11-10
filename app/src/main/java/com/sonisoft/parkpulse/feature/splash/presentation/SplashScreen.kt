package com.sonisoft.parkpulse.feature.splash.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.sonisoft.parkpulse.R
import com.sonisoft.parkpulse.core.designsystem.theme.ForestGreen
import com.sonisoft.parkpulse.core.designsystem.theme.ForestGreenDark
import com.sonisoft.parkpulse.core.designsystem.theme.OffWhite
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    // State for animations
    var startLottie by remember { mutableStateOf(false) }
    var showAppName by remember { mutableStateOf(false) }
    var showTagline by remember { mutableStateOf(false) }

    // Load Lottie composition
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.park))

    // Animate Lottie progress
    val lottieProgress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = startLottie,
        iterations = 1,
        speed = 1.0f
    )

    // Animation sequence
    LaunchedEffect(key1 = true) {
        // 1. Splash screen appears with a subtle fade-in
        delay(400.milliseconds) // Initial fade-in buffer
        startLottie = true

        // 2. Lottie animation starts playing. Wait a bit before showing text.
        delay(1500)

        // 3. App name appears
        showAppName = true

        // 4. Tagline appears shortly after
        delay(600)
        showTagline = true

        // Wait for the sequence to complete before navigating
        delay(1500)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        ForestGreenDark,
                        ForestGreen
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Lottie Animation
            Box(
                modifier = Modifier.size(400.dp),
                contentAlignment = Alignment.Center
            ) {
                if (composition != null) {
                    LottieAnimation(
                        composition = composition,
                        progress = { lottieProgress },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Graceful fallback if Lottie fails to load
                    Icon(
                        imageVector = Icons.Rounded.Eco,
                        contentDescription = "Nature Icon",
                        tint = OffWhite,
                        modifier = Modifier.size(100.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Animated App Name
            AnimatedVisibility(
                visible = showAppName,
                enter = fadeIn(animationSpec = tween(600, easing = FastOutSlowInEasing)) +
                        slideInVertically(
                            initialOffsetY = { 50 },
                            animationSpec = tween(600, easing = FastOutSlowInEasing)
                        )
            ) {
                Text(
                    text = "ParkPulse",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    ),
                    color = OffWhite
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Animated Tagline
            AnimatedVisibility(
                visible = showTagline,
                enter = fadeIn(animationSpec = tween(400, easing = FastOutSlowInEasing)) +
                        slideInVertically(
                            initialOffsetY = { 20 },
                            animationSpec = tween(400, easing = FastOutSlowInEasing)
                        )
            ) {
                Text(
                    text = "Discover. Survey. Preserve.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp
                    ),
                    color = OffWhite.copy(alpha = 0.8f)
                )
            }
        }
    }
}
