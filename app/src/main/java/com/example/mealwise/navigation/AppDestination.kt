package com.example.mealwise.navigation

sealed class AppDestination(val route: String) {
    object Splash : AppDestination("splash")
    object Login : AppDestination("login")
    object Register : AppDestination("register")
    object ForgotPassword : AppDestination("forgot_password")
    object Onboarding : AppDestination("onboarding")
    object Home : AppDestination("home")
    object RecipeFeed : AppDestination("recipe_feed")
    object MealPlanner : AppDestination("meal_planner")
    object ShoppingList : AppDestination("shopping_list")
    object NutritionOverview : AppDestination("nutrition_overview")
    object BudgetPlanner : AppDestination("budget_planner")
    object ManagePrices : AppDestination("manage_prices")
    object Profile : AppDestination("profile")
    data class RecipeDetail(val recipeId: String) : AppDestination("recipe_detail/$recipeId") {
        companion object {
            const val ROUTE = "recipe_detail/{recipeId}"
        }
    }
}
