package com.example.mealwise

import com.example.mealwise.data.model.BudgetCategory
import com.example.mealwise.data.model.Commodity
import com.example.mealwise.data.model.MonthlyBudget
import com.example.mealwise.data.repository.BudgetRepository
import com.example.mealwise.ui.budget.BudgetViewModel
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BudgetViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: BudgetViewModel
    private lateinit var fakeRepository: FakeBudgetRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeBudgetRepository()
    }

    private fun initViewModel() {
        viewModel = BudgetViewModel(
            fakeRepository,
            FakeMealPlanRepository(),
            FakeAuthRepository()
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `generateBudget with economical category scales correctly`() = runTest {
        initViewModel()
        advanceUntilIdle() // Load commodities
        
        val rollerMeal = viewModel.uiState.value.commodities.find { it.name.contains("Roller") }!!
        viewModel.addToWishlist(rollerMeal)
        
        viewModel.onCategorySelected(BudgetCategory.ECONOMICAL)
        viewModel.onAmountChanged("600")
        viewModel.onHouseholdSizeChanged("4")
        
        viewModel.generateOptimizedBudget()
        advanceUntilIdle()
        
        val budget = viewModel.uiState.value.generatedBudget
        assertNotNull("Budget should not be null", budget)
        assertEquals(BudgetCategory.ECONOMICAL, budget?.category)
        assertTrue("Budget total K${budget?.totalAmount} should be <= K600", budget!!.totalAmount <= 600.0)
        assertTrue("Budget should have items", budget.items.isNotEmpty())
    }

    @Test
    fun `generateBudget invalidates amount range`() = runTest {
        initViewModel()
        advanceUntilIdle()
        
        viewModel.onCategorySelected(BudgetCategory.ECONOMICAL)
        viewModel.onAmountChanged("1000") // Too high for economical (range 500-700)
        
        viewModel.generateOptimizedBudget()
        advanceUntilIdle()
        
        assertNotNull(viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.error!!.contains("Amount for ECONOMICAL must be between K500 - K700"))
    }

    @Test
    fun `loadSavedBudgets populates state`() = runTest {
        val testBudget = MonthlyBudget(id = "b1", userId = "123", totalAmount = 500.0)
        fakeRepository.savedBudgetsFlow.value = listOf(testBudget)
        
        // Use a real dispatcher for the test to ensure collect starts
        initViewModel()
        advanceUntilIdle()
        
        assertEquals(1, viewModel.uiState.value.savedBudgets.size)
        assertEquals("b1", viewModel.uiState.value.savedBudgets[0].id)
    }
}

class FakeBudgetRepository : BudgetRepository {
    private val _commodities = MutableStateFlow(listOf(
        Commodity("1", "Roller Meal", 190.0, "25kg", BudgetCategory.ECONOMICAL, 0.5, isStaple = true),
        Commodity("2", "Beans", 25.0, "kg", BudgetCategory.ECONOMICAL, 2.0, isMustHave = true),
        Commodity("5", "Breakfast Meal", 230.0, "25kg", BudgetCategory.AVERAGE, 0.5, isStaple = true),
        Commodity("10", "Beef", 95.0, "kg", BudgetCategory.ENJOYING, 3.0)
    ))
    val savedBudgetsFlow = MutableStateFlow<List<MonthlyBudget>>(emptyList())

    override fun getCommodities(): Flow<List<Commodity>> = _commodities

    override suspend fun updateCommodityPrice(id: String, newPrice: Double): Result<Unit> {
        val list = _commodities.value.map { if (it.id == id) it.copy(unitPrice = newPrice) else it }
        _commodities.value = list
        return Result.success(Unit)
    }

    override suspend fun initializeDefaultCommodities(): Result<Unit> = Result.success(Unit)

    override suspend fun updateCommodityMustHave(id: String, isMustHave: Boolean): Result<Unit> = Result.success(Unit)
    override suspend fun updateCommodityStapleStatus(id: String, isStaple: Boolean): Result<Unit> = Result.success(Unit)
    override suspend fun addCommodity(commodity: Commodity): Result<Unit> = Result.success(Unit)
    override suspend fun deleteCommodity(id: String): Result<Unit> = Result.success(Unit)

    override suspend fun saveBudget(budget: MonthlyBudget): Result<Unit> {
        savedBudgetsFlow.value = savedBudgetsFlow.value + budget
        return Result.success(Unit)
    }
    override fun getSavedBudgets(userId: String): Flow<List<MonthlyBudget>> = savedBudgetsFlow
}
