package com.example.mealwise

import com.example.mealwise.data.model.MealPlanEntry
import com.example.mealwise.data.model.UserProfile
import com.example.mealwise.ui.nutrition.NutritionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class NutritionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: NutritionViewModel
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
        viewModel = NutritionViewModel(fakeMealPlanRepository, fakeRecipeRepository, fakeAuthRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `calculateNutrition sums calories for today correctly`() = runTest {
        fakeAuthRepository.authenticated = true
        fakeAuthRepository.profiles[fakeAuthRepository.currentUserUid] = UserProfile(
            uid = fakeAuthRepository.currentUserUid,
            healthGoals = listOf("Weight Loss")
        )
        
        val today = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        
        // Add 2 meals for today using addMealPlanEntry to trigger flow
        fakeMealPlanRepository.addMealPlanEntry(MealPlanEntry(recipeId = "1", date = today))
        fakeMealPlanRepository.addMealPlanEntry(MealPlanEntry(recipeId = "2", date = today))
        
        initViewModel()
        advanceUntilIdle()

        // Recipe 1 (450 cal) + Recipe 2 (550 cal) = 1000 cal
        assertEquals(1000, viewModel.uiState.value.dailyCalories)
        // Weight loss goal should be 1800
        assertEquals(1800, viewModel.uiState.value.calorieGoal)
    }

    @Test
    fun `calculateNutrition ignores meals from other days`() = runTest {
        fakeAuthRepository.authenticated = true
        val today = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        val tomorrow = LocalDate.now().plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        
        fakeMealPlanRepository.addMealPlanEntry(MealPlanEntry(recipeId = "1", date = today))
        fakeMealPlanRepository.addMealPlanEntry(MealPlanEntry(recipeId = "2", date = tomorrow))
        
        initViewModel()
        advanceUntilIdle()

        // Only Recipe 1 for today (450 cal)
        assertEquals(450, viewModel.uiState.value.dailyCalories)
    }
}
