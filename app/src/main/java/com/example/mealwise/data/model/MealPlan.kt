package com.example.mealwise.data.model

data class MealPlanEntry(
    val id: String = "",
    val userId: String = "",
    val recipeId: String = "",
    val recipeTitle: String = "",
    val date: Long = 0L,
    val mealType: String = "" // Stored as String for better Firestore compatibility (e.g., "BREAKFAST")
)

enum class MealType {
    BREAKFAST, LUNCH, DINNER
}

data class ShoppingItem(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val isChecked: Boolean = false,
    val sourceRecipeId: String? = null
)
