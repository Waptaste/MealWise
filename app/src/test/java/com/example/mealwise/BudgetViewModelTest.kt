package com.example.mealwise

import com.example.mealwise.data.model.BudgetCategory
import com.example.mealwise.data.model.Commodity
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
        viewModel = BudgetViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `generateBudget with economical category scales correctly`() = runTest {
        advanceUntilIdle() // Load commodities
        
        viewModel.onCategorySelected(BudgetCategory.ECONOMICAL)
        viewModel.onAmountChanged("600")
        viewModel.onHouseholdSizeChanged("4")
        
        viewModel.generateBudget()
        
        val budget = viewModel.uiState.value.generatedBudget
        assertNotNull(budget)
        assertEquals(BudgetCategory.ECONOMICAL, budget?.category)
        assertTrue(budget!!.totalAmount <= 600.0)
        assertTrue(budget.items.isNotEmpty())
    }

    @Test
    fun `generateBudget invalidates amount range`() = runTest {
        advanceUntilIdle()
        
        viewModel.onCategorySelected(BudgetCategory.ECONOMICAL)
        viewModel.onAmountChanged("1000") // Too high for economical (range 500-700)
        
        viewModel.generateBudget()
        
        assertNotNull(viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.error!!.contains("Amount for ECONOMICAL must be between K500 - K700"))
    }
}

class FakeBudgetRepository : BudgetRepository {
    private val _commodities = MutableStateFlow(listOf(
        Commodity("1", "Roller Meal", 190.0, "25kg", BudgetCategory.ECONOMICAL, 0.5),
        Commodity("2", "Beans", 25.0, "kg", BudgetCategory.ECONOMICAL, 2.0),
        Commodity("5", "Breakfast Meal", 230.0, "25kg", BudgetCategory.AVERAGE, 0.5),
        Commodity("10", "Beef", 95.0, "kg", BudgetCategory.ENJOYING, 3.0)
    ))

    override fun getCommodities(): Flow<List<Commodity>> = _commodities

    override suspend fun updateCommodityPrice(id: String, newPrice: Double): Result<Unit> {
        val list = _commodities.value.map { if (it.id == id) it.copy(unitPrice = newPrice) else it }
        _commodities.value = list
        return Result.success(Unit)
    }

    override suspend fun initializeDefaultCommodities(): Result<Unit> = Result.success(Unit)
}
