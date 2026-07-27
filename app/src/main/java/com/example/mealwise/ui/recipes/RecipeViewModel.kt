package com.example.mealwise.ui.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
            
            val userProfileResult = authRepository.getCurrentUserProfile()
            val userTags = userProfileResult.getOrNull()?.dietaryPreferences ?: emptyList()
            
            val recipesResult = if (userTags.isNotEmpty()) {
                recipeRepository.getRecipesByTags(userTags)
            } else {
                recipeRepository.getRecipes()
            }
            
            recipesResult.onSuccess { recipes ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    recipes = recipes
                )
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = exception.message ?: "Failed to load recipes"
                )
            }
        }
    }

    fun selectRecipe(recipeId: String) {
        viewModelScope.launch {
            val result = recipeRepository.getRecipeById(recipeId)
            result.onSuccess { recipe ->
                _uiState.value = _uiState.value.copy(selectedRecipe = recipe)
            }
        }
    }
}
