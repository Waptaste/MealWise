package com.example.mealwise.ui.onboarding

data class OnboardingUiState(
    val currentStep: Int = 0,
    val selectedDietary: Set<String> = emptySet(),
    val selectedAllergies: Set<String> = emptySet(),
    val selectedGoals: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isComplete: Boolean = false
)
