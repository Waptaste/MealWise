package com.example.mealwise.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun MealWiseTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    testTag: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier
            .fillMaxWidth()
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .semantics {
                if (error != null) {
                    contentDescription = "$label error: $error"
                }
            },
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon,
        singleLine = true,
        enabled = enabled
    )
}
