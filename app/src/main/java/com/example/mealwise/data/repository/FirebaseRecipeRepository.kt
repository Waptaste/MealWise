package com.example.mealwise.data.repository

import com.example.mealwise.data.model.Recipe
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseRecipeRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val mockRecipeRepository: MockRecipeRepository // Fallback/Static data
) : RecipeRepository {

    private val collection = firestore.collection("recipes")

    override suspend fun getRecipes(): Result<List<Recipe>> {
        return try {
            val snapshot = collection.get().await()
            val globalRecipes = snapshot.toObjects<Recipe>()
            
            // Also fetch user-specific custom recipes
            val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            val customRecipes = if (uid != null) {
                firestore.collection("users")
                    .document(uid)
                    .collection("custom_recipes")
                    .get()
                    .await()
                    .toObjects<Recipe>()
            } else {
                emptyList()
            }
            
            val staticRecipes = mockRecipeRepository.getRecipes().getOrDefault(emptyList())
            Result.success(staticRecipes + globalRecipes + customRecipes)
        } catch (e: Exception) {
            mockRecipeRepository.getRecipes() // Fallback to static if firestore fails
        }
    }

    override suspend fun getRecipeById(id: String): Result<Recipe?> {
        return try {
            val doc = collection.document(id).get().await()
            if (doc.exists()) {
                Result.success(doc.toObject(Recipe::class.java))
            } else {
                mockRecipeRepository.getRecipeById(id)
            }
        } catch (e: Exception) {
            mockRecipeRepository.getRecipeById(id)
        }
    }

    override suspend fun getRecipesByTags(tags: List<String>): Result<List<Recipe>> {
        val all = getRecipes().getOrDefault(emptyList())
        if (tags.isEmpty()) return Result.success(all)
        val filtered = all.filter { recipe ->
            recipe.dietaryTags.any { tag -> tags.any { it.equals(tag, ignoreCase = true) } }
        }
        return Result.success(filtered)
    }

    override suspend fun addRecipe(recipe: Recipe, userId: String?): Result<Unit> {
        return try {
            val targetCollection = if (userId != null) {
                firestore.collection("users").document(userId).collection("custom_recipes")
            } else {
                collection
            }
            
            val docId = recipe.id.ifBlank { targetCollection.document().id }
            targetCollection.document(docId).set(recipe.copy(id = docId)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
