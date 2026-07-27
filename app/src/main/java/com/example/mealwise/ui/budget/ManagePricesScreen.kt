package com.example.mealwise.ui.budget

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mealwise.ui.components.MealWiseTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagePricesScreen(
    viewModel: BudgetViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Prices") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(uiState.commodities) { commodity ->
                PriceEditCard(
                    name = commodity.name,
                    unit = commodity.unit,
                    currentPrice = commodity.unitPrice,
                    onPriceUpdate = { newPrice ->
                        viewModel.updatePrice(commodity.id, newPrice)
                    }
                )
            }
        }
    }
}

@Composable
fun PriceEditCard(
    name: String,
    unit: String,
    currentPrice: Double,
    onPriceUpdate: (Double) -> Unit
) {
    var editValue by remember(currentPrice) { mutableStateOf(currentPrice.toString()) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(text = "Unit: $unit", style = MaterialTheme.typography.bodySmall)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = editValue,
                    onValueChange = { editValue = it },
                    label = { Text("Price (Kwacha)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Button(onClick = { 
                    val price = editValue.toDoubleOrNull()
                    if (price != null) onPriceUpdate(price)
                }) {
                    Text("Save")
                }
            }
        }
    }
}
