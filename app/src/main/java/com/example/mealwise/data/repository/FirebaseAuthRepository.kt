package com.example.mealwise.data.repository

import com.example.mealwise.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override suspend fun register(
        name: String,
        email: String,
        password: String
    ): Result<UserProfile> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: throw Exception("User creation failed")
            val uid = user.uid
            
            // Send verification email
            user.sendEmailVerification().await()
            
            val userProfile = UserProfile(
                uid = uid,
                name = name,
                email = email,
                createdAt = System.currentTimeMillis(),
                onboardingCompleted = false
            )
            
            try {
                firestore.collection("users").document(uid).set(userProfile).await()
                Result.success(userProfile)
            } catch (e: Exception) {
                // Rollback Auth user if Firestore write fails
                authResult.user?.delete()?.await()
                Result.failure(Exception("Failed to save user profile. Account creation cancelled."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(
        email: String,
        password: String
    ): Result<UserProfile> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: throw Exception("Login failed")
            
            val document = firestore.collection("users").document(uid).get().await()
            val userProfile = document.toObject(UserProfile::class.java)
            
            if (userProfile != null) {
                Result.success(userProfile)
            } else {
                Result.failure(Exception("Your account profile could not be found."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUserProfile(): Result<UserProfile?> {
        return try {
            val uid = firebaseAuth.currentUser?.uid
            if (uid != null) {
                val document = firestore.collection("users").document(uid).get().await()
                val profile = document.toObject(UserProfile::class.java)
                Result.success(profile)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserProfile(profile: UserProfile): Result<Unit> {
        return try {
            firestore.collection("users").document(profile.uid).set(profile).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun isUserAuthenticated(): Boolean {
        return firebaseAuth.currentUser != null
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendEmailVerification(): Result<Unit> {
        return try {
            firebaseAuth.currentUser?.sendEmailVerification()?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun isEmailVerified(): Boolean {
        return firebaseAuth.currentUser?.isEmailVerified == true
    }

    override fun logout() {
        firebaseAuth.signOut()
    }
}
