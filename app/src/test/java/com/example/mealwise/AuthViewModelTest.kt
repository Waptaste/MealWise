package com.example.mealwise

import com.example.mealwise.data.model.UserProfile
import com.example.mealwise.data.repository.AuthRepository
import com.example.mealwise.ui.auth.AuthNavigationEvent
import com.example.mealwise.ui.auth.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: AuthViewModel
    private lateinit var fakeRepository: FakeAuthRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeAuthRepository()
        // ViewModel init calls checkAuthStatus
    }

    private fun initViewModel() {
        viewModel = AuthViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `unauthenticated startup routes to Login`() = runTest {
        fakeRepository.authenticated = false
        initViewModel()
        
        var capturedEvent: AuthNavigationEvent? = null
        val job = launch {
            capturedEvent = viewModel.navigationEvents.first()
        }

        advanceUntilIdle()

        assertEquals(AuthNavigationEvent.NavigateToLogin, capturedEvent)
        job.cancel()
    }

    @Test
    fun `authenticated startup routes to Home`() = runTest {
        fakeRepository.authenticated = true
        initViewModel()
        
        var capturedEvent: AuthNavigationEvent? = null
        val job = launch {
            capturedEvent = viewModel.navigationEvents.first()
        }

        advanceUntilIdle()

        assertEquals(AuthNavigationEvent.NavigateToHome, capturedEvent)
        assertNotNull(viewModel.uiState.value.authenticatedProfile)
        job.cancel()
    }

    @Test
    fun `logout clears state and routes to Login`() = runTest {
        fakeRepository.authenticated = true
        initViewModel()
        advanceUntilIdle()

        var capturedEvent: AuthNavigationEvent? = null
        val job = launch {
            viewModel.navigationEvents.collect { event ->
                if (event is AuthNavigationEvent.NavigateToLogin) {
                    capturedEvent = event
                }
            }
        }

        viewModel.logout()
        advanceUntilIdle()

        assertEquals(AuthNavigationEvent.NavigateToLogin, capturedEvent)
        assertNull(viewModel.uiState.value.authenticatedProfile)
        assertEquals(false, fakeRepository.authenticated)
        job.cancel()
    }

    @Test
    fun `login with blank email should show error`() {
        fakeRepository.authenticated = false
        initViewModel()
        viewModel.onEmailChanged("")
        viewModel.onPasswordChanged("Password123")
        viewModel.login()

        assertEquals("Email is required", viewModel.uiState.value.emailError)
    }

    @Test
    fun `login with invalid email should show error`() {
        fakeRepository.authenticated = false
        initViewModel()
        viewModel.onEmailChanged("invalid-email")
        viewModel.onPasswordChanged("Password123")
        viewModel.login()

        assertEquals("Invalid email format", viewModel.uiState.value.emailError)
    }

    @Test
    fun `register with blank name should show error`() {
        fakeRepository.authenticated = false
        initViewModel()
        viewModel.onNameChanged("")
        viewModel.register()
        assertEquals("Name is required", viewModel.uiState.value.nameError)
    }

    @Test
    fun `register with short name should show error`() {
        fakeRepository.authenticated = false
        initViewModel()
        viewModel.onNameChanged("A")
        viewModel.register()
        assertEquals("Name must be at least 2 characters", viewModel.uiState.value.nameError)
    }

    @Test
    fun `register with weak password should show error`() {
        fakeRepository.authenticated = false
        initViewModel()
        viewModel.onPasswordChanged("weak")
        viewModel.register()
        assertNotNull(viewModel.uiState.value.passwordError)
    }

    @Test
    fun `register with password mismatch should show error`() {
        fakeRepository.authenticated = false
        initViewModel()
        viewModel.onPasswordChanged("Password123")
        viewModel.onConfirmPasswordChanged("Different123")
        viewModel.register()
        assertEquals("Passwords do not match", viewModel.uiState.value.confirmPasswordError)
    }

    @Test
    fun `successful registration should emit navigation event`() = runTest {
        fakeRepository.authenticated = false
        initViewModel()
        viewModel.onNameChanged("John Doe")
        viewModel.onEmailChanged("john@example.com")
        viewModel.onPasswordChanged("Password123")
        viewModel.onConfirmPasswordChanged("Password123")

        var capturedEvent: AuthNavigationEvent? = null
        val job = launch {
            viewModel.navigationEvents.collect { event ->
                if (event is AuthNavigationEvent.NavigateToHome) {
                    capturedEvent = event
                }
            }
        }

        viewModel.register()
        
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.generalError)
        assertEquals(false, viewModel.uiState.value.isLoading)
        assertNotNull(viewModel.uiState.value.authenticatedProfile)
        
        assertEquals(AuthNavigationEvent.NavigateToHome, capturedEvent)
        job.cancel()
    }

    @Test
    fun `failed registration should show general error`() = runTest {
        fakeRepository.authenticated = false
        initViewModel()
        fakeRepository.shouldReturnError = true
        
        viewModel.onNameChanged("John Doe")
        viewModel.onEmailChanged("john@example.com")
        viewModel.onPasswordChanged("Password123")
        viewModel.onConfirmPasswordChanged("Password123")

        viewModel.register()
        
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.generalError)
        assertEquals("Registration failed", viewModel.uiState.value.generalError)
        assertEquals(false, viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.authenticatedProfile)
    }

    @Test
    fun `successful login should emit navigation event`() = runTest {
        fakeRepository.authenticated = false
        initViewModel()
        viewModel.onEmailChanged("john@example.com")
        viewModel.onPasswordChanged("Password123")

        var capturedEvent: AuthNavigationEvent? = null
        val job = launch {
            viewModel.navigationEvents.collect { event ->
                if (event is AuthNavigationEvent.NavigateToHome) {
                    capturedEvent = event
                }
            }
        }

        viewModel.login()
        
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.generalError)
        assertEquals(false, viewModel.uiState.value.isLoading)
        assertNotNull(viewModel.uiState.value.authenticatedProfile)
        
        assertEquals(AuthNavigationEvent.NavigateToHome, capturedEvent)
        job.cancel()
    }

    @Test
    fun `failed login should show general error`() = runTest {
        fakeRepository.authenticated = false
        initViewModel()
        fakeRepository.shouldReturnError = true
        
        viewModel.onEmailChanged("john@example.com")
        viewModel.onPasswordChanged("Password123")

        viewModel.login()
        
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.generalError)
        assertEquals("Login failed", viewModel.uiState.value.generalError)
        assertEquals(false, viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.authenticatedProfile)
    }
}

