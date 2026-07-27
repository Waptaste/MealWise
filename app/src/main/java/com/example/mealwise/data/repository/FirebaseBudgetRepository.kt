package com.example.mealwise.data.repository

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
    private val firestore: FirebaseFirestore
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

    override suspend fun initializeDefaultCommodities(): Result<Unit> {
        return try {
            val defaults = listOf(
                // ECONOMICAL
                Commodity("1", "Roller Mealie Meal", 190.0, "25kg bag", BudgetCategory.ECONOMICAL, 0.5),
                Commodity("2", "Dry Beans", 25.0, "kg", BudgetCategory.ECONOMICAL, 2.0),
                Commodity("3", "Cabbage", 15.0, "Head", BudgetCategory.ECONOMICAL, 4.0),
                Commodity("4", "Small Kapenta", 80.0, "kg", BudgetCategory.ECONOMICAL, 1.0),
                
                // AVERAGE
                Commodity("5", "Breakfast Mealie Meal", 230.0, "25kg bag", BudgetCategory.AVERAGE, 0.5),
                Commodity("6", "Mixed Vegetables", 40.0, "kg", BudgetCategory.AVERAGE, 3.0),
                Commodity("7", "Soy Pieces", 20.0, "500g pack", BudgetCategory.AVERAGE, 4.0),
                Commodity("8", "Eggs", 65.0, "Tray (30)", BudgetCategory.AVERAGE, 1.0),
                Commodity("9", "Cooking Oil", 85.0, "2L", BudgetCategory.AVERAGE, 1.0),

                // ENJOYING
                Commodity("10", "Choice Mealie Meal", 260.0, "25kg bag", BudgetCategory.ENJOYING, 0.5),
                Commodity("11", "Beef (Standard)", 95.0, "kg", BudgetCategory.ENJOYING, 3.0),
                Commodity("12", "Fresh Tilapia", 75.0, "kg", BudgetCategory.ENJOYING, 4.0),
                Commodity("13", "Chicken (Broiler)", 110.0, "Whole", BudgetCategory.ENJOYING, 2.0),
                Commodity("14", "Basmati Rice", 140.0, "5kg bag", BudgetCategory.ENJOYING, 0.5),
                Commodity("15", "Assorted Fruits", 150.0, "Monthly Supply", BudgetCategory.ENJOYING, 1.0)
            )

            val batch = firestore.batch()
            defaults.forEach { commodity ->
                batch.set(collection.document(commodity.id), commodity)
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
