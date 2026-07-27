package com.example.mealwise.data.repository

import com.example.mealwise.data.model.Recipe
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockRecipeRepository @Inject constructor() : RecipeRepository {

    private val recipes = listOf(
        Recipe(
            id = "1",
            title = "Nshima with Ifisashi",
            description = "A staple Zambian dish featuring thick maize porridge served with a delicious peanut-based green vegetable relish.",
            imageUrl = "https://example.com/nshima_ifisashi.jpg",
            ingredients = listOf("Maize meal", "Water", "Pumpkin leaves or Spinach", "Groundnuts (Peanuts)", "Onions", "Tomatoes"),
            instructions = listOf(
                "Prepare nshima by boiling water and gradually adding maize meal while stirring.",
                "For ifisashi, boil the greens with onions and tomatoes.",
                "Stir in the pounded groundnuts and simmer until thick and creamy."
            ),
            dietaryTags = listOf("Vegetarian", "Gluten-Free", "Healthy Eating"),
            prepTimeMinutes = 10,
            cookTimeMinutes = 30,
            calories = 450,
            proteinGrams = 15,
            carbsGrams = 65,
            fatsGrams = 18,
            servings = 4
        ),
        Recipe(
            id = "2",
            title = "Village Chicken Stew",
            description = "Traditional Zambian free-range chicken stewed to perfection with local flavors.",
            imageUrl = "https://example.com/village_chicken.jpg",
            ingredients = listOf("Village chicken", "Onions", "Tomatoes", "Vegetable oil", "Salt"),
            instructions = listOf(
                "Clean and cut the chicken into pieces.",
                "Boil the chicken with salt until tender (village chicken takes longer).",
                "Fry onions and tomatoes, then add the chicken and simmer."
            ),
            dietaryTags = listOf("High Protein", "Paleo"),
            prepTimeMinutes = 15,
            cookTimeMinutes = 90,
            calories = 550,
            proteinGrams = 45,
            carbsGrams = 12,
            fatsGrams = 28,
            servings = 6
        ),
        Recipe(
            id = "3",
            title = "Chikanda (African Polony)",
            description = "A unique Zambian savory snack made from wild orchid tubers and peanuts.",
            imageUrl = "https://example.com/chikanda.jpg",
            ingredients = listOf("Chikanda tubers (pounded)", "Groundnuts", "Soda bicarbonate", "Chilli powder", "Salt"),
            instructions = listOf(
                "Mix groundnuts and chikanda powder with water to form a paste.",
                "Add salt, chilli, and a bit of soda.",
                "Boil the mixture while stirring continuously until it sets like a cake."
            ),
            dietaryTags = listOf("Vegan", "Vegetarian", "Gluten-Free"),
            prepTimeMinutes = 20,
            cookTimeMinutes = 40,
            calories = 300,
            proteinGrams = 12,
            carbsGrams = 40,
            fatsGrams = 15,
            servings = 8
        ),
        Recipe(
            id = "4",
            title = "Fried Tilapia with Kariba Bream",
            description = "Fresh fish from the Zambezi or Kariba, seasoned and fried until crispy.",
            imageUrl = "https://example.com/fried_tilapia.jpg",
            ingredients = listOf("Whole Tilapia", "Flour", "Salt", "Lemon", "Oil for frying"),
            instructions = listOf(
                "Clean the fish and make diagonal cuts on the sides.",
                "Season with salt and lemon juice.",
                "Lightly dust with flour and fry in hot oil until golden brown."
            ),
            dietaryTags = listOf("High Protein", "Gluten-Free"),
            prepTimeMinutes = 10,
            cookTimeMinutes = 20,
            calories = 400,
            proteinGrams = 38,
            carbsGrams = 8,
            fatsGrams = 14,
            servings = 2
        )
    )

    override suspend fun getRecipes(): Result<List<Recipe>> {
        return Result.success(recipes)
    }

    override suspend fun getRecipeById(id: String): Result<Recipe?> {
        return Result.success(recipes.find { it.id == id })
    }

    override suspend fun getRecipesByTags(tags: List<String>): Result<List<Recipe>> {
        if (tags.isEmpty()) return Result.success(recipes)
        val filtered = recipes.filter { recipe ->
            recipe.dietaryTags.any { tag -> tags.contains(tag) }
        }
        return Result.success(filtered)
    }
}