class FakeAuthRepository : AuthRepository {
    var shouldReturnError = false
    var authenticated = false
    val profiles = mutableMapOf<String, UserProfile>()
    val currentUserUid = "123"

    override suspend fun register(name: String, email: String, password: String): Result<UserProfile> {
        return if (shouldReturnError) {
            Result.failure(Exception("Registration failed"))
        } else {
            authenticated = true
            val profile = UserProfile(uid = currentUserUid, name = name, email = email, onboardingCompleted = false)
            profiles[currentUserUid] = profile
            Result.success(profile)
        }
    }
    
    override suspend fun login(email: String, password: String): Result<UserProfile> {
        return if (shouldReturnError) {
            Result.failure(Exception("Login failed"))
        } else {
            authenticated = true
            val profile = UserProfile(uid = currentUserUid, name = "John Doe", email = email, onboardingCompleted = true)
            profiles[currentUserUid] = profile
            Result.success(profile)
        }
    }
    
    override suspend fun getCurrentUserProfile(): Result<UserProfile?> {
        return if (authenticated) {
            Result.success(profiles[currentUserUid] ?: UserProfile(uid = currentUserUid, name = "John Doe", email = "john@example.com", onboardingCompleted = true))
        } else {
            Result.success(null)
        }
    }

    override suspend fun updateUserProfile(profile: UserProfile): Result<Unit> {
        return if (shouldReturnError) {
            Result.failure(Exception("Update failed"))
        } else {
            profiles[profile.uid] = profile
            Result.success(Unit)
        }
    }
    
    override fun isUserAuthenticated() = authenticated
    
    override fun logout() {
        authenticated = false
    }
}
