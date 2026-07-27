package com.example.mealwise.ui.recipes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mealwise.data.model.Recipe

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeFeedScreen(
    viewModel: RecipeViewModel,
    onRecipeClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Discover Recipes") }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.recipes) { recipe ->
                        RecipeCard(recipe = recipe, onClick = { onRecipeClick(recipe.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun RecipeCard(recipe: Recipe, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Recipe Image", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = recipe.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    recipe.dietaryTags.take(3).forEach { tag ->
                        SuggestionChip(
                            onClick = { },
                            label = { Text(tag, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        }
    }
}
