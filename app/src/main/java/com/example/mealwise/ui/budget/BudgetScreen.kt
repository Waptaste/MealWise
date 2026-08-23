package com.example.mealwise.ui.budget

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mealwise.data.model.BudgetCategory
import com.example.mealwise.data.model.Commodity
import com.example.mealwise.ui.components.MealWiseTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel,
    onNavigateToManagePrices: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monthly Budget Planner") },
                actions = {
                    IconButton(onClick = onNavigateToManagePrices) {
                        Icon(Icons.Default.Settings, contentDescription = "Manage Prices")
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState.commodities.isEmpty()) {
                LoadingOrErrorState(
                    isLoading = uiState.isLoading,
                    error = uiState.error,
                    onRetry = { viewModel.retryLoading() }
                )
            } else {
                Text(text = "Select Category", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    BudgetCategory.values().forEach { category ->
                        val label = when(category) {
                            BudgetCategory.ECONOMICAL -> "Economical"
                            BudgetCategory.AVERAGE -> "Average"
                            BudgetCategory.ENJOYING -> "Enjoying"
                        }
                        FilterChip(
                            selected = uiState.selectedCategory == category,
                            onClick = { viewModel.onCategorySelected(category) },
                            label = { Text(label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                MealWiseTextField(
                    value = uiState.enteredAmount,
                    onValueChange = { viewModel.onAmountChanged(it) },
                    label = "Monthly Amount (Kwacha)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    error = uiState.error
                )

                Spacer(modifier = Modifier.height(12.dp))

                MealWiseTextField(
                    value = uiState.householdSize,
                    onValueChange = { viewModel.onHouseholdSizeChanged(it) },
                    label = "Household Size (People)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.generateBudget() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Generate 30-Day Budget")
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (uiState.generatedBudget != null) {
                    BudgetResultCard(
                        budget = uiState.generatedBudget!!,
                        onReplaceItem = { viewModel.onReplaceItemClick(it) }
                    )
                }
            }
        }
    }

    if (uiState.itemToReplace != null) {
        ReplacementDialog(
            commodities = uiState.commodities,
            onDismiss = { viewModel.dismissReplacementDialog() },
            onSelect = { newCommodity ->
                viewModel.applyReplacement(uiState.itemToReplace!!, newCommodity)
            }
        )
    }
}

@Composable
fun LoadingOrErrorState(
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Fetching Zambian market prices...", textAlign = TextAlign.Center)
        } else if (error != null) {
            Text(text = error, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Retry Loading")
            }
        } else {
            Text("Setting up your budget engine...", textAlign = TextAlign.Center)
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp))
        }
    }
}

@Composable
fun BudgetResultCard(
    budget: com.example.mealwise.data.model.MonthlyBudget,
    onReplaceItem: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Monthly Budget Summary",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Category: ${budget.category.name} | People: ${budget.householdSize}",
                style = MaterialTheme.typography.bodyMedium
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            
            val sortedItems = budget.items.sortedByDescending { it.isMustHave }
            
            sortedItems.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = item.commodityName, fontWeight = FontWeight.Bold)
                            if (item.isMustHave) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.Star, 
                                    contentDescription = "Must-Have",
                                    modifier = Modifier.size(14.dp),
                                    tint = Color(0xFFFFC107)
                                )
                            }
                        }
                        Text(
                            text = "${"%.2f".format(item.quantity)} ${item.unit}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "K${"%.2f".format(item.totalCost)}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        IconButton(onClick = { onReplaceItem(item.commodityName) }) {
                            Icon(
                                Icons.Default.Cached, 
                                contentDescription = "Replace Item",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "TOTAL ESTIMATE", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = "K${"%.2f".format(budget.items.sumOf { it.totalCost })}", 
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold, 
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun ReplacementDialog(
    commodities: List<Commodity>,
    onDismiss: () -> Unit,
    onSelect: (Commodity) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Replace with Alternative") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                commodities.sortedBy { it.name }.forEach { commodity ->
                    ListItem(
                        headlineContent = { Text(commodity.name) },
                        supportingContent = { Text("K${commodity.unitPrice} per ${commodity.unit}") },
                        modifier = Modifier.clickable { onSelect(commodity) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
