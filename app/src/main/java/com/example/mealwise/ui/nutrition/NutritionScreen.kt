package com.example.mealwise.ui.nutrition

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionScreen(
    viewModel: NutritionViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedInfo by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nutritional Dashboard", fontWeight = FontWeight.Bold) },
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
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main Calorie Focal Point
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Daily Calorie Balance",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = { selectedInfo = "Calories: The energy you get from food. Balance this with your activity level to lose, maintain, or gain weight." }) {
                            Icon(Icons.Default.HelpOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    CalorieDisplay(
                        current = uiState.dailyCalories,
                        goal = uiState.calorieGoal
                    )
                }
            }

            Text(
                text = "Macronutrients",
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Macros in a more refined layout
            Card(
                modifier = Modifier.padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    MacroBar(
                        title = "Protein",
                        description = "Essential for building and repairing muscle and tissues.",
                        current = uiState.dailyProtein,
                        goal = uiState.proteinGoal,
                        color = MaterialTheme.colorScheme.secondary,
                        onInfoClick = { selectedInfo = it }
                    )
                    MacroBar(
                        title = "Carbs",
                        description = "Your body's primary source of energy for daily activities.",
                        current = uiState.dailyCarbs,
                        goal = uiState.carbsGoal,
                        color = Color(0xFF4CAF50),
                        onInfoClick = { selectedInfo = it }
                    )
                    MacroBar(
                        title = "Fats",
                        description = "Supports cell growth, organ protection, and nutrient absorption.",
                        current = uiState.dailyFats,
                        goal = uiState.fatsGoal,
                        color = Color(0xFFFFC107),
                        onInfoClick = { selectedInfo = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Weekly Insights
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info, 
                        contentDescription = null, 
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Weekly Insight",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "You're averaging ${uiState.weeklyAverageCalories} kcal/day.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        val status = if (uiState.weeklyAverageCalories <= uiState.calorieGoal) "On Track" else "Above Target"
                        Text(
                            text = "Status: $status",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (status == "On Track") Color(0xFF388E3C) else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }

    if (selectedInfo != null) {
        AlertDialog(
            onDismissRequest = { selectedInfo = null },
            title = { Text("Information") },
            text = { Text(selectedInfo!!) },
            confirmButton = {
                TextButton(onClick = { selectedInfo = null }) { Text("Got it") }
            }
        )
    }
}

@Composable
fun CalorieDisplay(current: Int, goal: Int) {
    val progress = (current.toFloat() / goal).coerceIn(0f, 1f)
    Box(contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(180.dp),
            strokeWidth = 16.dp,
            color = if (current > goal) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${(goal - current).coerceAtLeast(0)}", 
                style = MaterialTheme.typography.displayMedium, 
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Kcal Left", 
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MacroBar(
    title: String, 
    description: String,
    current: Int, 
    goal: Int, 
    color: Color,
    onInfoClick: (String) -> Unit
) {
    val progress = (current.toFloat() / goal).coerceIn(0f, 1f)
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                IconButton(onClick = { onInfoClick("$title: $description") }) {
                    Icon(Icons.Default.HelpOutline, contentDescription = null, modifier = Modifier.size(14.dp))
                }
            }
            Text(
                text = "$current / $goal g", 
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .background(Color.Transparent, RoundedCornerShape(5.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.15f),
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}
