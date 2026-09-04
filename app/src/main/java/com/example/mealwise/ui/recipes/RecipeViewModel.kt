package com.example.mealwise.ui.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mealwise.data.model.Recipe
import com.example.mealwise.data.repository.AuthRepository
import com.example.mealwise.data.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipeUiState())
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

    init {
        loadRecipes()
    }

    fun loadRecipes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val userProfile = authRepository.getCurrentUserProfile().getOrNull()
            
            recipeRepository.getRecipes().onSuccess { allRecipes ->
                // Filter by dietary preference if available
                val filtered = if (userProfile != null && userProfile.dietaryPreferences.isNotEmpty()) {
                    allRecipes.filter { recipe ->
                        recipe.dietaryTags.any { tag -> userProfile.dietaryPreferences.contains(tag) } || 
                        recipe.dietaryTags.isEmpty()
                    }
                } else {
                    allRecipes
                }
                
                _uiState.value = _uiState.value.copy(
                    recipes = filtered,
                    isLoading = false
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load recipes: ${e.message}"
                )
            }
        }
    }

    fun addAndImproveRecipe(title: String, rawInstructions: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val userProfile = authRepository.getCurrentUserProfile().getOrNull()
            
            if (userProfile == null) {
                _uiState.value = _uiState.value.copy(error = "User not logged in", isLoading = false)
                return@launch
            }

            // --- AI SIMULATION (Phase C) ---
            val improvedRecipe = Recipe(
                id = "", // Let Firestore generate ID
                title = title,
                description = "AI-Improved Zambian Delight: A balanced and healthy version of your recipe.",
                instructions = rawInstructions.split("\n").filter { it.isNotBlank() },
                dietaryTags = listOf("User-Created", "Traditional"),
                calories = 350, // AI Estimated
                proteinGrams = 12,
                carbsGrams = 45,
                fatsGrams = 8,
                isUserCreated = true,
                creatorId = userProfile.uid
            )

            recipeRepository.addRecipe(improvedRecipe, userProfile.uid).onSuccess {
                loadRecipes()
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(error = "Permission denied or failed to save: ${e.message}", isLoading = false)
            }
        }
    }

    fun selectRecipe(id: String) {
        viewModelScope.launch {
            recipeRepository.getRecipeById(id).onSuccess { recipe ->
                _uiState.value = _uiState.value.copy(selectedRecipe = recipe)
            }
        }
    }
}
