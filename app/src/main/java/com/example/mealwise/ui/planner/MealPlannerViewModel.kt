package com.example.mealwise.ui.planner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mealwise.data.model.MealPlanEntry
import com.example.mealwise.data.model.MealType
import com.example.mealwise.data.model.ShoppingItem
import com.example.mealwise.data.repository.AuthRepository
import com.example.mealwise.data.repository.MealPlanRepository
import com.example.mealwise.data.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject

@HiltViewModel
class MealPlannerViewModel @Inject constructor(
    private val mealPlanRepository: MealPlanRepository,
    private val recipeRepository: RecipeRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MealPlannerUiState())
    val uiState: StateFlow<MealPlannerUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val userId = authRepository.getCurrentUserProfile().getOrNull()?.uid 
            
            if (userId == null) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "User session not found")
                return@launch
            }
            
            recipeRepository.getRecipes().onSuccess { recipes ->
                _uiState.value = _uiState.value.copy(availableRecipes = recipes)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(error = "Failed to load recipes: ${e.message}")
            }

            combine(
                mealPlanRepository.getMealPlanEntries(userId),
                mealPlanRepository.getShoppingListFlow(userId)
            ) { plans, items ->
                plans to items
            }
            .catch { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
            .collect { (plans, items) ->
                // Sort by name for display, but keep them separate if they have different IDs
                val sortedItems = items.sortedBy { it.name }

                _uiState.value = _uiState.value.copy(
                    mealPlans = plans,
                    shoppingList = sortedItems,
                    isLoading = false
                )
                
                ensureIngredientsSync(userId, plans, sortedItems)
            }
        }
    }

    private fun cleanupLegacyItems(userId: String, items: List<ShoppingItem>) {
        viewModelScope.launch {
            val legacyItems = items.filter { it.id != it.name }
            if (legacyItems.isEmpty()) return@launch
            
            val repo = mealPlanRepository as? com.example.mealwise.data.repository.FirebaseMealPlanRepository
            legacyItems.forEach { item ->
                repo?.deleteOldShoppingItem(userId, item.id)
            }
        }
    }

    fun selectDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
    }

    fun addMeal(recipeId: String, mealType: MealType, portionSize: Double = 1.0) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val user = authRepository.getCurrentUserProfile().getOrNull() ?: return@launch
            val recipe = _uiState.value.availableRecipes.find { it.id == recipeId } ?: return@launch
            
            val entry = MealPlanEntry(
                userId = user.uid,
                recipeId = recipeId,
                recipeTitle = recipe.title,
                date = _uiState.value.selectedDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
                mealType = mealType.name,
                portionSize = portionSize
            )
            
            mealPlanRepository.addMealPlanEntry(entry).onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to add meal: ${e.message}")
            }
        }
    }

    fun removeMeal(entryId: String) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserProfile().getOrNull()?.uid ?: return@launch
            (mealPlanRepository as? com.example.mealwise.data.repository.FirebaseMealPlanRepository)
                ?.removeMealPlanEntry(userId, entryId)
        }
    }

    fun toggleShoppingItem(itemId: String) {
        viewModelScope.launch {
            val currentList = _uiState.value.shoppingList.toList()
            val itemIndex = currentList.indexOfFirst { it.id == itemId }
            if (itemIndex == -1) return@launch
            
            val item = currentList[itemIndex]
            val updatedItem = item.copy(isChecked = !item.isChecked)
            
            // Optimistic update
            val updatedList = currentList.toMutableList().apply {
                this[itemIndex] = updatedItem
            }
            _uiState.value = _uiState.value.copy(shoppingList = updatedList)

            mealPlanRepository.updateShoppingItem(updatedItem).onFailure { e ->
                _uiState.value = _uiState.value.copy(shoppingList = currentList, error = "Failed to save: ${e.message}")
            }
        }
    }

    private fun ensureIngredientsSync(userId: String, mealPlans: List<MealPlanEntry>, currentItems: List<ShoppingItem>) {
        viewModelScope.launch {
            if (_uiState.value.availableRecipes.isEmpty()) return@launch

            val neededIngredients = mutableSetOf<String>()
            mealPlans.forEach { plan ->
                val recipe = _uiState.value.availableRecipes.find { it.id == plan.recipeId }
                recipe?.ingredients?.forEach { neededIngredients.add(it) }
            }

            val existingNames = currentItems.map { it.name }.toSet()
            val missingIngredients = neededIngredients.filter { !existingNames.contains(it) }

            if (missingIngredients.isNotEmpty()) {
                val newItems = missingIngredients.map { name ->
                    ShoppingItem(id = name, name = name, userId = userId, isChecked = false)
                }
                mealPlanRepository.saveShoppingList(userId, newItems)
            }
        }
    }
}
