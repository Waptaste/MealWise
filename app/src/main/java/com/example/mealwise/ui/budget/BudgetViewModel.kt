package com.example.mealwise.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mealwise.data.model.BudgetCategory
import com.example.mealwise.data.model.BudgetItem
import com.example.mealwise.data.model.Commodity
import com.example.mealwise.data.model.MonthlyBudget
import com.example.mealwise.data.model.ShoppingItem
import com.example.mealwise.data.repository.AuthRepository
import com.example.mealwise.data.repository.BudgetRepository
import com.example.mealwise.data.repository.MealPlanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.floor

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val mealPlanRepository: MealPlanRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    init {
        loadCommodities()
    }

    private fun loadCommodities() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            budgetRepository.getCommodities()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Database error: ${e.message}")
                }
                .collect { commodities ->
                    if (commodities.isEmpty()) {
                        budgetRepository.initializeDefaultCommodities()
                    } else {
                        _uiState.value = _uiState.value.copy(
                            commodities = commodities,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    fun onCategorySelected(category: BudgetCategory) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun onAmountChanged(amount: String) {
        _uiState.value = _uiState.value.copy(enteredAmount = amount, error = null)
    }

    fun onHouseholdSizeChanged(size: String) {
        _uiState.value = _uiState.value.copy(householdSize = size)
        calculateWishlistTotal()
    }

    fun addToWishlist(commodity: Commodity) {
        val currentWishlist = _uiState.value.wishlist.toMutableList()
        if (!currentWishlist.any { it.id == commodity.id }) {
            currentWishlist.add(commodity)
            _uiState.value = _uiState.value.copy(wishlist = currentWishlist)
            calculateWishlistTotal()
        }
    }

    fun removeFromWishlist(commodityId: String) {
        val currentWishlist = _uiState.value.wishlist.filter { it.id != commodityId }
        _uiState.value = _uiState.value.copy(wishlist = currentWishlist)
        calculateWishlistTotal()
    }

    private fun calculateWishlistTotal() {
        val size = _uiState.value.householdSize.toIntOrNull() ?: 1
        val total = _uiState.value.wishlist.sumOf { it.unitPrice * it.baseQuantityPerPerson * size }
        _uiState.value = _uiState.value.copy(wishlistTotal = total)
    }

    fun generateOptimizedBudget() {
        val cashAmount = _uiState.value.enteredAmount.toDoubleOrNull()
        val size = _uiState.value.householdSize.toIntOrNull() ?: 1
        
        if (cashAmount == null) {
            _uiState.value = _uiState.value.copy(error = "Please enter your available cash")
            return
        }

        var currentBudgetItems = mutableListOf<BudgetItem>()
        val currentWishlist = _uiState.value.wishlist.toMutableList()
        
        // 1. Initial calculation based on wishlist
        currentWishlist.forEach { commodity ->
            val qty = if (commodity.isDiscrete) floor(commodity.baseQuantityPerPerson * size).coerceAtLeast(1.0) else commodity.baseQuantityPerPerson * size
            currentBudgetItems.add(BudgetItem(
                commodityName = commodity.name,
                quantity = qty,
                unit = commodity.unit,
                totalCost = qty * commodity.unitPrice,
                isMustHave = commodity.isMustHave,
                isStaple = commodity.isStaple,
                isDiscrete = commodity.isDiscrete
            ))
        }

        // 2. Optimization: If over budget, swap for cheaper alternatives
        var currentTotal = currentBudgetItems.sumOf { it.totalCost }
        
        if (currentTotal > cashAmount) {
            // Find "Enjoying" or "Average" items to swap
            val swappableIndices = currentBudgetItems.indices.filter { !currentBudgetItems[it].isStaple }
            
            for (index in swappableIndices) {
                if (currentTotal <= cashAmount) break
                
                val item = currentBudgetItems[index]
                val originalCommodity = _uiState.value.commodities.find { it.name == item.commodityName } ?: continue
                
                // Find a cheaper alternative (same name but lower category, or common swap)
                val alternative = findCheaperAlternative(originalCommodity)
                if (alternative != null) {
                    val newQty = if (alternative.isDiscrete) floor(alternative.baseQuantityPerPerson * size).coerceAtLeast(1.0) else alternative.baseQuantityPerPerson * size
                    val newCost = newQty * alternative.unitPrice
                    
                    currentTotal = currentTotal - item.totalCost + newCost
                    currentBudgetItems[index] = BudgetItem(
                        commodityName = alternative.name + " (Saved ZMW)",
                        quantity = newQty,
                        unit = alternative.unit,
                        totalCost = newCost,
                        isMustHave = alternative.isMustHave,
                        isStaple = alternative.isStaple,
                        isDiscrete = alternative.isDiscrete
                    )
                }
            }
        }

        // 3. Final scaling if still over budget (scale non-staples first)
        if (currentTotal > cashAmount) {
            val scalingFactor = cashAmount / currentTotal
            currentBudgetItems = currentBudgetItems.map { item ->
                if (!item.isStaple) {
                    val newQty = if (item.isDiscrete) floor(item.quantity * scalingFactor).coerceAtLeast(1.0) else item.quantity * scalingFactor
                    item.copy(quantity = newQty, totalCost = newQty * (item.totalCost / item.quantity))
                } else item
            }.toMutableList()
        }

        _uiState.value = _uiState.value.copy(
            generatedBudget = MonthlyBudget(cashAmount, _uiState.value.selectedCategory, size, currentBudgetItems),
            isWishlistMode = false
        )
    }

    private fun findCheaperAlternative(original: Commodity): Commodity? {
        // Logic: Find something in a lower category that might serve a similar purpose
        // For a true implementation, we'd have a "tags" or "group" field.
        // For now, we'll use name-based matching.
        return when {
            original.name.contains("Breakfast", true) -> _uiState.value.commodities.find { it.name.contains("Roller", true) }
            original.name.contains("Beef", true) || original.name.contains("Chicken", true) -> _uiState.value.commodities.find { it.name.contains("Soy", true) }
            original.name.contains("Rice", true) -> _uiState.value.commodities.find { it.name.contains("Mealie", true) }
            else -> null
        }
    }

    fun switchToWishlist() {
        _uiState.value = _uiState.value.copy(isWishlistMode = true)
    }

    fun onReplaceItemClick(itemName: String) {
        _uiState.value = _uiState.value.copy(itemToReplace = itemName)
    }

    fun dismissReplacementDialog() {
        _uiState.value = _uiState.value.copy(itemToReplace = null)
    }

    fun applyReplacement(oldItemName: String, newCommodity: Commodity) {
        val currentBudget = _uiState.value.generatedBudget ?: return
        val newItems = currentBudget.items.map { item ->
            if (item.commodityName == oldItemName) {
                var quantity = if (newCommodity.unitPrice > 0) item.totalCost / newCommodity.unitPrice else 0.0
                if (newCommodity.isDiscrete) quantity = floor(quantity)
                BudgetItem(
                    commodityName = newCommodity.name,
                    quantity = quantity,
                    unit = newCommodity.unit,
                    totalCost = quantity * newCommodity.unitPrice,
                    isMustHave = newCommodity.isMustHave,
                    isStaple = newCommodity.isStaple,
                    isDiscrete = newCommodity.isDiscrete
                )
            } else item
        }
        _uiState.value = _uiState.value.copy(generatedBudget = currentBudget.copy(items = newItems), itemToReplace = null)
    }

    fun updatePrice(id: String, newPrice: Double) {
        viewModelScope.launch {
            val currentList = _uiState.value.commodities
            val updatedList = currentList.map { if (it.id == id) it.copy(unitPrice = newPrice) else it }
            _uiState.value = _uiState.value.copy(commodities = updatedList)
            budgetRepository.updateCommodityPrice(id, newPrice).onFailure {
                _uiState.value = _uiState.value.copy(commodities = currentList)
            }
        }
    }

    fun toggleMustHave(id: String, isMustHave: Boolean) {
        viewModelScope.launch {
            val currentList = _uiState.value.commodities
            val updatedList = currentList.map { if (it.id == id) it.copy(isMustHave = isMustHave) else it }
            _uiState.value = _uiState.value.copy(commodities = updatedList)
            budgetRepository.updateCommodityMustHave(id, isMustHave).onFailure {
                _uiState.value = _uiState.value.copy(commodities = currentList)
            }
        }
    }

    fun toggleStaple(id: String, isStaple: Boolean) {
        viewModelScope.launch {
            val currentList = _uiState.value.commodities
            val updatedList = currentList.map { if (it.id == id) it.copy(isStaple = isStaple) else it }
            _uiState.value = _uiState.value.copy(commodities = updatedList)
            budgetRepository.updateCommodityStapleStatus(id, isStaple).onFailure {
                _uiState.value = _uiState.value.copy(commodities = currentList)
            }
        }
    }

    fun addCustomCommodity(name: String, price: Double, unit: String, category: BudgetCategory, isMustHave: Boolean = false, isStaple: Boolean = false, isDiscrete: Boolean = false) {
        viewModelScope.launch {
            val commodity = Commodity(name = name, unitPrice = price, unit = unit, category = category, baseQuantityPerPerson = 1.0, isMustHave = isMustHave, isStaple = isStaple, isDiscrete = isDiscrete)
            budgetRepository.addCommodity(commodity)
        }
    }

    fun deleteCommodity(id: String) {
        viewModelScope.launch { budgetRepository.deleteCommodity(id) }
    }

    fun saveBudgetToShoppingList() {
        viewModelScope.launch {
            val budget = _uiState.value.generatedBudget ?: return@launch
            val user = authRepository.getCurrentUserProfile().getOrNull() ?: return@launch
            
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val shoppingItems = budget.items.map { item ->
                ShoppingItem(
                    id = "budget_${System.currentTimeMillis()}_${item.commodityName}",
                    userId = user.uid,
                    name = "${item.commodityName} (${if (item.isDiscrete) item.quantity.toInt() else "%.1f".format(item.quantity)} ${item.unit})",
                    isChecked = false
                )
            }
            
            mealPlanRepository.saveShoppingList(user.uid, shoppingItems).onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to save: ${e.message}")
            }
        }
    }
    
    fun retryLoading() { loadCommodities() }
}
