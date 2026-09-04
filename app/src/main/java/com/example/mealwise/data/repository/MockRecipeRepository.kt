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
            imageUrl = "https://www.worldfoodtravel.org/wp-content/uploads/2020/04/Zambia-Nshima.jpg",
            ingredients = listOf("Maize meal", "Water", "Pumpkin leaves or Spinach", "Groundnuts (Peanuts)", "Onions", "Tomatoes"),
            instructions = listOf(
                "Prepare nshima by boiling water and gradually adding maize meal while stirring.",
                "For ifisashi, boil the greens with onions and tomatoes.",
                "Stir in the pounded groundnuts and simmer until thick and creamy."
            ),
            dietaryTags = listOf("Vegetarian", "Lunch", "Dinner"),
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
            imageUrl = "https://zambiankitchen.com/wp-content/uploads/2016/11/Village-Chicken.jpg",
            ingredients = listOf("Village chicken", "Onions", "Tomatoes", "Vegetable oil", "Salt"),
            instructions = listOf(
                "Clean and cut the chicken into pieces.",
                "Boil the chicken with salt until tender (village chicken takes longer).",
                "Fry onions and tomatoes, then add the chicken and simmer."
            ),
            dietaryTags = listOf("High Protein", "Lunch", "Dinner"),
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
            imageUrl = "https://c8.alamy.com/comp/2B0B0N6/chikanda-is-a-zambian-dish-made-from-wild-orchid-tubers-2B0B0N6.jpg",
            ingredients = listOf("Chikanda tubers (pounded)", "Groundnuts", "Soda bicarbonate", "Chilli powder", "Salt"),
            instructions = listOf(
                "Mix groundnuts and chikanda powder with water to form a paste.",
                "Add salt, chilli, and a bit of soda.",
                "Boil the mixture while stirring continuously until it sets like a cake."
            ),
            dietaryTags = listOf("Vegan", "Starter", "Snack"),
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
            title = "Fried Tilapia",
            description = "Fresh fish from the Zambezi or Kariba, seasoned and fried until crispy.",
            imageUrl = "https://i.pinimg.com/736x/8f/3e/26/8f3e264560b37805179a9629b3528b18.jpg",
            ingredients = listOf("Whole Tilapia", "Flour", "Salt", "Lemon", "Oil for frying"),
            instructions = listOf(
                "Clean the fish and make diagonal cuts on the sides.",
                "Season with salt and lemon juice.",
                "Lightly dust with flour and fry in hot oil until golden brown."
            ),
            dietaryTags = listOf("High Protein", "Lunch", "Dinner"),
            prepTimeMinutes = 10,
            cookTimeMinutes = 20,
            calories = 400,
            proteinGrams = 38,
            carbsGrams = 8,
            fatsGrams = 14,
            servings = 2
        ),
        Recipe(
            id = "5",
            title = "Munkoyo Beverage",
            description = "A traditional Zambian fermented beverage made from maize meal and munkoyo roots.",
            imageUrl = "https://i0.wp.com/zambiankitchen.com/wp-content/uploads/2017/09/Munkoyo.jpg",
            ingredients = listOf("Maize meal", "Munkoyo roots", "Water", "Sugar (optional)"),
            instructions = listOf(
                "Cook a thin maize porridge and let it cool.",
                "Add crushed munkoyo roots to the porridge.",
                "Allow to ferment for 24-48 hours until desired tanginess is achieved."
            ),
            dietaryTags = listOf("Beverage", "Traditional"),
            prepTimeMinutes = 30,
            cookTimeMinutes = 20,
            calories = 150,
            proteinGrams = 2,
            carbsGrams = 35,
            fatsGrams = 1,
            servings = 10
        ),
        Recipe(
            id = "6",
            title = "Zambian Fruit Salad",
            description = "A refreshing mix of seasonal Zambian fruits like mango, guava, and papaya.",
            imageUrl = "https://i.pinimg.com/originals/8a/c5/4b/8ac54b8575084976723b72c237c86518.jpg",
            ingredients = listOf("Mango", "Guava", "Papaya", "Banana", "Lemon juice"),
            instructions = listOf(
                "Peel and dice all the fruits into bite-sized pieces.",
                "Mix in a large bowl and drizzle with lemon juice to prevent browning.",
                "Chill before serving."
            ),
            dietaryTags = listOf("Dessert", "Healthy Eating"),
            prepTimeMinutes = 15,
            cookTimeMinutes = 0,
            calories = 120,
            proteinGrams = 1,
            carbsGrams = 28,
            fatsGrams = 0,
            servings = 4
        ),
        Recipe(
            id = "7",
            title = "Sweet Potato Porridge",
            description = "A warm and filling breakfast dish made from mashed sweet potatoes and milk.",
            imageUrl = "https://i.ytimg.com/vi/wO6pL3v7W9g/maxresdefault.jpg",
            ingredients = listOf("Sweet potatoes", "Milk or Water", "Salt", "Butter or Peanut butter (optional)"),
            instructions = listOf(
                "Boil sweet potatoes until very soft.",
                "Mash the potatoes and stir in milk until a porridge consistency is reached.",
                "Serve hot with a dollop of peanut butter."
            ),
            dietaryTags = listOf("Breakfast", "Vegetarian"),
            prepTimeMinutes = 5,
            cookTimeMinutes = 20,
            calories = 350,
            proteinGrams = 8,
            carbsGrams = 60,
            fatsGrams = 10,
            servings = 2
        ),
        Recipe(
            id = "8",
            title = "Sample Breakfast Eggs",
            description = "Simple fried or scrambled eggs with a Zambian tomato and onion garnish.",
            imageUrl = "https://zambiankitchen.com/wp-content/uploads/2016/09/Breakfast-Eggs.jpg",
            ingredients = listOf("Eggs", "Onions", "Tomatoes", "Salt", "Oil"),
            instructions = listOf(
                "Fry onions and tomatoes until soft.",
                "Add eggs and scramble or fry as desired.",
                "Season with salt."
            ),
            dietaryTags = listOf("Breakfast", "High Protein"),
            prepTimeMinutes = 5,
            cookTimeMinutes = 10,
            calories = 220,
            proteinGrams = 14,
            carbsGrams = 4,
            fatsGrams = 16,
            servings = 1
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
            recipe.dietaryTags.any { tag -> tags.any { it.equals(tag, ignoreCase = true) } }
        }
        return Result.success(filtered)
    }


    override suspend fun addRecipe(recipe: Recipe, userId: String?): Result<Unit> {
        return Result.success(Unit) // Static mock
    }
}
