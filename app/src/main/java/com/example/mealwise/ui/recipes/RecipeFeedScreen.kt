package com.example.mealwise.ui.recipes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.mealwise.data.model.Recipe
import com.example.mealwise.data.model.MealType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeFeedScreen(
    viewModel: RecipeViewModel,
    onRecipeClick: (String) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddRecipeDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Discover Recipes", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddRecipeDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Your Recipe")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddRecipeDialog = true },
                icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) },
                text = { Text("AI Improve Recipe") }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
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

    if (showAddRecipeDialog) {
        AddAndImproveRecipeDialog(
            onDismiss = { showAddRecipeDialog = false },
            onImprove = { title, instructions, type ->
                viewModel.addAndImproveRecipe(title, instructions, type)
                showAddRecipeDialog = false
            }
        )
    }
}

@Composable
fun RecipeCard(
    recipe: Recipe,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (recipe.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = recipe.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.Center),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                }
            }
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = recipe.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    if (recipe.isUserCreated) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI Improved", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = recipe.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row {
                    BadgeChip(label = "${recipe.calories} kcal")
                    Spacer(modifier = Modifier.width(8.dp))
                    BadgeChip(label = "${recipe.prepTimeMinutes + recipe.cookTimeMinutes} mins")
                }
            }
        }
    }
}

@Composable
fun BadgeChip(label: String) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun AddAndImproveRecipeDialog(
    onDismiss: () -> Unit,
    onImprove: (String, String, MealType) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(MealType.LUNCH) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Improve Your Recipe", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Enter your simple recipe details, and our AI will improve the nutrition and instructions for you!", style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Recipe Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Text("Meal Category:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val types = listOf(MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER)
                    types.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }

                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text("Rough Instructions / Ingredients") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank()) onImprove(title, instructions, selectedType) },
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Improve with AI")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = RoundedCornerShape(28.dp)
    )
}
