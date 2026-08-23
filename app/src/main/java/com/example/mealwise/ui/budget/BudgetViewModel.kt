package com.example.mealwise.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mealwise.data.model.BudgetCategory
import com.example.mealwise.data.model.BudgetItem
import com.example.mealwise.data.model.Commodity
import com.example.mealwise.data.model.MonthlyBudget
import com.example.mealwise.data.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository
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
        _uiState.value = _uiState.value.copy(selectedCategory = category, generatedBudget = null)
    }

    fun onAmountChanged(amount: String) {
        _uiState.value = _uiState.value.copy(enteredAmount = amount, error = null)
    }

    fun onHouseholdSizeChanged(size: String) {
        _uiState.value = _uiState.value.copy(householdSize = size)
    }

    fun generateBudget() {
        val totalAmountInput = _uiState.value.enteredAmount.toDoubleOrNull()
        val size = _uiState.value.householdSize.toIntOrNull() ?: 1
        
        if (totalAmountInput == null) {
            _uiState.value = _uiState.value.copy(error = "Please enter a valid amount")
            return
        }

        val totalAmount: Double = totalAmountInput
        val category = _uiState.value.selectedCategory
        
        val isValidRange = when (category) {
            BudgetCategory.ECONOMICAL -> totalAmount in 500.0..700.0
            BudgetCategory.AVERAGE -> totalAmount in 700.0..2000.0
            BudgetCategory.ENJOYING -> totalAmount >= 2000.0
        }

        if (!isValidRange) {
            val range = when (category) {
                BudgetCategory.ECONOMICAL -> "K500 - K700"
                BudgetCategory.AVERAGE -> "K700 - K2000"
                BudgetCategory.ENJOYING -> "at least K2000"
            }
            _uiState.value = _uiState.value.copy(error = "Amount for ${category.name} must be $range")
            return
        }

        val commodities = _uiState.value.commodities
        val mustHaves = commodities.filter { it.isMustHave }
        val categoryOptionals = commodities.filter { !it.isMustHave && it.category == category }
        
        val budgetItems = mutableListOf<BudgetItem>()
        var runningAmount: Double = totalAmount

        // 1. Allocate FULL "Must-Have" budget first
        mustHaves.forEach { commodity ->
            val quantity = commodity.baseQuantityPerPerson * size
            val cost = quantity * commodity.unitPrice
            budgetItems.add(BudgetItem(commodity.name, quantity, commodity.unit, cost, isMustHave = true, isStaple = commodity.isStaple))
            runningAmount -= cost
        }

        // 2. Allocate remaining budget to "Category-Specific" optional items
        if (categoryOptionals.isNotEmpty() && runningAmount > 0) {
            var optionalBaseTotal = 0.0
            categoryOptionals.forEach { optionalBaseTotal += it.baseQuantityPerPerson * size * it.unitPrice }
            
            val scalingFactor = if (optionalBaseTotal > 0) runningAmount / optionalBaseTotal else 0.0
            
            categoryOptionals.forEach { commodity ->
                val quantity = commodity.baseQuantityPerPerson * size * scalingFactor
                val cost = quantity * commodity.unitPrice
                budgetItems.add(BudgetItem(commodity.name, quantity, commodity.unit, cost, isMustHave = false, isStaple = commodity.isStaple))
            }
            runningAmount = 0.0
        }

        // 3. Handle Overage with Staple Protection
        if (runningAmount < 0) {
            val deficit = -runningAmount
            val mustHaveNonStaples = budgetItems.filter { it.isMustHave && !it.isStaple }
            val mustHaveStaples = budgetItems.filter { it.isMustHave && it.isStaple }
            
            val totalNonStapleCost = mustHaveNonStaples.sumOf { it.totalCost }
            
            val finalItems = if (totalNonStapleCost >= deficit) {
                // We can cover the deficit just by scaling down non-staple must-haves
                val scaleDownFactor = (totalNonStapleCost - deficit) / totalNonStapleCost
                budgetItems.map { item ->
                    if (item.isMustHave && !item.isStaple) {
                        item.copy(quantity = item.quantity * scaleDownFactor, totalCost = item.totalCost * scaleDownFactor)
                    } else {
                        item
                    }
                }
            } else {
                // Deficit is so large even staples must be scaled
                val remainingDeficit = deficit - totalNonStapleCost
                val totalStapleCost = mustHaveStaples.sumOf { it.totalCost }
                val stapleScaleFactor = if (totalStapleCost > 0) (totalStapleCost - remainingDeficit) / totalStapleCost else 0.0
                
                budgetItems.map { item ->
                    if (item.isMustHave && !item.isStaple) {
                        item.copy(quantity = 0.0, totalCost = 0.0)
                    } else if (item.isMustHave && item.isStaple) {
                        item.copy(quantity = item.quantity * stapleScaleFactor, totalCost = item.totalCost * stapleScaleFactor)
                    } else {
                        item
                    }
                }
            }
            
            _uiState.value = _uiState.value.copy(
                generatedBudget = MonthlyBudget(totalAmount, category, size, finalItems)
            )
        } else {
            _uiState.value = _uiState.value.copy(
                generatedBudget = MonthlyBudget(totalAmount, category, size, budgetItems)
            )
        }
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
                val quantity = if (newCommodity.unitPrice > 0) item.totalCost / newCommodity.unitPrice else 0.0
                BudgetItem(
                    commodityName = newCommodity.name,
                    quantity = quantity,
                    unit = newCommodity.unit,
                    totalCost = item.totalCost,
                    isMustHave = newCommodity.isMustHave
                )
            } else {
                item
            }
        }
        
        _uiState.value = _uiState.value.copy(
            generatedBudget = currentBudget.copy(items = newItems),
            itemToReplace = null
        )
    }

    fun updatePrice(id: String, newPrice: Double) {
        viewModelScope.launch {
            val currentList = _uiState.value.commodities
            val updatedList = currentList.map { 
                if (it.id == id) it.copy(unitPrice = newPrice) else it 
            }
            _uiState.value = _uiState.value.copy(commodities = updatedList)
            budgetRepository.updateCommodityPrice(id, newPrice).onFailure {
                _uiState.value = _uiState.value.copy(commodities = currentList)
            }
        }
    }

    fun toggleMustHave(id: String, isMustHave: Boolean) {
        viewModelScope.launch {
            val currentList = _uiState.value.commodities
            val updatedList = currentList.map { 
                if (it.id == id) it.copy(isMustHave = isMustHave) else it 
            }
            _uiState.value = _uiState.value.copy(commodities = updatedList)
            budgetRepository.updateCommodityMustHave(id, isMustHave).onFailure {
                _uiState.value = _uiState.value.copy(commodities = currentList)
            }
        }
    }

    fun toggleStaple(id: String, isStaple: Boolean) {
        viewModelScope.launch {
            val currentList = _uiState.value.commodities
            val updatedList = currentList.map { 
                if (it.id == id) it.copy(isStaple = isStaple) else it 
            }
            _uiState.value = _uiState.value.copy(commodities = updatedList)
            budgetRepository.updateCommodityStapleStatus(id, isStaple).onFailure {
                _uiState.value = _uiState.value.copy(commodities = currentList)
            }
        }
    }

    fun addCustomCommodity(
        name: String, 
        price: Double, 
        unit: String, 
        category: BudgetCategory,
        isMustHave: Boolean = false,
        isStaple: Boolean = false
    ) {
        viewModelScope.launch {
            val commodity = Commodity(
                name = name,
                unitPrice = price,
                unit = unit,
                category = category,
                baseQuantityPerPerson = 1.0,
                isMustHave = isMustHave,
                isStaple = isStaple
            )
            budgetRepository.addCommodity(commodity)
        }
    }

    fun deleteCommodity(id: String) {
        viewModelScope.launch {
            budgetRepository.deleteCommodity(id)
        }
    }
    
    fun retryLoading() {
        loadCommodities()
    }
}
