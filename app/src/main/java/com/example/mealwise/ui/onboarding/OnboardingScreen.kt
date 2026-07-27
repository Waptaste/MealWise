package com.example.mealwise.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mealwise.ui.components.LoadingButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onComplete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete) {
            onComplete()
        }
    }

    Scaffold(
        bottomBar = {
            OnboardingBottomBar(
                currentStep = uiState.currentStep,
                isLoading = uiState.isLoading,
                onBack = { viewModel.previousStep() },
                onNext = { viewModel.nextStep() }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OnboardingHeader(step = uiState.currentStep)
            
            Spacer(modifier = Modifier.height(32.dp))

            when (uiState.currentStep) {
                0 -> SelectionStep(
                    title = "Dietary Preferences",
                    options = viewModel.dietaryOptions,
                    selectedOptions = uiState.selectedDietary,
                    onToggle = { viewModel.toggleDietary(it) }
                )
                1 -> SelectionStep(
                    title = "Allergies",
                    options = viewModel.allergyOptions,
                    selectedOptions = uiState.selectedAllergies,
                    onToggle = { viewModel.toggleAllergy(it) }
                )
                2 -> SelectionStep(
                    title = "Health Goals",
                    options = viewModel.goalOptions,
                    selectedOptions = uiState.selectedGoals,
                    onToggle = { viewModel.toggleGoal(it) }
                )
            }

            uiState.error?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun OnboardingHeader(step: Int) {
    val title = when (step) {
        0 -> "What's your diet?"
        1 -> "Any allergies?"
        2 -> "What are your goals?"
        else -> ""
    }
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Step ${step + 1} of 3",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SelectionStep(
    title: String,
    options: List<String>,
    selectedOptions: Set<String>,
    onToggle: (String) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Select all that apply",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                val isSelected = selectedOptions.contains(option)
                FilterChip(
                    selected = isSelected,
                    onClick = { onToggle(option) },
                    label = { Text(option) },
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}

@Composable
fun OnboardingBottomBar(
    currentStep: Int,
    isLoading: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (currentStep > 0) {
            TextButton(onClick = onBack, enabled = !isLoading) {
                Text("Back")
            }
        } else {
            Spacer(modifier = Modifier.width(1.dp))
        }

        LoadingButton(
            text = if (currentStep == 2) "Finish" else "Next",
            onClick = onNext,
            isLoading = isLoading,
            modifier = Modifier.width(120.dp)
        )
    }
}
