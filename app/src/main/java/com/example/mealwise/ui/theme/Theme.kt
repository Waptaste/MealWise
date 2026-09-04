package com.example.mealwise.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimaryGreen,
    onPrimary = OnPrimaryGreen,
    primaryContainer = PrimaryContainerGreen,
    secondary = DarkSecondaryOchre,
    onSecondary = OnEarthOchre,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    background = DarkSurface,
    onBackground = DarkOnSurface
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = OnPrimaryGreen,
    primaryContainer = PrimaryContainerGreen,
    secondary = EarthOchre,
    onSecondary = OnEarthOchre,
    secondaryContainer = EarthContainerOchre,
    surface = SurfaceCream,
    onSurface = OnSurfaceStone,
    surfaceVariant = SurfaceVariantStone,
    background = SurfaceCream,
    onBackground = OnSurfaceStone,
    error = ErrorRed
)

@Composable
fun MealWiseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
