package com.example.mealwise.ui.planner

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    onNavigateToShopping: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showRecipeDialog by remember { mutableStateOf<MealType?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weekly Planner") },
                actions = {
                    TextButton(onClick = onNavigateToShopping) {
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
        ) {
            CalendarRow(
                selectedDate = uiState.selectedDate,
                onDateSelected = { viewModel.selectDate(it) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MealTypeSection(
                    title = "Breakfast",
                    plans = uiState.mealPlans.filter { 
                        it.mealType == MealType.BREAKFAST.name && 
                        isSameDate(it.date, uiState.selectedDate)
                    },
                    onAdd = { showRecipeDialog = MealType.BREAKFAST },
                    onRemove = { viewModel.removeMeal(it) }
                )

                MealTypeSection(
                    title = "Lunch",
                    plans = uiState.mealPlans.filter { 
                        it.mealType == MealType.LUNCH.name && 
                        isSameDate(it.date, uiState.selectedDate)
                    },
                    onAdd = { showRecipeDialog = MealType.LUNCH },
                    onRemove = { viewModel.removeMeal(it) }
                )

                MealTypeSection(
                    title = "Dinner",
                    plans = uiState.mealPlans.filter { 
                        it.mealType == MealType.DINNER.name && 
                        isSameDate(it.date, uiState.selectedDate)
                    },
                    onAdd = { showRecipeDialog = MealType.DINNER },
                    onRemove = { viewModel.removeMeal(it) }
                )
                
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
            }
        }
    }

    if (showRecipeDialog != null) {
        RecipeSelectionDialog(
            recipes = uiState.availableRecipes,
            onDismiss = { showRecipeDialog = null },
            onSelect = { recipeId ->
                viewModel.addMeal(recipeId, showRecipeDialog!!)
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(dates) { date ->
            val isSelected = date == selectedDate
            FilterChip(
                selected = isSelected,
                onClick = { onDateSelected(date) },
                label = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(date.format(DateTimeFormatter.ofPattern("EEE")))
                        Text(date.dayOfMonth.toString(), fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

@Composable
fun MealTypeSection(
    title: String,
    plans: List<MealPlanEntry>,
    onAdd: () -> Unit,
    onRemove: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                IconButton(onClick = onAdd) {
                    Icon(Icons.Default.Add, contentDescription = "Add meal")
                }
            }

            if (plans.isEmpty()) {
                Text(
                    text = "No meals planned",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                plans.forEach { plan ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = plan.recipeTitle, style = MaterialTheme.typography.bodyLarge)
                        IconButton(onClick = { onRemove(plan.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecipeSelectionDialog(
    recipes: List<com.example.mealwise.data.model.Recipe>,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Recipe") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (recipes.isEmpty()) {
                    Text("No recipes available")
                } else {
                    recipes.forEach { recipe ->
                        ListItem(
                            headlineContent = { Text(recipe.title) },
                            modifier = Modifier.clickable { onSelect(recipe.id) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

fun isSameDate(millis: Long, date: LocalDate): Boolean {
    val planDate = java.time.Instant.ofEpochMilli(millis).atZone(java.time.ZoneOffset.UTC).toLocalDate()
    return planDate == date
}
