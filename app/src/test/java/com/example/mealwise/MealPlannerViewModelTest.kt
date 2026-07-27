package com.example.mealwise

import com.example.mealwise.data.model.MealPlanEntry
import com.example.mealwise.data.model.MealType
import com.example.mealwise.data.model.ShoppingItem
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MealPlannerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: MealPlannerViewModel
    private lateinit var fakeMealPlanRepository: FakeMealPlanRepository
    private lateinit var fakeRecipeRepository: FakeRecipeRepository
    private lateinit var fakeAuthRepository: FakeAuthRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeMealPlanRepository = FakeMealPlanRepository()
        fakeRecipeRepository = FakeRecipeRepository()
        fakeAuthRepository = FakeAuthRepository()
    }

    private fun initViewModel() {
        viewModel = MealPlannerViewModel(fakeMealPlanRepository, fakeRecipeRepository, fakeAuthRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `shopping list aggregates ingredients from multiple recipes`() = runTest {
        fakeAuthRepository.authenticated = true
        initViewModel()
        advanceUntilIdle() // Process loadData init

        // Add 2 meals
        viewModel.addMeal("1", MealType.LUNCH)
        advanceUntilIdle()
        viewModel.addMeal("2", MealType.DINNER)
        advanceUntilIdle()

        val shoppingList = viewModel.uiState.value.shoppingList
        assertEquals(2, shoppingList.size)
        assertTrue(shoppingList.any { it.name == "Keto Item" })
        assertTrue(shoppingList.any { it.name == "Vegan Item" })
    }

    @Test
    fun `shopping list preserves checked status for existing items`() = runTest {
        fakeAuthRepository.authenticated = true
        // Important: Add item with specific ID and name to match logic
        fakeMealPlanRepository.shoppingItems.add(ShoppingItem(id = "item1", name = "Keto Item", isChecked = true))
        
        initViewModel()
        advanceUntilIdle()

        // Add a meal that uses "Keto Item"
        viewModel.addMeal("1", MealType.LUNCH)
        advanceUntilIdle()

        val shoppingList = viewModel.uiState.value.shoppingList
        val ketoItem = shoppingList.find { it.name == "Keto Item" }
        assertEquals(true, ketoItem?.isChecked)
    }
}

class FakeMealPlanRepository : MealPlanRepository {
    val mealPlans = mutableListOf<MealPlanEntry>()
    val shoppingItems = mutableListOf<ShoppingItem>()
    private val _mealPlanFlow = MutableStateFlow<List<MealPlanEntry>>(emptyList())
    private val _shoppingFlow = MutableStateFlow<List<ShoppingItem>>(emptyList())

    override suspend fun addMealPlanEntry(entry: MealPlanEntry): Result<String> {
        val entryWithId = if (entry.id.isBlank()) entry.copy(id = "plan_${mealPlans.size}") else entry
        mealPlans.add(entryWithId)
        _mealPlanFlow.value = mealPlans.toList()
        return Result.success(entryWithId.id)
    }

    override suspend fun removeMealPlanEntry(id: String): Result<Unit> {
        mealPlans.removeIf { it.id == id }
        _mealPlanFlow.value = mealPlans.toList()
        return Result.success(Unit)
    }

    override fun getMealPlanEntries(userId: String): Flow<List<MealPlanEntry>> {
        return _mealPlanFlow
    }

    override fun getShoppingListFlow(userId: String): Flow<List<ShoppingItem>> {
        return _shoppingFlow
    }

    override suspend fun updateShoppingItem(item: ShoppingItem): Result<Unit> {
        shoppingItems.removeIf { it.id == item.id || it.name == item.name }
        shoppingItems.add(item)
        _shoppingFlow.value = shoppingItems.toList()
        return Result.success(Unit)
    }

    override suspend fun getShoppingList(userId: String): Result<List<ShoppingItem>> {
        return Result.success(shoppingItems.toList())
    }

    override suspend fun saveShoppingList(userId: String, items: List<ShoppingItem>): Result<Unit> {
        items.forEach { newItem ->
            if (!shoppingItems.any { it.name == newItem.name }) {
                shoppingItems.add(newItem)
            }
        }
        _shoppingFlow.value = shoppingItems.toList()
        return Result.success(Unit)
    }
}
