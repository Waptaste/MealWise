package com.example.mealwise.ui.budget

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.mealwise.data.model.BudgetCategory
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
            Text(text = "Select Category", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BudgetCategory.values().forEach { category ->
                    FilterChip(
                        selected = uiState.selectedCategory == category,
                        onClick = { viewModel.onCategorySelected(category) },
                        label = { Text(category.name.lowercase().capitalize()) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            MealWiseTextField(
                value = uiState.enteredAmount,
                onValueChange = { viewModel.onAmountChanged(it) },
                label = "Monthly Amount (Kwacha)",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                error = uiState.error
            )

            Spacer(modifier = Modifier.height(8.dp))

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
                BudgetResultCard(budget = uiState.generatedBudget!!)
            }
        }
    }
}

@Composable
fun BudgetResultCard(budget: com.example.mealwise.data.model.MonthlyBudget) {
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
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            budget.items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = item.commodityName, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${"%.2f".format(item.quantity)} ${item.unit}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(text = "K${"%.2f".format(item.totalCost)}")
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "TOTAL ESTIMATE", fontWeight = FontWeight.Bold)
                Text(text = "K${"%.2f".format(budget.items.sumOf { it.totalCost })}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
