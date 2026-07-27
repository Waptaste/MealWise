package com.example.mealwise.ui.nutrition

data class NutritionUiState(
    val dailyCalories: Int = 0,
    val dailyProtein: Int = 0,
    val dailyCarbs: Int = 0,
    val dailyFats: Int = 0,
    val weeklyAverageCalories: Int = 0,
    val calorieGoal: Int = 2000,
    val proteinGoal: Int = 150,
    val carbsGoal: Int = 250,
    val fatsGoal: Int = 70,
    val isLoading: Boolean = false,
    val error: String? = null
)
