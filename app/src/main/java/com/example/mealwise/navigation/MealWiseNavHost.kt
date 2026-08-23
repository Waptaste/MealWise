package com.example.mealwise.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mealwise.ui.auth.AuthViewModel
import com.example.mealwise.ui.auth.LoginScreen
import com.example.mealwise.ui.auth.RegisterScreen
import com.example.mealwise.ui.budget.BudgetScreen
import com.example.mealwise.ui.budget.BudgetViewModel
import com.example.mealwise.ui.budget.ManagePricesScreen
import com.example.mealwise.ui.home.HomeScreen
import com.example.mealwise.ui.nutrition.NutritionScreen
import com.example.mealwise.ui.nutrition.NutritionViewModel
import com.example.mealwise.ui.onboarding.OnboardingScreen
import com.example.mealwise.ui.onboarding.OnboardingViewModel
import com.example.mealwise.ui.planner.MealPlannerScreen
import com.example.mealwise.ui.planner.MealPlannerViewModel
import com.example.mealwise.ui.recipes.RecipeDetailScreen
import com.example.mealwise.ui.recipes.RecipeFeedScreen
import com.example.mealwise.ui.recipes.RecipeViewModel
import com.example.mealwise.ui.shopping.ShoppingListScreen
import com.example.mealwise.ui.splash.SplashScreen

@Composable
fun MealWiseNavHost() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = AppDestination.Splash.route
    ) {
        composable(AppDestination.Splash.route) {
            SplashScreen(
                viewModel = authViewModel,
                onNavigateToLogin = {
                    navController.navigate(AppDestination.Login.route) {
                        popUpTo(AppDestination.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    val profile = authViewModel.uiState.value.authenticatedProfile
                    if (profile?.onboardingCompleted == true) {
                        navController.navigate("main") {
                            popUpTo(AppDestination.Splash.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(AppDestination.Onboarding.route) {
                            popUpTo(AppDestination.Splash.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(AppDestination.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = {
                    navController.navigate(AppDestination.Register.route)
                },
                onNavigateToHome = {
                    val profile = authViewModel.uiState.value.authenticatedProfile
                    if (profile?.onboardingCompleted == true) {
                        navController.navigate("main") {
                            popUpTo(AppDestination.Login.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(AppDestination.Onboarding.route) {
                            popUpTo(AppDestination.Login.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(AppDestination.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onNavigateToHome = {
                    navController.navigate(AppDestination.Onboarding.route) {
                        popUpTo(AppDestination.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(AppDestination.Onboarding.route) {
            val onboardingViewModel: OnboardingViewModel = hiltViewModel()
            OnboardingScreen(
                viewModel = onboardingViewModel,
                onComplete = {
                    navController.navigate("main") {
                        popUpTo(AppDestination.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // Nested Navigation Graph for main app area to share ViewModels
        navigation(startDestination = AppDestination.Home.route, route = "main") {
            composable(AppDestination.Home.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("main")
                }
                
                HomeScreen(
                    viewModel = authViewModel,
                    onNavigateToLogin = {
                        navController.navigate(AppDestination.Login.route) {
                            popUpTo("main") { inclusive = true }
                        }
                    },
                    onNavigateToRecipes = {
                        navController.navigate(AppDestination.RecipeFeed.route)
                    },
                    onNavigateToPlanner = {
                        navController.navigate(AppDestination.MealPlanner.route)
                    },
                    onNavigateToShopping = {
                        navController.navigate(AppDestination.ShoppingList.route)
                    },
                    onNavigateToNutrition = {
                        navController.navigate(AppDestination.NutritionOverview.route)
                    },
                    onNavigateToBudget = {
                        navController.navigate(AppDestination.BudgetPlanner.route)
                    }
                )
            }

            composable(AppDestination.RecipeFeed.route) {
                val recipeViewModel: RecipeViewModel = hiltViewModel()
                RecipeFeedScreen(
                    viewModel = recipeViewModel,
                    onRecipeClick = { recipeId ->
                        navController.navigate(AppDestination.RecipeDetail(recipeId).route)
                    }
                )
            }

            composable(
                route = AppDestination.RecipeDetail.ROUTE,
                arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
            ) { backStackEntry ->
                val recipeId = backStackEntry.arguments?.getString("recipeId") ?: return@composable
                val recipeViewModel: RecipeViewModel = hiltViewModel()
                RecipeDetailScreen(
                    recipeId = recipeId,
                    viewModel = recipeViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(AppDestination.MealPlanner.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("main")
                }
                val plannerViewModel: MealPlannerViewModel = hiltViewModel(parentEntry)
                
                MealPlannerScreen(
                    viewModel = plannerViewModel,
                    onNavigateToShopping = {
                        navController.navigate(AppDestination.ShoppingList.route)
                    }
                )
            }

            composable(AppDestination.ShoppingList.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("main")
                }
                val plannerViewModel: MealPlannerViewModel = hiltViewModel(parentEntry)
                
                ShoppingListScreen(
                    viewModel = plannerViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(AppDestination.NutritionOverview.route) {
                val nutritionViewModel: NutritionViewModel = hiltViewModel()
                NutritionScreen(
                    viewModel = nutritionViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(AppDestination.BudgetPlanner.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("main")
                }
                val budgetViewModel: BudgetViewModel = hiltViewModel(parentEntry)
                
                BudgetScreen(
                    viewModel = budgetViewModel,
                    onNavigateToManagePrices = {
                        navController.navigate(AppDestination.ManagePrices.route)
                    }
                )
            }

            composable(AppDestination.ManagePrices.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("main")
                }
                val budgetViewModel: BudgetViewModel = hiltViewModel(parentEntry)

                ManagePricesScreen(
                    viewModel = budgetViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
