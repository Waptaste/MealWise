package com.example.mealwise.ui.budget

import android.content.Context
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mealwise.data.model.BudgetCategory
import com.example.mealwise.data.model.Commodity
import com.example.mealwise.data.model.MonthlyBudget
import com.example.mealwise.ui.components.MealWiseTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel,
    onNavigateToManagePrices: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isWishlistMode) "Build Your Wishlist" else "Budget Result", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.isWishlistMode) {
                        IconButton(onClick = onNavigateToManagePrices) {
                            Icon(Icons.Default.Settings, contentDescription = "Manage Prices")
                        }
                    } else {
                        IconButton(onClick = { viewModel.switchToWishlist() }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Wishlist")
                        }
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
            if (uiState.commodities.isEmpty()) {
                LoadingOrErrorState(
                    isLoading = uiState.isLoading,
                    error = uiState.error,
                    onRetry = { viewModel.retryLoading() }
                )
            } else {
                AnimatedContent(
                    targetState = uiState.isWishlistMode,
                    label = "budget_flow"
                ) { isWishlist ->
                    if (isWishlist) {
                        WishlistMode(
                            uiState = uiState,
                            onAddToWishlist = { viewModel.addToWishlist(it) },
                            onRemoveFromWishlist = { viewModel.removeFromWishlist(it) },
                            onAmountChanged = { viewModel.onAmountChanged(it) },
                            onHouseholdSizeChanged = { viewModel.onHouseholdSizeChanged(it) },
                            onGenerate = { viewModel.generateOptimizedBudget() }
                        )
                    } else {
                        ResultMode(
                            uiState = uiState,
                            onReplaceItem = { viewModel.onReplaceItemClick(it) },
                            onSaveToShoppingList = { viewModel.saveBudgetToShoppingList() },
                            onShare = { shareBudget(context, uiState.generatedBudget) }
                        )
                    }
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
fun WishlistMode(
    uiState: BudgetUiState,
    onAddToWishlist: (Commodity) -> Unit,
    onRemoveFromWishlist: (String) -> Unit,
    onAmountChanged: (String) -> Unit,
    onHouseholdSizeChanged: (String) -> Unit,
    onGenerate: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Top Section: Inputs & Summary
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Estimated Total", style = MaterialTheme.typography.labelMedium)
                        Text("ZMW ${"%.2f".format(uiState.wishlistTotal)}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onGenerate, enabled = uiState.wishlist.isNotEmpty() && uiState.enteredAmount.isNotBlank()) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Done", modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MealWiseTextField(
                        value = uiState.enteredAmount,
                        onValueChange = onAmountChanged,
                        label = "Cash in Hand",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    MealWiseTextField(
                        value = uiState.householdSize,
                        onValueChange = onHouseholdSizeChanged,
                        label = "People",
                        modifier = Modifier.weight(0.6f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
        }

        Text(
            text = "Tap items to add to your monthly list:",
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.commodities.sortedBy { it.name }) { commodity ->
                val isInWishlist = uiState.wishlist.any { it.id == commodity.id }
                WishlistItemRow(
                    commodity = commodity,
                    isSelected = isInWishlist,
                    onToggle = { if (isInWishlist) onRemoveFromWishlist(commodity.id) else onAddToWishlist(commodity) }
                )
            }
        }
    }
}

@Composable
fun WishlistItemRow(commodity: Commodity, isSelected: Boolean, onToggle: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        tonalElevation = if (isSelected) 0.dp else 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isSelected) Icons.Default.RemoveCircle else Icons.Default.AddCircle,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = commodity.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(text = "K${commodity.unitPrice} per ${commodity.unit}", style = MaterialTheme.typography.bodySmall)
            }
            if (commodity.isStaple) {
                Icon(Icons.Default.PushPin, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

@Composable
fun ResultMode(
    uiState: BudgetUiState,
    onReplaceItem: (String) -> Unit,
    onSaveToShoppingList: () -> Unit,
    onShare: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BudgetResultCard(
            budget = uiState.generatedBudget!!,
            onReplaceItem = onReplaceItem
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onSaveToShoppingList,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save to List")
            }

            OutlinedButton(
                onClick = onShare,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share List")
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

private fun shareBudget(context: Context, budget: MonthlyBudget?) {
    if (budget == null) return
    
    val sb = StringBuilder()
    sb.append("🛒 My MealWise Zambian Grocery List\n")
    sb.append("Optimized for ${budget.householdSize} people\n\n")
    
    budget.items.forEach { item ->
        val qty = if (item.isDiscrete) item.quantity.toInt().toString() else "%.1f".format(item.quantity)
        sb.append("• ${item.commodityName}: $qty ${item.unit} (K${"%.2f".format(item.totalCost)})\n")
    }
    
    sb.append("\nTotal Estimated: K${"%.2f".format(budget.items.sumOf { it.totalCost })}\n")
    sb.append("Generated with MealWise App")

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "My Grocery List")
        putExtra(Intent.EXTRA_TEXT, sb.toString())
    }
    context.startActivity(Intent.createChooser(intent, "Share via"))
}

@Composable
fun LoadingOrErrorState(
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (isLoading) {
                CircularProgressIndicator(strokeWidth = 4.dp)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Fetching Zambian Market Data...", style = MaterialTheme.typography.bodyMedium)
            } else if (error != null) {
                Text(text = error, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onRetry) { Text("Retry") }
            }
        }
    }
}

@Composable
fun BudgetResultCard(
    budget: com.example.mealwise.data.model.MonthlyBudget,
    onReplaceItem: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Monthly Projections",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Optimized for ${budget.householdSize} People",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Text(
                    text = "ZMW ${"%.2f".format(budget.items.sumOf { it.totalCost })}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
            
            val sortedItems = budget.items.sortedByDescending { it.isMustHave }
            
            sortedItems.forEach { item ->
                BudgetItemRow(item = item, onReplace = { onReplaceItem(item.commodityName) })
            }
        }
    }
}

@Composable
fun BudgetItemRow(item: com.example.mealwise.data.model.BudgetItem, onReplace: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = item.commodityName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                if (item.isMustHave) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        Icons.Default.Star, 
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFFFFC107)
                    )
                }
            }
            Text(
                text = if (item.isDiscrete) {
                    "${item.quantity.toInt()} ${item.unit}"
                } else {
                    "${"%.2f".format(item.quantity)} ${item.unit}"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "K${"%.2f".format(item.totalCost)}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onReplace,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Cached, 
                    contentDescription = "Replace",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
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
        title = { Text("Alternative Item", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                commodities.sortedBy { it.name }.forEach { commodity ->
                    ListItem(
                        headlineContent = { Text(commodity.name, fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text("K${commodity.unitPrice} / ${commodity.unit}") },
                        leadingContent = {
                            Icon(Icons.Default.Restaurant, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                        },
                        modifier = Modifier.clickable { onSelect(commodity) }
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = RoundedCornerShape(28.dp)
    )
}
