package com.example.mealwise.data.model

data class MealPlanEntry(
    val id: String = "",
    val userId: String = "",
    val recipeId: String = "",
    val recipeTitle: String = "",
    val date: Long = 0L,
    val mealType: String = "", // e.g., "BREAKFAST", etc.
    val portionSize: Double = 1.0 // 0.5 for small, 1.0 for medium, 1.5 for large
)

enum class MealType {
    STARTER, BREAKFAST, LUNCH, DINNER, DESSERT, BEVERAGE
}

data class ShoppingItem(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val isChecked: Boolean = false,
    val sourceRecipeId: String? = null
)
