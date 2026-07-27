package com.example.mealwise.ui.recipes

import com.example.mealwise.data.model.Recipe

data class RecipeUiState(
    val recipes: List<Recipe> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedRecipe: Recipe? = null
)
