package com.example.mealwise.ui.budget

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ManagePricesScreen(
    viewModel: BudgetViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventory & Priorities", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Add Item", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search commodities...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            val filteredCommodities = uiState.commodities.filter {
                it.name.contains(searchQuery, ignoreCase = true)
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val grouped = filteredCommodities.sortedBy { it.name }.groupBy { it.category }
                
                BudgetCategory.values().forEach { category ->
                    val itemsInCategory = grouped[category] ?: emptyList()
                    if (itemsInCategory.isNotEmpty()) {
                        stickyHeader {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = category.name.uppercase(),
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        items(itemsInCategory) { commodity ->
                            PriceEditCard(
                                commodity = commodity,
                                onPriceUpdate = { viewModel.updatePrice(commodity.id, it) },
                                onMustHaveToggle = { viewModel.toggleMustHave(commodity.id, !commodity.isMustHave) },
                                onStapleToggle = { viewModel.toggleStaple(commodity.id, !commodity.isStaple) },
                                onDelete = { viewModel.deleteCommodity(commodity.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddCommodityDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, price, unit, cat, must, staple ->
                viewModel.addCustomCommodity(name, price, unit, cat, must, staple)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun PriceEditCard(
    commodity: Commodity,
    onPriceUpdate: (Double) -> Unit,
    onMustHaveToggle: () -> Unit,
    onStapleToggle: () -> Unit,
    onDelete: () -> Unit
) {
    var editValue by remember(commodity.unitPrice) { mutableStateOf(commodity.unitPrice.toString()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = commodity.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(text = "Unit: ${commodity.unit}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onStapleToggle) {
                        Icon(
                            imageVector = if (commodity.isStaple) Icons.Default.PushPin else Icons.Default.PushPin,
                            contentDescription = "Protection",
                            tint = if (commodity.isStaple) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    }
                    IconButton(onClick = onMustHaveToggle) {
                        Icon(
                            imageVector = if (commodity.isMustHave) Icons.Default.Star else Icons.Default.StarOutline,
                            contentDescription = "Priority",
                            tint = if (commodity.isMustHave) Color(0xFFFFC107) else MaterialTheme.colorScheme.outline
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = editValue,
                    onValueChange = { editValue = it },
                    label = { Text("Price (ZMW)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Button(
                    onClick = { 
                        val price = editValue.toDoubleOrNull()
                        if (price != null) onPriceUpdate(price)
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Update")
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
        title = { Text("New Commodity", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Item Name") }, shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Unit Price (ZMW)") }, shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Unit (e.g. 25kg bag)") }, shape = RoundedCornerShape(12.dp))
                
                Text("Budget Tier:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    BudgetCategory.values().forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.name.lowercase().capitalize()) }
                        )
                    }
                }

                HorizontalDivider()

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isMustHave, onCheckedChange = { isMustHave = it })
                    Text("Prioritize as Must-Have", style = MaterialTheme.typography.bodyMedium)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isStaple, onCheckedChange = { isStaple = it })
                    Text("Protect as Staple", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = price.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank()) onAdd(name, p, unit, category, isMustHave, isStaple)
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Add to Inventory")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = RoundedCornerShape(28.dp)
    )
}
