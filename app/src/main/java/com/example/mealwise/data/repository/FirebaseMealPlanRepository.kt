package com.example.mealwise.data.repository

import com.example.mealwise.data.model.MealPlanEntry
import com.example.mealwise.data.model.ShoppingItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseMealPlanRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : MealPlanRepository {

    override suspend fun addMealPlanEntry(entry: MealPlanEntry): Result<String> {
        return try {
            val docRef = firestore.collection("users")
                .document(entry.userId)
                .collection("meal_plans")
                .document()
            
            val entryWithId = entry.copy(id = docRef.id)
            docRef.set(entryWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeMealPlanEntry(id: String): Result<Unit> {
        return Result.failure(NotImplementedError("Remove requires specific path"))
    }
    
    suspend fun removeMealPlanEntry(userId: String, entryId: String): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .collection("meal_plans")
                .document(entryId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getMealPlanEntries(userId: String): Flow<List<MealPlanEntry>> {
        return firestore.collection("users")
            .document(userId)
            .collection("meal_plans")
            .snapshots()
            .map { it.toObjects<MealPlanEntry>() }
    }

    override fun getShoppingListFlow(userId: String): Flow<List<ShoppingItem>> {
        return firestore.collection("users")
            .document(userId)
            .collection("shopping_list")
            .snapshots()
            .map { it.toObjects<ShoppingItem>() }
    }

    override suspend fun updateShoppingItem(item: ShoppingItem): Result<Unit> {
        return try {
            // Use the actual unique ID, not the name
            val docId = item.id.ifBlank { firestore.collection("users").document(item.userId).collection("shopping_list").document().id }
            firestore.collection("users")
                .document(item.userId)
                .collection("shopping_list")
                .document(docId)
                .set(item.copy(id = docId))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getShoppingList(userId: String): Result<List<ShoppingItem>> {
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("shopping_list")
                .get()
                .await()
            Result.success(snapshot.toObjects<ShoppingItem>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveShoppingList(userId: String, items: List<ShoppingItem>): Result<Unit> {
        return try {
            val batch = firestore.batch()
            val collection = firestore.collection("users").document(userId).collection("shopping_list")
            
            items.forEach { item ->
                // Generate unique ID if not present
                val docId = item.id.ifBlank { collection.document().id }
                val itemToSave = item.copy(id = docId, userId = userId)
                batch.set(collection.document(docId), itemToSave)
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteOldShoppingItem(userId: String, docId: String): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .collection("shopping_list")
                .document(docId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
