package com.example.mealwise.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mealwise.ui.components.LoadingButton
import com.example.mealwise.ui.components.MealWisePasswordField
import com.example.mealwise.ui.components.MealWiseTextField

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.navigationEvents.collect { event ->
            when (event) {
                is AuthNavigationEvent.NavigateToHome -> onNavigateToHome()
                else -> {}
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Join MealWise today",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(32.dp))

        MealWiseTextField(
            value = uiState.name,
            onValueChange = { viewModel.onNameChanged(it) },
            label = "Full Name",
            error = uiState.nameError,
            enabled = !uiState.isLoading,
            testTag = "name_field"
        )

        Spacer(modifier = Modifier.height(16.dp))

        MealWiseTextField(
            value = uiState.email,
            onValueChange = { viewModel.onEmailChanged(it) },
            label = "Email",
            error = uiState.emailError,
            enabled = !uiState.isLoading,
            testTag = "email_field"
        )

        Spacer(modifier = Modifier.height(16.dp))

        MealWisePasswordField(
            value = uiState.password,
            onValueChange = { viewModel.onPasswordChanged(it) },
            label = "Password",
            error = uiState.passwordError,
            isVisible = uiState.isPasswordVisible,
            onToggleVisibility = { viewModel.togglePasswordVisibility() },
            enabled = !uiState.isLoading,
            testTag = "password_field"
        )

        Spacer(modifier = Modifier.height(16.dp))

        MealWisePasswordField(
            value = uiState.confirmPassword,
            onValueChange = { viewModel.onConfirmPasswordChanged(it) },
            label = "Confirm Password",
            error = uiState.confirmPasswordError,
            isVisible = uiState.isConfirmPasswordVisible,
            onToggleVisibility = { viewModel.toggleConfirmPasswordVisibility() },
            enabled = !uiState.isLoading,
            testTag = "confirm_password_field"
        )

        Spacer(modifier = Modifier.height(24.dp))

        uiState.generalError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .testTag("general_error")
            )
        }

        LoadingButton(
            text = "Register",
            onClick = { viewModel.register() },
            isLoading = uiState.isLoading,
            enabled = !uiState.isLoading,
            testTag = "register_button"
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onNavigateToLogin,
            enabled = !uiState.isLoading,
            modifier = Modifier.testTag("navigate_to_login")
        ) {
            Text("Already have an account? Login")
        }
    }
}
