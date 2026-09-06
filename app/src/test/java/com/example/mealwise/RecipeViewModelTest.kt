package com.example.mealwise

import com.example.mealwise.data.model.Recipe
import com.example.mealwise.data.model.UserProfile
import com.example.mealwise.data.repository.RecipeRepository
import com.example.mealwise.ui.recipes.RecipeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecipeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: RecipeViewModel
    private lateinit var fakeRecipeRepository: FakeRecipeRepository
    private lateinit var fakeAuthRepository: FakeAuthRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRecipeRepository = FakeRecipeRepository()
        fakeAuthRepository = FakeAuthRepository()
    }

    private fun initViewModel() {
        viewModel = RecipeViewModel(fakeRecipeRepository, fakeAuthRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadRecipes with no user preferences returns all recipes`() = runTest {
        fakeAuthRepository.authenticated = true
        // No dietary preferences
        
        initViewModel()
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.recipes.size)
        assertEquals("Recipe 1", viewModel.uiState.value.recipes[0].title)
    }

    @Test
    fun `loadRecipes with user preferences returns filtered recipes`() = runTest {
        fakeAuthRepository.authenticated = true
        fakeAuthRepository.profiles[fakeAuthRepository.currentUserUid] = UserProfile(
            uid = fakeAuthRepository.currentUserUid,
            dietaryPreferences = listOf("Vegan")
        )
        
        initViewModel()
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.recipes.size)
        assertEquals("Recipe 2", viewModel.uiState.value.recipes[0].title)
        assertTrue(viewModel.uiState.value.recipes[0].dietaryTags.contains("Vegan"))
    }

    @Test
    fun `addAndImproveRecipe populates ingredients and details`() = runTest {
        fakeAuthRepository.authenticated = true
        initViewModel()
        advanceUntilIdle()

        val rawInput = "2 cups flour\n1kg sugar\nMix them together and bake"
        viewModel.addAndImproveRecipe("New Cake", rawInput)
        
        // Handle delay in simulation
        testDispatcher.scheduler.advanceTimeBy(2100)
        advanceUntilIdle()

        val savedRecipe = fakeRecipeRepository.addedRecipes.last()
        assertEquals("New Cake", savedRecipe.title)
        assertTrue(savedRecipe.ingredients.contains("2 cups flour"))
        assertTrue(savedRecipe.ingredients.contains("1kg sugar"))
        assertTrue(savedRecipe.instructions.contains("Mix them together and bake"))
        assertTrue(savedRecipe.isUserCreated)
        assertTrue(savedRecipe.calories > 0)
    }
}

class FakeRecipeRepository : RecipeRepository {
    private val recipes = mutableListOf(
        Recipe(id = "1", title = "Recipe 1", dietaryTags = listOf("Keto"), ingredients = listOf("Keto Item"), calories = 450),
        Recipe(id = "2", title = "Recipe 2", dietaryTags = listOf("Vegan"), ingredients = listOf("Vegan Item"), calories = 550)
    )
    val addedRecipes = mutableListOf<Recipe>()

    override suspend fun getRecipes(): Result<List<Recipe>> = Result.success(recipes + addedRecipes)

    override suspend fun getRecipeById(id: String): Result<Recipe?> = Result.success(recipes.find { it.id == id })

    override suspend fun getRecipesByTags(tags: List<String>): Result<List<Recipe>> {
        val filtered = (recipes + addedRecipes).filter { r -> r.dietaryTags.any { t -> tags.contains(t) } }
        return Result.success(filtered)
    }

    override suspend fun addRecipe(recipe: Recipe, userId: String?): Result<Unit> {
        addedRecipes.add(recipe)
        return Result.success(Unit)
    }
}
