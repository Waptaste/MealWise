package com.example.mealwise

import com.example.mealwise.data.model.MealPlanEntry
import com.example.mealwise.data.model.MealType
import com.example.mealwise.data.model.ShoppingItem
import com.example.mealwise.data.model.UserProfile
import com.example.mealwise.data.model.Recipe
import com.example.mealwise.data.repository.AuthRepository
import com.example.mealwise.data.repository.MealPlanRepository
import com.example.mealwise.data.repository.RecipeRepository
import com.example.mealwise.ui.planner.MealPlannerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ShoppingListBugReproductionTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: MealPlannerViewModel
    private lateinit var fakeMealPlanRepository: ReproFakeMealPlanRepository
    private lateinit var fakeRecipeRepository: ReproFakeRecipeRepository
    private lateinit var fakeAuthRepository: ReproFakeAuthRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeMealPlanRepository = ReproFakeMealPlanRepository()
        fakeRecipeRepository = ReproFakeRecipeRepository()
        fakeAuthRepository = ReproFakeAuthRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun initViewModel() {
        viewModel = MealPlannerViewModel(fakeMealPlanRepository, fakeRecipeRepository, fakeAuthRepository)
    }

    @Test
    fun `reproduce repeating items bug`() = runTest {
        fakeAuthRepository.authenticated = true
        initViewModel()
        advanceUntilIdle()

        // Simulate adding a meal that adds "Keto Item"
        viewModel.addMeal("1", MealType.LUNCH)
        advanceUntilIdle()
        
        assertEquals(1, viewModel.uiState.value.shoppingList.size)
        assertEquals("Keto Item", viewModel.uiState.value.shoppingList[0].name)

        // Mock the case where an item already exists with a different ID but same name
        fakeMealPlanRepository.shoppingItems.clear()
        fakeMealPlanRepository.shoppingItems.add(ShoppingItem(id = "old_id", name = "Keto Item", userId = "123"))
        fakeMealPlanRepository.triggerUpdate()
        advanceUntilIdle()
        
        // ensureIngredientsSync should NOT add it again because it checks by name
        assertEquals(1, viewModel.uiState.value.shoppingList.size)
    }

    @Test
    fun `reproduce uncrossing bug`() = runTest {
        fakeAuthRepository.authenticated = true
        fakeMealPlanRepository.shoppingItems.add(ShoppingItem(id = "item1", name = "Salt", isChecked = false, userId = "123"))
        fakeMealPlanRepository.triggerUpdate()
        initViewModel()
        advanceUntilIdle()

        val item = viewModel.uiState.value.shoppingList[0]
        assertEquals("Salt", item.name)
        assertFalse(item.isChecked)

        // Toggle
        viewModel.toggleShoppingItem(item.id)
        advanceUntilIdle()
        
        // UI should be checked (optimistic)
        assertTrue(viewModel.uiState.value.shoppingList[0].isChecked)
        
        // Simulate a delay where Firestore flow hasn't updated yet, but something else triggers collect
        fakeMealPlanRepository.triggerUpdate()
        advanceUntilIdle()
        
        // After repo update and flow collect, it should still be checked
        assertTrue(viewModel.uiState.value.shoppingList[0].isChecked)
    }
}

class ReproFakeMealPlanRepository : MealPlanRepository {
    val mealPlans = mutableListOf<MealPlanEntry>()
    val shoppingItems = mutableListOf<ShoppingItem>()
    private val _mealPlanFlow = MutableStateFlow<List<MealPlanEntry>>(emptyList())
    private val _shoppingFlow = MutableStateFlow<List<ShoppingItem>>(emptyList())

    fun triggerUpdate() {
        _mealPlanFlow.value = mealPlans.toList()
        _shoppingFlow.value = shoppingItems.toList()
    }

    override suspend fun addMealPlanEntry(entry: MealPlanEntry): Result<String> {
        val entryWithId = if (entry.id.isBlank()) entry.copy(id = "plan_${mealPlans.size}") else entry
        mealPlans.add(entryWithId)
        triggerUpdate()
        return Result.success(entryWithId.id)
    }

    override suspend fun removeMealPlanEntry(id: String): Result<Unit> {
        mealPlans.removeIf { it.id == id }
        triggerUpdate()
        return Result.success(Unit)
    }

    override fun getMealPlanEntries(userId: String): Flow<List<MealPlanEntry>> = _mealPlanFlow

    override fun getShoppingListFlow(userId: String): Flow<List<ShoppingItem>> = _shoppingFlow

    override suspend fun updateShoppingItem(item: ShoppingItem): Result<Unit> {
        shoppingItems.removeIf { it.id == item.id || it.name == item.name }
        shoppingItems.add(item)
        triggerUpdate()
        return Result.success(Unit)
    }

    override suspend fun getShoppingList(userId: String): Result<List<ShoppingItem>> = Result.success(shoppingItems.toList())

    override suspend fun saveShoppingList(userId: String, items: List<ShoppingItem>): Result<Unit> {
        items.forEach { newItem ->
            if (!shoppingItems.any { it.name == newItem.name }) {
                shoppingItems.add(newItem)
            }
        }
        triggerUpdate()
        return Result.success(Unit)
    }
}

class ReproFakeAuthRepository : AuthRepository {
    var authenticated = false
    val profiles = mutableMapOf<String, UserProfile>()
    val currentUserUid = "123"

    override suspend fun register(name: String, email: String, password: String): Result<UserProfile> {
        authenticated = true
        val profile = UserProfile(uid = currentUserUid, name = name, email = email, onboardingCompleted = false)
        profiles[currentUserUid] = profile
        return Result.success(profile)
    }
    
    override suspend fun login(email: String, password: String): Result<UserProfile> {
        authenticated = true
        val profile = UserProfile(uid = currentUserUid, name = "John Doe", email = email, onboardingCompleted = true)
        profiles[currentUserUid] = profile
        return Result.success(profile)
    }
    
    override suspend fun getCurrentUserProfile(): Result<UserProfile?> {
        return if (authenticated) {
            Result.success(profiles[currentUserUid] ?: UserProfile(uid = currentUserUid, name = "John Doe", email = "john@example.com", onboardingCompleted = true))
        } else {
            Result.success(null)
        }
    }

    override suspend fun updateUserProfile(profile: UserProfile): Result<Unit> {
        profiles[profile.uid] = profile
        return Result.success(Unit)
    }
    
    override fun isUserAuthenticated() = authenticated
    override fun logout() { authenticated = false }
    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> = Result.success(Unit)
    override suspend fun sendEmailVerification(): Result<Unit> = Result.success(Unit)
    override fun isEmailVerified(): Boolean = true
}

class ReproFakeRecipeRepository : RecipeRepository {
    private val recipes = listOf(
        Recipe(id = "1", title = "Recipe 1", dietaryTags = listOf("Keto"), ingredients = listOf("Keto Item"), calories = 450),
        Recipe(id = "2", title = "Recipe 2", dietaryTags = listOf("Vegan"), ingredients = listOf("Vegan Item"), calories = 550)
    )

    override suspend fun getRecipes(): Result<List<Recipe>> = Result.success(recipes)
    override suspend fun getRecipeById(id: String): Result<Recipe?> = Result.success(recipes.find { it.id == id })
    override suspend fun getRecipesByTags(tags: List<String>): Result<List<Recipe>> {
        val filtered = recipes.filter { r -> r.dietaryTags.any { t -> tags.contains(t) } }
        return Result.success(filtered)
    }
    override suspend fun addRecipe(recipe: Recipe, userId: String?): Result<Unit> = Result.success(Unit)
}

