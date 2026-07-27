package com.example.mealwise.data.model

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val createdAt: Long = 0L,
    val onboardingCompleted: Boolean = false,
    val dietaryPreferences: List<String> = emptyList(),
    val allergies: List<String> = emptyList(),
    val healthGoals: List<String> = emptyList()
)
