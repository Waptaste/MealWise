package com.example.mealwise.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mealwise.data.model.BudgetCategory
import com.example.mealwise.data.model.BudgetItem
import com.example.mealwise.data.model.MonthlyBudget
import com.example.mealwise.data.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            budgetRepository.getCommodities().collect { commodities ->
                if (commodities.isEmpty()) {
                    budgetRepository.initializeDefaultCommodities()
                }
                _uiState.value = _uiState.value.copy(commodities = commodities)
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
        val amount = _uiState.value.enteredAmount.toDoubleOrNull()
        val size = _uiState.value.householdSize.toIntOrNull() ?: 1
        
        if (amount == null) {
            _uiState.value = _uiState.value.copy(error = "Please enter a valid amount")
            return
        }

        // Validate amount ranges
        val category = _uiState.value.selectedCategory
        val isValidRange = when (category) {
            BudgetCategory.ECONOMICAL -> amount in 500.0..700.0
            BudgetCategory.AVERAGE -> amount in 700.0..1000.0
            BudgetCategory.ENJOYING -> amount in 1000.0..1800.0
        }

        if (!isValidRange) {
            val range = when (category) {
                BudgetCategory.ECONOMICAL -> "K500 - K700"
                BudgetCategory.AVERAGE -> "K700 - K1000"
                BudgetCategory.ENJOYING -> "K1000 - K1800"
            }
            _uiState.value = _uiState.value.copy(error = "Amount for ${category.name} must be between $range")
            return
        }

        // Filter commodities for selected category
        val availableCommodities = _uiState.value.commodities.filter { it.category == category }
        
        // Generation Logic: 
        // 1. Calculate base costs for household size
        // 2. Adjust quantities if total exceeds user amount
        val budgetItems = mutableListOf<BudgetItem>()
        var currentTotal = 0.0
        
        availableCommodities.forEach { commodity ->
            val quantity = commodity.baseQuantityPerPerson * size
            val cost = quantity * commodity.unitPrice
            budgetItems.add(BudgetItem(commodity.name, quantity, commodity.unit, cost))
            currentTotal += cost
        }

        // Scaling factor if we are over budget
        if (currentTotal > amount) {
            val factor = amount / currentTotal
            val scaledItems = budgetItems.map { item ->
                val newQty = item.quantity * factor
                item.copy(quantity = newQty, totalCost = newQty * (item.totalCost / item.quantity))
            }
            _uiState.value = _uiState.value.copy(
                generatedBudget = MonthlyBudget(amount, category, size, scaledItems)
            )
        } else {
            _uiState.value = _uiState.value.copy(
                generatedBudget = MonthlyBudget(currentTotal, category, size, budgetItems)
            )
        }
    }

    fun updatePrice(id: String, newPrice: Double) {
        viewModelScope.launch {
            budgetRepository.updateCommodityPrice(id, newPrice)
        }
    }
}
