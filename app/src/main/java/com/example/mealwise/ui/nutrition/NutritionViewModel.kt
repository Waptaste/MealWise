package com.example.mealwise.ui.nutrition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mealwise.data.model.MealPlanEntry
import com.example.mealwise.data.model.Recipe
import com.example.mealwise.data.repository.AuthRepository
import com.example.mealwise.data.repository.MealPlanRepository
import com.example.mealwise.data.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject

@HiltViewModel
class NutritionViewModel @Inject constructor(
    private val mealPlanRepository: MealPlanRepository,
    private val recipeRepository: RecipeRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NutritionUiState())
    val uiState: StateFlow<NutritionUiState> = _uiState.asStateFlow()

    private var allRecipes: List<Recipe> = emptyList()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val user = authRepository.getCurrentUserProfile().getOrNull()
            val userId = user?.uid ?: return@launch
            
            // Set goals based on user profile goals
            val userGoals = user.healthGoals
            val calorieGoal = if (userGoals.contains("Weight Loss")) 1800 else if (userGoals.contains("Muscle Gain")) 2500 else 2000
            
            _uiState.value = _uiState.value.copy(
                calorieGoal = calorieGoal,
                proteinGoal = (calorieGoal * 0.25 / 4).toInt(),
                carbsGoal = (calorieGoal * 0.50 / 4).toInt(),
                fatsGoal = (calorieGoal * 0.25 / 9).toInt()
            )

            recipeRepository.getRecipes().onSuccess { recipes ->
                allRecipes = recipes
            }

            mealPlanRepository.getMealPlanEntries(userId).collect { entries ->
                calculateNutrition(entries)
            }
        }
    }

    private fun calculateNutrition(entries: List<MealPlanEntry>) {
        val today = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        val todayEntries = entries.filter { it.date == today }
        
        var totalCalories = 0
        var totalProtein = 0
        var totalCarbs = 0
        var totalFats = 0
        
        todayEntries.forEach { entry ->
            val recipe = allRecipes.find { it.id == entry.recipeId }
            if (recipe != null) {
                totalCalories += recipe.calories
                totalProtein += recipe.proteinGrams
                totalCarbs += recipe.carbsGrams
                totalFats += recipe.fatsGrams
            }
        }

        // Simple weekly average calculation (sum of all plans / 7)
        var weeklyTotalCalories = 0
        entries.forEach { entry ->
            val recipe = allRecipes.find { it.id == entry.recipeId }
            weeklyTotalCalories += recipe?.calories ?: 0
        }

        _uiState.value = _uiState.value.copy(
            dailyCalories = totalCalories,
            dailyProtein = totalProtein,
            dailyCarbs = totalCarbs,
            dailyFats = totalFats,
            weeklyAverageCalories = weeklyTotalCalories / 7,
            isLoading = false
        )
    }
}
