package com.example.mealwise.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mealwise.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    val dietaryOptions = listOf("Vegan", "Vegetarian", "Paleo", "Keto", "Gluten-Free", "Low Carb")
    val allergyOptions = listOf("Nuts", "Dairy", "Shellfish", "Eggs", "Soy", "Wheat")
    val goalOptions = listOf("Weight Loss", "Muscle Gain", "Healthy Eating", "Save Time", "Reduce Waste")

    fun nextStep() {
        if (_uiState.value.currentStep < 2) {
            _uiState.value = _uiState.value.copy(currentStep = _uiState.value.currentStep + 1)
        } else {
            completeOnboarding()
        }
    }

    fun previousStep() {
        if (_uiState.value.currentStep > 0) {
            _uiState.value = _uiState.value.copy(currentStep = _uiState.value.currentStep - 1)
        }
    }

    fun toggleDietary(option: String) {
        val current = _uiState.value.selectedDietary
        val updated = if (current.contains(option)) current - option else current + option
        _uiState.value = _uiState.value.copy(selectedDietary = updated)
    }

    fun toggleAllergy(option: String) {
        val current = _uiState.value.selectedAllergies
        val updated = if (current.contains(option)) current - option else current + option
        _uiState.value = _uiState.value.copy(selectedAllergies = updated)
    }

    fun toggleGoal(option: String) {
        val current = _uiState.value.selectedGoals
        val updated = if (current.contains(option)) current - option else current + option
        _uiState.value = _uiState.value.copy(selectedGoals = updated)
    }

    private fun completeOnboarding() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val currentProfileResult = authRepository.getCurrentUserProfile()
            
            currentProfileResult.onSuccess { profile ->
                if (profile != null) {
                    val updatedProfile = profile.copy(
                        dietaryPreferences = _uiState.value.selectedDietary.toList(),
                        allergies = _uiState.value.selectedAllergies.toList(),
                        healthGoals = _uiState.value.selectedGoals.toList(),
                        onboardingCompleted = true
                    )
                    
                    val updateResult = authRepository.updateUserProfile(updatedProfile)
                    updateResult.onSuccess {
                        _uiState.value = _uiState.value.copy(isLoading = false, isComplete = true)
                    }.onFailure { e ->
                        _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                    }
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "User profile not found")
                }
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }
}
