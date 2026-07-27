package com.example.mealwise.ui.nutrition

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionScreen(
    viewModel: NutritionViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nutrition Overview") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Today's Progress",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            CalorieCircle(
                current = uiState.dailyCalories,
                goal = uiState.calorieGoal
            )

            Spacer(modifier = Modifier.height(32.dp))

            MacroSection(
                title = "Protein",
                current = uiState.dailyProtein,
                goal = uiState.proteinGoal,
                color = Color(0xFFE91E63)
            )

            Spacer(modifier = Modifier.height(16.dp))

            MacroSection(
                title = "Carbs",
                current = uiState.dailyCarbs,
                goal = uiState.carbsGoal,
                color = Color(0xFF2196F3)
            )

            Spacer(modifier = Modifier.height(16.dp))

            MacroSection(
                title = "Fats",
                current = uiState.dailyFats,
                goal = uiState.fatsGoal,
                color = Color(0xFFFFC107)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Weekly Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Average Daily Calories: ${uiState.weeklyAverageCalories} kcal",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    val status = if (uiState.weeklyAverageCalories <= uiState.calorieGoal) "On Track" else "Above Target"
                    Text(
                        text = "Status: $status",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (status == "On Track") Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun CalorieCircle(current: Int, goal: Int) {
    val progress = (current.toFloat() / goal).coerceIn(0f, 1f)
    Box(contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(150.dp),
            strokeWidth = 12.dp,
            color = if (current > goal) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "$current", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(text = "of $goal kcal", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun MacroSection(title: String, current: Int, goal: Int, color: Color) {
    val progress = (current.toFloat() / goal).coerceIn(0f, 1f)
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = "$current / $goal g", style = MaterialTheme.typography.bodyMedium)
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}
