package com.example.mealwise.ui.budget

import com.example.mealwise.data.model.BudgetCategory
import com.example.mealwise.data.model.Commodity
import com.example.mealwise.data.model.MonthlyBudget

data class BudgetUiState(
    val selectedCategory: BudgetCategory = BudgetCategory.AVERAGE,
    val enteredAmount: String = "",
    val householdSize: String = "4",
    val generatedBudget: MonthlyBudget? = null,
    val commodities: List<Commodity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
