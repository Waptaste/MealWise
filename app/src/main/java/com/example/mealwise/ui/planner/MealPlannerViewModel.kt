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
import kotlinx.coroutines.delay
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

    // Tracking items that are currently being updated to prevent race conditions with Firestore flow
    // Map of itemId to isChecked state
    private val pendingToggles = MutableStateFlow<Map<String, Boolean>>(emptyMap())

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
                mealPlanRepository.getShoppingListFlow(userId),
                pendingToggles
            ) { plans, items, pending ->
                Triple(plans, items, pending)
            }
            .catch { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
            .collect { (plans, items, pending) ->
                // Sort by name for display
                val sortedItems = items.sortedBy { it.name }

                // Merge incoming items with pending local changes to prevent "uncrossing" bug
                val mergedItems = sortedItems.map { item ->
                    if (pending.containsKey(item.id)) {
                        // Keep the local state if an update is still in flight
                        item.copy(isChecked = pending[item.id] ?: item.isChecked)
                    } else {
                        item
                    }
                }

                _uiState.value = _uiState.value.copy(
                    mealPlans = plans,
                    shoppingList = mergedItems,
                    isLoading = false
                )
                
                // Remove from pending only when the backend state matches our intended state
                val itemsToClear = pending.filter { (id, desiredChecked) ->
                    val serverItem = items.find { it.id == id }
                    serverItem != null && serverItem.isChecked == desiredChecked
                }.keys
                
                if (itemsToClear.isNotEmpty()) {
                    // Small delay to ensure any transient UI states settle
                    viewModelScope.launch {
                        delay(200)
                        pendingToggles.value = pendingToggles.value - itemsToClear
                    }
                }
                
                ensureIngredientsSync(userId, plans, mergedItems)
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
            val newCheckedState = !item.isChecked
            val updatedItem = item.copy(isChecked = newCheckedState)
            
            // Mark as pending with the intended state
            pendingToggles.value = pendingToggles.value + (itemId to newCheckedState)
            
            // Optimistic update
            val updatedList = currentList.toMutableList().apply {
                this[itemIndex] = updatedItem
            }
            _uiState.value = _uiState.value.copy(shoppingList = updatedList)

            mealPlanRepository.updateShoppingItem(updatedItem)
                .onFailure { e ->
                    // Remove from pending on failure to revert to server state immediately
                    pendingToggles.value = pendingToggles.value - itemId
                    _uiState.value = _uiState.value.copy(shoppingList = currentList, error = "Failed to save: ${e.message}")
                }
            // On success, we wait for the flow to reconcile and remove it from pendingToggles (handled in loadData)
            // On success, we wait for the flow to reconcile and remove it from pendingToggles (handled in loadData)
        }
    }

    private fun ensureIngredientsSync(userId: String, mealPlans: List<MealPlanEntry>, currentItems: List<ShoppingItem>) {
        viewModelScope.launch {
            if (_uiState.value.availableRecipes.isEmpty()) return@launch

            // Use a case-insensitive set for comparison
            val neededIngredients = mutableSetOf<String>()
            mealPlans.forEach { plan ->
                val recipe = _uiState.value.availableRecipes.find { it.id == plan.recipeId }
                recipe?.ingredients?.forEach { ingredient ->
                    val normalized = ingredient.trim()
                    if (normalized.isNotBlank()) {
                        neededIngredients.add(normalized)
                    }
                }
            }

            val existingNames = currentItems.map { it.name.trim().lowercase() }.toSet()
            // Only add if it doesn't exist (case insensitive) AND we aren't already trying to add it
            val missingIngredients = neededIngredients.filter { ingredient ->
                val normalized = ingredient.lowercase()
                !existingNames.contains(normalized)
            }

            if (missingIngredients.isNotEmpty()) {
                // To prevent immediate repeated calls, we filter out duplicates in the missing list itself
                val uniqueMissing = missingIngredients.distinctBy { it.lowercase() }
                val newItems = uniqueMissing.map { name ->
                    ShoppingItem(id = name, name = name, userId = userId, isChecked = false)
                }
                mealPlanRepository.saveShoppingList(userId, newItems)
            }
        }
    }
}
