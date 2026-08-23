package com.example.mealwise.data.repository

import com.example.mealwise.data.model.Commodity
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun getCommodities(): Flow<List<Commodity>>
    suspend fun updateCommodityPrice(id: String, newPrice: Double): Result<Unit>
    suspend fun updateCommodityMustHave(id: String, isMustHave: Boolean): Result<Unit>
    suspend fun updateCommodityStapleStatus(id: String, isStaple: Boolean): Result<Unit>
    suspend fun addCommodity(commodity: Commodity): Result<Unit>
    suspend fun deleteCommodity(id: String): Result<Unit>
    suspend fun initializeDefaultCommodities(): Result<Unit>
}
