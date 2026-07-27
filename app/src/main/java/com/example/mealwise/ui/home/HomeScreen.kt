package com.example.mealwise.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mealwise.ui.auth.AuthNavigationEvent
import com.example.mealwise.ui.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToRecipes: () -> Unit,
    onNavigateToPlanner: () -> Unit,
    onNavigateToShopping: () -> Unit,
    onNavigateToNutrition: () -> Unit,
    onNavigateToBudget: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val profile = uiState.authenticatedProfile

    LaunchedEffect(Unit) {
        viewModel.navigationEvents.collect { event ->
            when (event) {
                is AuthNavigationEvent.NavigateToLogin -> onNavigateToLogin()
                else -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MealWise") },
                actions = {
                    TextButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.testTag("logout_button")
                    ) {
                        Text("Logout")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Hello, ${profile?.name ?: "User"}!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("welcome_text")
            )
            Text(
                text = profile?.email ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("email_text")
            )

            Spacer(modifier = Modifier.padding(16.dp))

            val features = listOf(
                "Meal Plans",
                "Recipes",
                "Shopping List",
                "Nutrition Overview",
                "Budget Planner"
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(features) { feature ->
                    FeatureCard(
                        title = feature,
                        onClick = {
                            when (feature) {
                                "Recipes" -> onNavigateToRecipes()
                                "Meal Plans" -> onNavigateToPlanner()
                                "Shopping List" -> onNavigateToShopping()
                                "Nutrition Overview" -> onNavigateToNutrition()
                                "Budget Planner" -> onNavigateToBudget()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FeatureCard(title: String, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("feature_card_$title")
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Get started",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun Spacer(modifier: Modifier) {
    androidx.compose.foundation.layout.Spacer(modifier = modifier)
}
