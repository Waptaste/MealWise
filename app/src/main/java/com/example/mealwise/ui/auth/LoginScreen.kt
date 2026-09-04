package com.example.mealwise.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalAutofill
import androidx.compose.ui.platform.LocalAutofillTree
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.autofill.AutofillNode
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mealwise.ui.components.LoadingButton
import com.example.mealwise.ui.components.MealWisePasswordField
import com.example.mealwise.ui.components.MealWiseTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val autofill = LocalAutofill.current
    val autofillTree = LocalAutofillTree.current

    LaunchedEffect(Unit) {
        viewModel.navigationEvents.collect { event ->
            when (event) {
                is AuthNavigationEvent.NavigateToHome -> onNavigateToHome()
                else -> {}
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Sign in to continue your Zambian meal journey",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            MealWiseTextField(
                value = uiState.email,
                onValueChange = { viewModel.onEmailChanged(it) },
                label = "Email Address",
                error = uiState.emailError,
                modifier = Modifier
                    .testTag("email_field")
                    .onGloballyPositioned { coordinates ->
                        autofillTree += AutofillNode(
                            autofillTypes = listOf(AutofillType.EmailAddress),
                            onFill = { viewModel.onEmailChanged(it) },
                            boundingBox = coordinates.parentLayoutCoordinates?.localBoundingBoxOf(coordinates)
                        )
                    }
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            autofill?.requestAutofillForNode(autofillTree.children.values.last())
                        }
                    }
            )

            Spacer(modifier = Modifier.height(16.dp))

            MealWisePasswordField(
                value = uiState.password,
                onValueChange = { viewModel.onPasswordChanged(it) },
                label = "Password",
                error = uiState.passwordError,
                isVisible = uiState.isPasswordVisible,
                onToggleVisibility = { viewModel.togglePasswordVisibility() },
                modifier = Modifier
                    .testTag("password_field")
                    .onGloballyPositioned { coordinates ->
                        autofillTree += AutofillNode(
                            autofillTypes = listOf(AutofillType.Password),
                            onFill = { viewModel.onPasswordChanged(it) },
                            boundingBox = coordinates.parentLayoutCoordinates?.localBoundingBoxOf(coordinates)
                        )
                    }
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            autofill?.requestAutofillForNode(autofillTree.children.values.last())
                        }
                    }
            )

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                TextButton(onClick = onNavigateToForgotPassword) {
                    Text("Forgot Password?", style = MaterialTheme.typography.bodyMedium)
                }
            }

            if (uiState.generalError != null) {
                Text(
                    text = uiState.generalError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            LoadingButton(
                text = "Login",
                isLoading = uiState.isLoading,
                onClick = { viewModel.login() },
                modifier = Modifier.testTag("login_button")
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Don't have an account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = onNavigateToRegister,
                    modifier = Modifier.testTag("navigate_register_button")
                ) {
                    Text(
                        "Register", 
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
