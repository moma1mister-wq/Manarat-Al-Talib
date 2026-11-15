package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = PurpleSoft,
    onPrimary = Color.White,
    primaryContainer = PurpleDeep,
    onPrimaryContainer = Color.White,
    secondary = EmeraldPrimary,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF004D25),
    onSecondaryContainer = EmeraldLight,
    tertiary = GoldAccent,
    background = DarkBackground,
    onBackground = Color(0xFFF3EFFF),
    surface = DarkSurface,
    onSurface = Color(0xFFF3EFFF),
    surfaceVariant = DarkCard,
    onSurfaceVariant = Color(0xFFD1C4E9)
)

private val LightColorScheme = lightColorScheme(
    primary = PurplePrimary,
    onPrimary = Color.White,
    primaryContainer = PurpleCardTint,
    onPrimaryContainer = PurpleDeep,
    secondary = EmeraldPrimary,
    onSecondary = Color.White,
    secondaryContainer = EmeraldSoftBg,
    onSecondaryContainer = Color(0xFF005B23),
    tertiary = GoldAccent,
    background = PurpleLightBg,
    onBackground = Color(0xFF211238),
    surface = Color.White,
    onSurface = Color(0xFF211238),
    surfaceVariant = Color(0xFFEDE7F6),
    onSurfaceVariant = Color(0xFF4A3B69)
)

val AppShapes = Shapes(
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(32.dp),
    extraLarge = RoundedCornerShape(40.dp)
)

@Composable
fun StudyOrganizerTheme(
    darkTheme: Boolean = false,
    isArabicRtl: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val layoutDirection = if (isArabicRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = AppShapes,
            content = content
        )
    }
}
