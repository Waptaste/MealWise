package com.example.mealwise.data.model

enum class BudgetCategory {
    ECONOMICAL, AVERAGE, ENJOYING
}

data class Commodity(
    val id: String = "",
    val name: String = "",
    val unitPrice: Double = 0.0,
    val unit: String = "", // e.g., "25kg bag", "kg", "Tray (30)"
    val category: BudgetCategory = BudgetCategory.AVERAGE,
    val baseQuantityPerPerson: Double = 1.0 // Per month
)

data class BudgetItem(
    val commodityName: String = "",
    val quantity: Double = 0.0,
    val unit: String = "",
    val totalCost: Double = 0.0
)

data class MonthlyBudget(
    val totalAmount: Double = 0.0,
    val category: BudgetCategory = BudgetCategory.AVERAGE,
    val householdSize: Int = 1,
    val items: List<BudgetItem> = emptyList()
)
