package com.example.mealwise.ui.budget

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.mealwise.data.model.BudgetCategory
import com.example.mealwise.data.model.Commodity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagePricesScreen(
    viewModel: BudgetViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Prices & Priorities") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Custom Item")
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
            val grouped = uiState.commodities.sortedBy { it.name }.groupBy { it.category }
            
            BudgetCategory.values().forEach { category ->
                val itemsInCategory = grouped[category] ?: emptyList()
                if (itemsInCategory.isNotEmpty()) {
                    stickyHeader {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = category.name,
                                modifier = Modifier.padding(8.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                    
                    items(itemsInCategory) { commodity ->
                        PriceEditCard(
                            name = commodity.name,
                            unit = commodity.unit,
                            currentPrice = commodity.unitPrice,
                            isMustHave = commodity.isMustHave,
                            isStaple = commodity.isStaple,
                            onPriceUpdate = { newPrice ->
                                viewModel.updatePrice(commodity.id, newPrice)
                            },
                            onMustHaveToggle = {
                                viewModel.toggleMustHave(commodity.id, !commodity.isMustHave)
                            },
                            onStapleToggle = {
                                viewModel.toggleStaple(commodity.id, !commodity.isStaple)
                            },
                            onDelete = {
                                viewModel.deleteCommodity(commodity.id)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddCommodityDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, price, unit, category, isMustHave, isStaple ->
                viewModel.addCustomCommodity(name, price, unit, category, isMustHave, isStaple)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun PriceEditCard(
    name: String,
    unit: String,
    currentPrice: Double,
    isMustHave: Boolean,
    isStaple: Boolean,
    onPriceUpdate: (Double) -> Unit,
    onMustHaveToggle: () -> Unit,
    onStapleToggle: () -> Unit,
    onDelete: () -> Unit
) {
    var editValue by remember(currentPrice) { mutableStateOf(currentPrice.toString()) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(text = "Unit: $unit", style = MaterialTheme.typography.bodySmall)
                }
                
                Row {
                    IconButton(onClick = onStapleToggle) {
                        Icon(
                            imageVector = if (isStaple) Icons.Default.Star else Icons.Default.StarOutline,
                            contentDescription = "Toggle Staple",
                            tint = if (isStaple) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onMustHaveToggle) {
                        Icon(
                            imageVector = if (isMustHave) Icons.Default.Star else Icons.Default.StarOutline,
                            contentDescription = "Toggle Must-Have",
                            tint = if (isMustHave) Color(0xFFFFC107) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Item",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
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
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
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

@Composable
fun AddCommodityDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Double, String, BudgetCategory, Boolean, Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(BudgetCategory.AVERAGE) }
    var isMustHave by remember { mutableStateOf(false) }
    var isStaple by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom Commodity") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Item Name") })
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Unit Price (K)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Unit (e.g. kg)") })
                
                Spacer(modifier = Modifier.height(8.dp))
                Text("Designated Category:", style = MaterialTheme.typography.labelMedium)
                Column {
                    BudgetCategory.values().forEach { cat ->
                        val rangeText = when(cat) {
                            BudgetCategory.ECONOMICAL -> "Economical (K500 - K700)"
                            BudgetCategory.AVERAGE -> "Average (K700 - K2000)"
                            BudgetCategory.ENJOYING -> "Enjoying (K2000+)"
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = category == cat, onClick = { category = cat })
                            Text(rangeText, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isMustHave, onCheckedChange = { isMustHave = it })
                    Text("Prioritize (Must-Have)", style = MaterialTheme.typography.bodySmall)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isStaple, onCheckedChange = { isStaple = it })
                    Text("Protect (Staple)", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val p = price.toDoubleOrNull() ?: 0.0
                if (name.isNotBlank()) onAdd(name, p, unit, category, isMustHave, isStaple)
            }) {
                Text("Add Item")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
