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
    val baseQuantityPerPerson: Double = 1.0, // Per month
    val isMustHave: Boolean = false, // User preference: prioritize this item
    val isStaple: Boolean = false, // Essential item protection logic
    val isDiscrete: Boolean = false // If true, quantity must be a whole number (e.g. bags, trays)
)

data class BudgetItem(
    val commodityName: String = "",
    val quantity: Double = 0.0,
    val unit: String = "",
    val totalCost: Double = 0.0,
    val isMustHave: Boolean = false,
    val isStaple: Boolean = false,
    val isDiscrete: Boolean = false
)

data class MonthlyBudget(
    val id: String = "",
    val userId: String = "",
    val monthYear: String = "", // e.g., "2024-08"
    val totalAmount: Double = 0.0,
    val category: BudgetCategory = BudgetCategory.AVERAGE,
    val householdSize: Int = 1,
    val items: List<BudgetItem> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)
