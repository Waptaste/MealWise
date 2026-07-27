package com.example.mealwise.ui.shopping

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.mealwise.ui.planner.MealPlannerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    viewModel: MealPlannerViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shopping List") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isLoading && uiState.shoppingList.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.shoppingList.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No ingredients to buy.\nPlan some meals first!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = uiState.shoppingList,
                        key = { it.name }
                    ) { item ->
                        ShoppingListItem(
                            name = item.name,
                            isChecked = item.isChecked,
                            onToggle = { 
                                viewModel.toggleShoppingItem(item.name) 
                            }
                        )
                    }
                }
            }
            
            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun ShoppingListItem(
    name: String,
    isChecked: Boolean,
    onToggle: () -> Unit
) {
    // Surface handles the click for the entire row
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox onCheckedChange is null to delegate all clicks to the Surface
            Checkbox(
                checked = isChecked,
                onCheckedChange = null
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None,
                color = if (isChecked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
