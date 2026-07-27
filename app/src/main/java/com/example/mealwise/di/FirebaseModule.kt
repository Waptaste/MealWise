package com.example.mealwise.di

import com.example.mealwise.data.repository.AuthRepository
import com.example.mealwise.data.repository.BudgetRepository
import com.example.mealwise.data.repository.FirebaseAuthRepository
import com.example.mealwise.data.repository.FirebaseBudgetRepository
import com.example.mealwise.data.repository.FirebaseMealPlanRepository
import com.example.mealwise.data.repository.MealPlanRepository
import com.example.mealwise.data.repository.MockRecipeRepository
import com.example.mealwise.data.repository.RecipeRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FirebaseModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        firebaseAuthRepository: FirebaseAuthRepository
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindRecipeRepository(
        mockRecipeRepository: MockRecipeRepository
    ): RecipeRepository

    @Binds
    @Singleton
    abstract fun bindMealPlanRepository(
        firebaseMealPlanRepository: FirebaseMealPlanRepository
    ): MealPlanRepository

    @Binds
    @Singleton
    abstract fun bindBudgetRepository(
        firebaseBudgetRepository: FirebaseBudgetRepository
    ): BudgetRepository

    companion object {
        @Provides
        @Singleton
        fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

        @Provides
        @Singleton
        fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
    }
}
