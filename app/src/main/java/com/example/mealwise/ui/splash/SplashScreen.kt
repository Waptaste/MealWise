package com.example.mealwise.ui.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mealwise.ui.auth.AuthNavigationEvent
import com.example.mealwise.ui.auth.AuthViewModel

@Composable
fun SplashScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.navigationEvents.collect { event ->
            when (event) {
                is AuthNavigationEvent.NavigateToLogin -> onNavigateToLogin()
                is AuthNavigationEvent.NavigateToHome -> onNavigateToHome()
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "MealWise",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }
    }
}
