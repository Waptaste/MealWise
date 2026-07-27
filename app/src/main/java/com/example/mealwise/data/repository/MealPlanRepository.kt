package com.example.mealwise.data.repository

import com.example.mealwise.data.model.MealPlanEntry
import com.example.mealwise.data.model.ShoppingItem
import kotlinx.coroutines.flow.Flow

interface MealPlanRepository {
    suspend fun addMealPlanEntry(entry: MealPlanEntry): Result<String>
    suspend fun removeMealPlanEntry(id: String): Result<Unit>
    fun getMealPlanEntries(userId: String): Flow<List<MealPlanEntry>>
    
    fun getShoppingListFlow(userId: String): Flow<List<ShoppingItem>>
    suspend fun updateShoppingItem(item: ShoppingItem): Result<Unit>
    suspend fun getShoppingList(userId: String): Result<List<ShoppingItem>>
    suspend fun saveShoppingList(userId: String, items: List<ShoppingItem>): Result<Unit>
}
