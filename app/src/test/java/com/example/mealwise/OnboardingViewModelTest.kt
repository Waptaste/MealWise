package com.example.mealwise

import com.example.mealwise.data.model.UserProfile
import com.example.mealwise.ui.onboarding.OnboardingViewModel
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
class OnboardingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: OnboardingViewModel
    private lateinit var fakeRepository: FakeAuthRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeAuthRepository()
        viewModel = OnboardingViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `nextStep increments currentStep`() {
        assertEquals(0, viewModel.uiState.value.currentStep)
        viewModel.nextStep()
        assertEquals(1, viewModel.uiState.value.currentStep)
        viewModel.nextStep()
        assertEquals(2, viewModel.uiState.value.currentStep)
    }

    @Test
    fun `previousStep decrements currentStep`() {
        viewModel.nextStep()
        assertEquals(1, viewModel.uiState.value.currentStep)
        viewModel.previousStep()
        assertEquals(0, viewModel.uiState.value.currentStep)
    }

    @Test
    fun `toggleDietary adds and removes options`() {
        viewModel.toggleDietary("Vegan")
        assertTrue(viewModel.uiState.value.selectedDietary.contains("Vegan"))
        viewModel.toggleDietary("Vegan")
        assertTrue(viewModel.uiState.value.selectedDietary.isEmpty())
    }

    @Test
    fun `completeOnboarding updates repository and set complete state`() = runTest {
        fakeRepository.authenticated = true
        
        viewModel.toggleDietary("Vegan")
        viewModel.toggleAllergy("Nuts")
        viewModel.toggleGoal("Weight Loss")
        
        // Step 0 -> 1
        viewModel.nextStep()
        // Step 1 -> 2
        viewModel.nextStep()
        // Step 2 -> Complete
        viewModel.nextStep()
        
        advanceUntilIdle()
        
        assertTrue(viewModel.uiState.value.isComplete)
        val updatedProfile = fakeRepository.profiles[fakeRepository.currentUserUid]
        assertNotNull(updatedProfile)
        assertEquals(listOf("Vegan"), updatedProfile?.dietaryPreferences)
        assertEquals(listOf("Nuts"), updatedProfile?.allergies)
        assertEquals(listOf("Weight Loss"), updatedProfile?.healthGoals)
        assertTrue(updatedProfile?.onboardingCompleted == true)
    }
}

private fun assertNotNull(obj: Any?) {
    assertTrue(obj != null)
}
