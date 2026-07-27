package com.example.mealwise.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mealwise.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _navigationEvents = MutableSharedFlow<AuthNavigationEvent>(replay = 1)
    val navigationEvents: SharedFlow<AuthNavigationEvent> = _navigationEvents.asSharedFlow()

    private val emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCheckingAuth = true)
            if (authRepository.isUserAuthenticated()) {
                val result = authRepository.getCurrentUserProfile()
                result.onSuccess { profile ->
                    if (profile != null) {
                        _uiState.value = _uiState.value.copy(
                            authenticatedProfile = profile,
                            isCheckingAuth = false
                        )
                        _navigationEvents.emit(AuthNavigationEvent.NavigateToHome)
                    } else {
                        // User exists in Auth but no profile in Firestore
                        _uiState.value = _uiState.value.copy(
                            isCheckingAuth = false,
                            generalError = "Account profile missing. Please log in again."
                        )
                        logout() // Clear session
                    }
                }.onFailure {
                    _uiState.value = _uiState.value.copy(isCheckingAuth = false)
                    _navigationEvents.emit(AuthNavigationEvent.NavigateToLogin)
                }
            } else {
                _uiState.value = _uiState.value.copy(isCheckingAuth = false)
                _navigationEvents.emit(AuthNavigationEvent.NavigateToLogin)
            }
        }
    }

    fun onNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(name = name, nameError = null)
    }

    fun onEmailChanged(email: String) {
        _uiState.value = _uiState.value.copy(email = email, emailError = null)
    }

    fun onPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(password = password, passwordError = null)
    }

    fun onConfirmPasswordChanged(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = confirmPassword, confirmPasswordError = null)
    }

    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(isPasswordVisible = !_uiState.value.isPasswordVisible)
    }

    fun toggleConfirmPasswordVisibility() {
        _uiState.value = _uiState.value.copy(isConfirmPasswordVisible = !_uiState.value.isConfirmPasswordVisible)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(generalError = null)
    }

    fun login() {
        if (validateLoginForm()) {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true, generalError = null)
                val result = authRepository.login(
                    email = _uiState.value.email,
                    password = _uiState.value.password
                )
                
                result.onSuccess { profile ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        authenticatedProfile = profile
                    )
                    _navigationEvents.emit(AuthNavigationEvent.NavigateToHome)
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        generalError = exception.message ?: "An unknown error occurred during login"
                    )
                }
            }
        }
    }

    fun register() {
        if (validateRegisterForm()) {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true, generalError = null)
                val result = authRepository.register(
                    name = _uiState.value.name,
                    email = _uiState.value.email,
                    password = _uiState.value.password
                )
                
                result.onSuccess { profile ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        authenticatedProfile = profile
                    )
                    _navigationEvents.emit(AuthNavigationEvent.NavigateToHome)
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        generalError = exception.message ?: "An unknown error occurred during registration"
                    )
                }
            }
        }
    }

    fun logout() {
        authRepository.logout()
        _uiState.value = AuthUiState(isCheckingAuth = false)
        viewModelScope.launch {
            _navigationEvents.emit(AuthNavigationEvent.NavigateToLogin)
        }
    }

    private fun validateLoginForm(): Boolean {
        var isValid = true
        val email = _uiState.value.email
        val password = _uiState.value.password

        if (email.isBlank()) {
            _uiState.value = _uiState.value.copy(emailError = "Email is required")
            isValid = false
        } else if (!isValidEmail(email)) {
            _uiState.value = _uiState.value.copy(emailError = "Invalid email format")
            isValid = false
        }

        if (password.isBlank()) {
            _uiState.value = _uiState.value.copy(passwordError = "Password is required")
            isValid = false
        }

        return isValid
    }

    private fun validateRegisterForm(): Boolean {
        var isValid = true
        val state = _uiState.value

        if (state.name.isBlank()) {
            _uiState.value = _uiState.value.copy(nameError = "Name is required")
            isValid = false
        } else if (state.name.trim().length < 2) {
            _uiState.value = _uiState.value.copy(nameError = "Name must be at least 2 characters")
            isValid = false
        }

        if (state.email.isBlank()) {
            _uiState.value = _uiState.value.copy(emailError = "Email is required")
            isValid = false
        } else if (!isValidEmail(state.email)) {
            _uiState.value = _uiState.value.copy(emailError = "Invalid email format")
            isValid = false
        }

        val passwordError = validatePassword(state.password)
        if (passwordError != null) {
            _uiState.value = _uiState.value.copy(passwordError = passwordError)
            isValid = false
        }

        if (state.confirmPassword != state.password) {
            _uiState.value = _uiState.value.copy(confirmPasswordError = "Passwords do not match")
            isValid = false
        }

        return isValid
    }

    private fun isValidEmail(email: String): Boolean {
        return emailPattern.matches(email)
    }

    private fun validatePassword(password: String): String? {
        if (password.isBlank()) return "Password is required"
        if (password.length < 8) return "Password must be at least 8 characters"
        if (!password.any { it.isUpperCase() }) return "Password must contain an uppercase letter"
        if (!password.any { it.isLowerCase() }) return "Password must contain a lowercase letter"
        if (!password.any { it.isDigit() }) return "Password must contain a digit"
        return null
    }
}

sealed class AuthNavigationEvent {
    object NavigateToHome : AuthNavigationEvent()
    object NavigateToLogin : AuthNavigationEvent()
}
