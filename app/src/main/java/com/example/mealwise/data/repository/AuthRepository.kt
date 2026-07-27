package com.example.mealwise.data.repository

import com.example.mealwise.data.model.UserProfile

interface AuthRepository {
    suspend fun register(
        name: String,
        email: String,
        password: String
    ): Result<UserProfile>

    suspend fun login(
        email: String,
        password: String
    ): Result<UserProfile>

    suspend fun getCurrentUserProfile(): Result<UserProfile?>

    suspend fun updateUserProfile(profile: UserProfile): Result<Unit>

    fun isUserAuthenticated(): Boolean

    fun logout()
}
