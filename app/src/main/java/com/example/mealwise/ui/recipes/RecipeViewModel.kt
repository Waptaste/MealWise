package com.example.mealwise.ui.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mealwise.data.model.Recipe
import com.example.mealwise.data.repository.AuthRepository
import com.example.mealwise.data.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

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
            
            // --- AI SIMULATION DELAY ---
            // Makes the user feel like the AI is actually "thinking" and improving the recipe
            delay(2000)

            val userProfile = authRepository.getCurrentUserProfile().getOrNull()
            
            if (userProfile == null) {
                _uiState.value = _uiState.value.copy(error = "User not logged in", isLoading = false)
                return@launch
            }

            // --- IMPROVED AI SIMULATION LOGIC ---
            // Better parsing and structuring of the recipe
            val lines = rawInstructions.split("\n").map { it.trim() }.filter { it.isNotBlank() }
            
            val finalIngredients = mutableListOf<String>()
            val finalInstructions = mutableListOf<String>()
            
            lines.forEach { line ->
                // Heuristic: lines starting with numbers or symbols are likely ingredients
                if (line.any { it.isDigit() } || line.startsWith("-") || line.startsWith("*") || line.length < 25) {
                    finalIngredients.add(line.removePrefix("-").removePrefix("*").trim())
                } else {
                    finalInstructions.add(line)
                }
            }

            // Fallbacks if one side is empty
            if (finalIngredients.isEmpty()) finalIngredients.add("Main components from: $title")
            if (finalInstructions.isEmpty()) finalInstructions.add("Prepare as traditionally preferred for $title.")

            // Assign a default Zambian-themed image if it's an AI-improved recipe
            val defaultImages = listOf(
                "https://zambiankitchen.com/wp-content/uploads/2016/11/Village-Chicken.jpg",
                "https://www.worldfoodtravel.org/wp-content/uploads/2020/04/Zambia-Nshima.jpg",
                "https://zambiankitchen.com/wp-content/uploads/2017/09/Munkoyo.jpg"
            )
            val randomImage = defaultImages.random()

            val improvedRecipe = Recipe(
                id = "", // Let Firestore generate ID
                title = title.ifBlank { "AI Optimized Recipe" },
                description = "AI-Improved Zambian Delight: A nutritionally balanced and traditional version of your recipe notes.",
                imageUrl = randomImage,
                ingredients = finalIngredients,
                instructions = finalInstructions,
                dietaryTags = listOf("User-Created", "AI-Improved", "Traditional"),
                calories = Random.nextInt(300, 600),
                proteinGrams = Random.nextInt(15, 35),
                carbsGrams = Random.nextInt(40, 70),
                fatsGrams = Random.nextInt(5, 20),
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
