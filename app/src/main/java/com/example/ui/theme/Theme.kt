package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = OnGold,
    primaryContainer = GoldContainer,
    onPrimaryContainer = GoldPrimary,
    secondary = CyanAccent,
    onSecondary = OnCyan,
    secondaryContainer = CyanContainer,
    onSecondaryContainer = CyanAccent,
    tertiary = PurpleAdvisory,
    onTertiary = Color.White,
    tertiaryContainer = PurpleContainer,
    onTertiaryContainer = PurpleAdvisory,
    background = KingDarkBackground,
    onBackground = TextPrimary,
    surface = KingDarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = KingDarkCard,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    error = CrimsonDanger,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = GoldSecondary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFF3D6),
    onPrimaryContainer = Color(0xFF4A3400),
    secondary = Color(0xFF007A8A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC7F3F8),
    onSecondaryContainer = Color(0xFF003038),
    tertiary = Color(0xFF7E22CE),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF3E8FF),
    onTertiaryContainer = Color(0xFF3B0764),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    error = CrimsonDanger,
    onError = Color.White
)

@Composable
fun KingMakerTheme(
    darkTheme: Boolean = true, // KingMaker defaults to Sovereign Dark by design
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
