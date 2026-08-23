package com.example.mealwise.data.repository

import android.util.Log
import com.example.mealwise.data.api.HdxApiService
import com.example.mealwise.data.model.BudgetCategory
import com.example.mealwise.data.model.Commodity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseBudgetRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val hdxApiService: HdxApiService
) : BudgetRepository {

    private val collection = firestore.collection("commodities")

    override fun getCommodities(): Flow<List<Commodity>> {
        return collection.snapshots().map { it.toObjects<Commodity>() }
    }

    override suspend fun updateCommodityPrice(id: String, newPrice: Double): Result<Unit> {
        return try {
            collection.document(id).update("unitPrice", newPrice).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateCommodityMustHave(id: String, isMustHave: Boolean): Result<Unit> {
        return try {
            collection.document(id).update("isMustHave", isMustHave).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateCommodityStapleStatus(id: String, isStaple: Boolean): Result<Unit> {
        return try {
            collection.document(id).update("isStaple", isStaple).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addCommodity(commodity: Commodity): Result<Unit> {
        return try {
            val docId = commodity.id.ifBlank { collection.document().id }
            collection.document(docId).set(commodity.copy(id = docId)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCommodity(id: String): Result<Unit> {
        return try {
            collection.document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun initializeDefaultCommodities(): Result<Unit> {
        return try {
            val freshPrices = fetchHdxPrices()
            
            val defaults = listOf(
                // ECONOMICAL
                Commodity("1", "Roller Mealie Meal", freshPrices["Maize meal (roller)"] ?: 190.0, "25kg bag", BudgetCategory.ECONOMICAL, 0.5, isMustHave = true, isStaple = true),
                Commodity("2", "Dry Beans", freshPrices["Beans (dried)"] ?: 25.0, "kg", BudgetCategory.ECONOMICAL, 2.0, isStaple = true),
                Commodity("3", "Cabbage", freshPrices["Cabbage"] ?: 15.0, "Head", BudgetCategory.ECONOMICAL, 4.0),
                Commodity("4", "Small Kapenta", freshPrices["Kapenta"] ?: 80.0, "kg", BudgetCategory.ECONOMICAL, 1.0),
                
                // AVERAGE
                Commodity("5", "Breakfast Mealie Meal", freshPrices["Maize meal (breakfast)"] ?: 230.0, "25kg bag", BudgetCategory.AVERAGE, 0.5, isMustHave = true, isStaple = true),
                Commodity("6", "Mixed Vegetables", 40.0, "kg", BudgetCategory.AVERAGE, 3.0),
                Commodity("7", "Soy Pieces", 20.0, "500g pack", BudgetCategory.AVERAGE, 4.0),
                Commodity("8", "Eggs", freshPrices["Eggs"] ?: 65.0, "Tray (30)", BudgetCategory.AVERAGE, 1.0),
                Commodity("9", "Cooking Oil", freshPrices["Oil (vegetable)"] ?: 85.0, "2L", BudgetCategory.AVERAGE, 1.0, isStaple = true),

                // ENJOYING
                Commodity("10", "Choice Mealie Meal", 260.0, "25kg bag", BudgetCategory.ENJOYING, 0.5, isMustHave = true, isStaple = true),
                Commodity("11", "Beef (Standard)", freshPrices["Meat (beef)"] ?: 95.0, "kg", BudgetCategory.ENJOYING, 3.0),
                Commodity("12", "Fresh Tilapia", freshPrices["Fish (fresh)"] ?: 75.0, "kg", BudgetCategory.ENJOYING, 4.0),
                Commodity("13", "Chicken (Broiler)", freshPrices["Meat (chicken)"] ?: 110.0, "Whole", BudgetCategory.ENJOYING, 2.0),
                Commodity("14", "Basmati Rice", freshPrices["Rice (basmati)"] ?: 140.0, "5kg bag", BudgetCategory.ENJOYING, 0.5, isStaple = true),
                Commodity("15", "Assorted Fruits", 150.0, "Monthly Supply", BudgetCategory.ENJOYING, 1.0)
            )

            val batch = firestore.batch()
            defaults.forEach { commodity ->
                batch.set(collection.document(commodity.id), commodity)
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("BudgetRepo", "Failed to init commodities", e)
            Result.failure(e)
        }
    }

    private suspend fun fetchHdxPrices(): Map<String, Double> {
        return try {
            val response = hdxApiService.getFoodPrices()
            response.data
                .sortedByDescending { it.date }
                .distinctBy { it.commodityName }
                .associate { it.commodityName to it.price }
        } catch (e: Exception) {
            Log.e("BudgetRepo", "HAPI Fetch failed, using hardcoded defaults", e)
            emptyMap()
        }
    }
}
