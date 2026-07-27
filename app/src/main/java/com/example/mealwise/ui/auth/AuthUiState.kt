package com.example.mealwise.ui.auth

import com.example.mealwise.data.model.UserProfile

data class AuthUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val generalError: String? = null,
    val isLoading: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val authenticatedProfile: UserProfile? = null,
    val isCheckingAuth: Boolean = true
)
