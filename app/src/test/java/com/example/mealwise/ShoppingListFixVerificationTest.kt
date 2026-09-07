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
class ShoppingListFixVerificationTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: MealPlannerViewModel
    private lateinit var fakeMealPlanRepository: FixFakeMealPlanRepository
    private lateinit var fakeRecipeRepository: FixFakeRecipeRepository
    private lateinit var fakeAuthRepository: FixFakeAuthRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeMealPlanRepository = FixFakeMealPlanRepository()
        fakeRecipeRepository = FixFakeRecipeRepository()
        fakeAuthRepository = FixFakeAuthRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun initViewModel() {
        viewModel = MealPlannerViewModel(fakeMealPlanRepository, fakeRecipeRepository, fakeAuthRepository)
    }

    @Test
    fun `repeating items bug is fixed by normalization and name-based aggregation`() = runTest {
        fakeAuthRepository.authenticated = true
        initViewModel()
        advanceUntilIdle()

        // Add meal with "Keto Item"
        viewModel.addMeal("1", MealType.LUNCH)
        advanceUntilIdle()
        
        assertEquals(1, viewModel.uiState.value.shoppingList.size)
        assertEquals("Keto Item", viewModel.uiState.value.shoppingList[0].name)

        // Mock case where "keto item " (with space) is added to another recipe
        // or just simulate ensureIngredientsSync running with a slightly different name
        // The ViewModel now trims and lowercases for comparison
        
        // Let's add another recipe with "keto item "
        fakeRecipeRepository.addRecipe(Recipe(id = "3", title = "Recipe 3", ingredients = listOf("keto item ")), null)
        viewModel.addMeal("3", MealType.DINNER)
        advanceUntilIdle()
        
        // Should still only have 1 item because "keto item " trims to "Keto Item"
        assertEquals(1, viewModel.uiState.value.shoppingList.size)
    }

    @Test
    fun `uncrossing bug is fixed by pendingToggles mechanism`() = runTest {
        fakeAuthRepository.authenticated = true
        fakeMealPlanRepository.shoppingItems.add(ShoppingItem(id = "Salt", name = "Salt", isChecked = false, userId = "123"))
        fakeMealPlanRepository.triggerUpdate()
        initViewModel()
        advanceUntilIdle()

        val item = viewModel.uiState.value.shoppingList[0]
        assertFalse(item.isChecked)

        // 1. Toggle item
        viewModel.toggleShoppingItem(item.id)
        
        // 2. Simulate Firestore emitting STALE data (unchecked) immediately (local cache behavior)
        val staleList = listOf(ShoppingItem(id = "Salt", name = "Salt", isChecked = false, userId = "123"))
        fakeMealPlanRepository.emitStale(staleList)
        advanceUntilIdle()
        
        // UI should still be checked because it's in pendingToggles
        assertTrue("UI should stay checked despite stale data", viewModel.uiState.value.shoppingList[0].isChecked)
        
        // 3. Simulate Firestore emitting CORRECT data (checked) from server
        fakeMealPlanRepository.shoppingItems[0] = ShoppingItem(id = "Salt", name = "Salt", isChecked = true, userId = "123")
        fakeMealPlanRepository.triggerUpdate()
        advanceUntilIdle()
        
        // Should still be checked
        assertTrue("UI should be checked after server sync", viewModel.uiState.value.shoppingList[0].isChecked)
        
        // 4. After some time, pendingToggles should be cleared (the 200ms delay in ViewModel)
        // advanceUntilIdle already handled it, but let's be explicit if needed
        
        // Now if we emit stale data AGAIN, it should finally uncross (because it's no longer pending)
        // (Though this shouldn't happen in real Firestore after a server sync)
        fakeMealPlanRepository.emitStale(staleList)
        advanceUntilIdle()
        assertFalse("UI should uncross if server says so and NOT pending", viewModel.uiState.value.shoppingList[0].isChecked)
    }
}

class FixFakeMealPlanRepository : MealPlanRepository {
    val mealPlans = mutableListOf<MealPlanEntry>()
    val shoppingItems = mutableListOf<ShoppingItem>()
    private val _mealPlanFlow = MutableStateFlow<List<MealPlanEntry>>(emptyList())
    private val _shoppingFlow = MutableStateFlow<List<ShoppingItem>>(emptyList())

    fun triggerUpdate() {
        _mealPlanFlow.value = mealPlans.toList()
        _shoppingFlow.value = shoppingItems.toList()
    }
    
    fun emitStale(items: List<ShoppingItem>) {
        _shoppingFlow.value = items
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
            if (!shoppingItems.any { it.name.trim().lowercase() == newItem.name.trim().lowercase() }) {
                shoppingItems.add(newItem)
            }
        }
        triggerUpdate()
        return Result.success(Unit)
    }
}

class FixFakeAuthRepository : AuthRepository {
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

class FixFakeRecipeRepository : RecipeRepository {
    private val recipes = mutableListOf(
        Recipe(id = "1", title = "Recipe 1", dietaryTags = listOf("Keto"), ingredients = listOf("Keto Item"), calories = 450),
        Recipe(id = "2", title = "Recipe 2", dietaryTags = listOf("Vegan"), ingredients = listOf("Vegan Item"), calories = 550)
    )

    override suspend fun getRecipes(): Result<List<Recipe>> = Result.success(recipes)
    override suspend fun getRecipeById(id: String): Result<Recipe?> = Result.success(recipes.find { it.id == id })
    override suspend fun getRecipesByTags(tags: List<String>): Result<List<Recipe>> {
        val filtered = recipes.filter { r -> r.dietaryTags.any { t -> tags.contains(t) } }
        return Result.success(filtered)
    }
    override suspend fun addRecipe(recipe: Recipe, userId: String?): Result<Unit> {
        recipes.add(recipe)
        return Result.success(Unit)
    }
}
