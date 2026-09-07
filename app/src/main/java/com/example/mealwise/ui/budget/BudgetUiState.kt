package com.example.mealwise.ui.budget

import com.example.mealwise.data.model.BudgetCategory
import com.example.mealwise.data.model.BudgetItem
import com.example.mealwise.data.model.Commodity
import com.example.mealwise.data.model.MonthlyBudget

data class BudgetUiState(
    val selectedCategory: BudgetCategory = BudgetCategory.AVERAGE,
    val enteredAmount: String = "",
    val householdSize: String = "4",
    val generatedBudget: MonthlyBudget? = null,
    val savedBudgets: List<MonthlyBudget> = emptyList(),
    val commodities: List<Commodity> = emptyList(),
    val wishlist: List<Commodity> = emptyList(), // Items user wants
    val isLoading: Boolean = false,
    val error: String? = null,
    val itemToReplace: String? = null,
    val isWishlistMode: Boolean = true, // Start by building a wishlist
    val wishlistTotal: Double = 0.0
)
