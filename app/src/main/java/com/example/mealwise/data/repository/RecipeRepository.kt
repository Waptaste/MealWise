package com.example.mealwise.data.repository

import com.example.mealwise.data.model.Recipe

interface RecipeRepository {
    suspend fun getRecipes(): Result<List<Recipe>>
    suspend fun getRecipeById(id: String): Result<Recipe?>
    suspend fun getRecipesByTags(tags: List<String>): Result<List<Recipe>>
    suspend fun addRecipe(recipe: Recipe, userId: String? = null): Result<Unit>
}
