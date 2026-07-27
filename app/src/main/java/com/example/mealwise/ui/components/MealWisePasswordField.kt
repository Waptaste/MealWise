package com.example.mealwise.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun MealWisePasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    isVisible: Boolean = false,
    onToggleVisibility: () -> Unit,
    enabled: Boolean = true,
    testTag: String? = null
) {
    MealWiseTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        error = error,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            val icon = if (isVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
            IconButton(onClick = onToggleVisibility, enabled = enabled) {
                Icon(imageVector = icon, contentDescription = if (isVisible) "Hide password" else "Show password")
            }
        },
        enabled = enabled,
        testTag = testTag
    )
}
