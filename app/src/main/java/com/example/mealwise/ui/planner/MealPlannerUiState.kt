package com.example.mealwise.ui.planner

import com.example.mealwise.data.model.MealPlanEntry
import com.example.mealwise.data.model.Recipe
import com.example.mealwise.data.model.ShoppingItem
import java.time.LocalDate

data class MealPlannerUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val mealPlans: List<MealPlanEntry> = emptyList(),
    val shoppingList: List<ShoppingItem> = emptyList(),
    val availableRecipes: List<Recipe> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
