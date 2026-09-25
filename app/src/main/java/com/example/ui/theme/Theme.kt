package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = RomanticRosePrimary,
    onPrimary = Color.White,
    primaryContainer = RomanticRubySecondary,
    onPrimaryContainer = Color.White,
    secondary = RomanticPastelBlush,
    onSecondary = RomanticWineText,
    background = RomanticDarkBg,
    onBackground = RomanticSoftPink,
    surface = RomanticDarkCard,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = RomanticRosePrimary,
    onPrimary = Color.White,
    primaryContainer = RomanticSoftPink,
    onPrimaryContainer = RomanticWineText,
    secondary = RomanticRubySecondary,
    onSecondary = Color.White,
    background = RomanticBlushBg,
    onBackground = RomanticWineText,
    surface = RomanticCardBg,
    onSurface = RomanticWineText,
    surfaceVariant = RomanticSoftPink,
    onSurfaceVariant = RomanticWineText
)

@Composable
fun MyApplicationTheme(
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
