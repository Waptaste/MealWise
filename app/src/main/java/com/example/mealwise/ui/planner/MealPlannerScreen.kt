package com.example.mealwise.ui.planner

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mealwise.data.model.MealPlanEntry
import com.example.mealwise.data.model.MealType
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlannerScreen(
    viewModel: MealPlannerViewModel,
    onNavigateToShopping: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showRecipeDialog by remember { mutableStateOf<MealType?>(null) }
    var selectedRecipeIdForPortion by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weekly Meal Plan", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = onNavigateToShopping,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Shopping List")
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
            Text(
                text = "Select Day",
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            CalendarRow(
                selectedDate = uiState.selectedDate,
                onDateSelected = { viewModel.selectDate(it) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                val categories = listOf(
                    Triple("Starters", Icons.Default.Restaurant, MealType.STARTER),
                    Triple("Breakfast", Icons.Default.Coffee, MealType.BREAKFAST),
                    Triple("Lunch", Icons.Default.WbSunny, MealType.LUNCH),
                    Triple("Dinner", Icons.Default.NightsStay, MealType.DINNER),
                    Triple("Desserts", Icons.Default.Icecream, MealType.DESSERT),
                    Triple("Beverages", Icons.Default.LocalDrink, MealType.BEVERAGE)
                )

                categories.forEach { (title, icon, type) ->
                    MealTimeSection(
                        title = title,
                        icon = icon,
                        plans = uiState.mealPlans.filter { 
                            it.mealType == type.name && 
                            isSameDate(it.date, uiState.selectedDate)
                        },
                        onAdd = { showRecipeDialog = type },
                        onRemove = { viewModel.removeMeal(it) }
                    )
                }
                
                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(48.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showRecipeDialog != null) {
        val filteredRecipes = uiState.availableRecipes.filter { recipe ->
            recipe.dietaryTags.any { it.equals(showRecipeDialog!!.name, ignoreCase = true) }
        }

        RecipeSelectionDialog(
            title = "Choose a ${showRecipeDialog!!.name.lowercase().replaceFirstChar { it.uppercase() }}",
            recipes = filteredRecipes,
            onDismiss = { showRecipeDialog = null },
            onSelect = { recipeId ->
                selectedRecipeIdForPortion = recipeId
            }
        )
    }

    if (selectedRecipeIdForPortion != null) {
        PortionSelectionDialog(
            onDismiss = { selectedRecipeIdForPortion = null },
            onConfirm = { portionSize ->
                viewModel.addMeal(selectedRecipeIdForPortion!!, showRecipeDialog!!, portionSize)
                selectedRecipeIdForPortion = null
                showRecipeDialog = null
            }
        )
    }
}

@Composable
fun CalendarRow(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val dates = remember { (0..6).map { LocalDate.now().plusDays(it.toLong()) } }
    
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(dates) { date ->
            val isSelected = date == selectedDate
            CalendarDateCard(
                date = date,
                isSelected = isSelected,
                onClick = { onDateSelected(date) }
            )
        }
    }
}

@Composable
fun CalendarDateCard(date: LocalDate, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = Modifier
            .width(64.dp)
            .height(84.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        tonalElevation = if (isSelected) 0.dp else 4.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(4.dp)
        ) {
            Text(
                text = date.format(DateTimeFormatter.ofPattern("EEE")),
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.7f)
            )
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

@Composable
fun MealTimeSection(
    title: String,
    icon: ImageVector,
    plans: List<MealPlanEntry>,
    onAdd: () -> Unit,
    onRemove: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = onAdd) {
                Icon(Icons.Default.AddCircle, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (plans.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "No $title planned",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            plans.forEach { plan ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = plan.recipeTitle, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                            val portionText = when(plan.portionSize) {
                                0.5 -> "Small Portion"
                                1.0 -> "Standard Portion"
                                1.5 -> "Large Portion"
                                else -> "Custom Portion"
                            }
                            Text(text = portionText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { onRemove(plan.id) }) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecipeSelectionDialog(
    title: String,
    recipes: List<com.example.mealwise.data.model.Recipe>,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (recipes.isEmpty()) {
                    Text("No recipes available for this category yet.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    recipes.forEach { recipe ->
                        ListItem(
                            headlineContent = { Text(recipe.title, fontWeight = FontWeight.SemiBold) },
                            supportingContent = { Text("${recipe.calories} kcal • ${recipe.dietaryTags.firstOrNull { it != "Breakfast" && it != "Lunch" && it != "Dinner" } ?: "Standard"}") },
                            leadingContent = {
                                Box(
                                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.RestaurantMenu, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                }
                            },
                            modifier = Modifier.clickable { onSelect(recipe.id) }
                        )
                    }
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

@Composable
fun PortionSelectionDialog(
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var selectedPortion by remember { mutableStateOf(1.0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("How much are you eating?", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Select your portion size to accurately track your calories.", style = MaterialTheme.typography.bodyMedium)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    PortionOption(
                        label = "Small",
                        multiplier = 0.5,
                        isSelected = selectedPortion == 0.5,
                        onClick = { selectedPortion = 0.5 }
                    )
                    PortionOption(
                        label = "Medium",
                        multiplier = 1.0,
                        isSelected = selectedPortion == 1.0,
                        onClick = { selectedPortion = 1.0 }
                    )
                    PortionOption(
                        label = "Large",
                        multiplier = 1.5,
                        isSelected = selectedPortion == 1.5,
                        onClick = { selectedPortion = 1.5 }
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedPortion) }) {
                Text("Add to Plan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Back") }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
fun PortionOption(
    label: String,
    multiplier: Double,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(80.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "x$multiplier",
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

fun isSameDate(millis: Long, date: LocalDate): Boolean {
    val planDate = java.time.Instant.ofEpochMilli(millis).atZone(java.time.ZoneOffset.UTC).toLocalDate()
    return planDate == date
}
