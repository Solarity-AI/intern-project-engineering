package com.productreview.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Custom App Colors that extend Material3
data class AppColors(
    val background: Color,
    val foreground: Color,
    val card: Color,
    val cardForeground: Color,
    val primary: Color,
    val primaryForeground: Color,
    val secondary: Color,
    val secondaryForeground: Color,
    val muted: Color,
    val mutedForeground: Color,
    val accent: Color,
    val accentForeground: Color,
    val border: Color,
    val starFilled: Color,
    val starEmpty: Color,
    val success: Color,
    val destructive: Color
)

val LightAppColors = AppColors(
    background = LightColors.Background,
    foreground = LightColors.Foreground,
    card = LightColors.Card,
    cardForeground = LightColors.CardForeground,
    primary = LightColors.Primary,
    primaryForeground = LightColors.PrimaryForeground,
    secondary = LightColors.Secondary,
    secondaryForeground = LightColors.SecondaryForeground,
    muted = LightColors.Muted,
    mutedForeground = LightColors.MutedForeground,
    accent = LightColors.Accent,
    accentForeground = LightColors.AccentForeground,
    border = LightColors.Border,
    starFilled = LightColors.StarFilled,
    starEmpty = LightColors.StarEmpty,
    success = LightColors.Success,
    destructive = LightColors.Destructive
)

val DarkAppColors = AppColors(
    background = DarkColors.Background,
    foreground = DarkColors.Foreground,
    card = DarkColors.Card,
    cardForeground = DarkColors.CardForeground,
    primary = DarkColors.Primary,
    primaryForeground = DarkColors.PrimaryForeground,
    secondary = DarkColors.Secondary,
    secondaryForeground = DarkColors.SecondaryForeground,
    muted = DarkColors.Muted,
    mutedForeground = DarkColors.MutedForeground,
    accent = DarkColors.Accent,
    accentForeground = DarkColors.AccentForeground,
    border = DarkColors.Border,
    starFilled = DarkColors.StarFilled,
    starEmpty = DarkColors.StarEmpty,
    success = DarkColors.Success,
    destructive = DarkColors.Destructive
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }

private val LightColorScheme = lightColorScheme(
    primary = LightColors.Primary,
    onPrimary = LightColors.PrimaryForeground,
    secondary = LightColors.Secondary,
    onSecondary = LightColors.SecondaryForeground,
    background = LightColors.Background,
    onBackground = LightColors.Foreground,
    surface = LightColors.Card,
    onSurface = LightColors.CardForeground,
    error = LightColors.Destructive,
    outline = LightColors.Border,
    surfaceVariant = LightColors.Muted,
    onSurfaceVariant = LightColors.MutedForeground
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkColors.Primary,
    onPrimary = DarkColors.PrimaryForeground,
    secondary = DarkColors.Secondary,
    onSecondary = DarkColors.SecondaryForeground,
    background = DarkColors.Background,
    onBackground = DarkColors.Foreground,
    surface = DarkColors.Card,
    onSurface = DarkColors.CardForeground,
    error = DarkColors.Destructive,
    outline = DarkColors.Border,
    surfaceVariant = DarkColors.Muted,
    onSurfaceVariant = DarkColors.MutedForeground
)

@Composable
fun ProductReviewTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val appColors = if (darkTheme) DarkAppColors else LightAppColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

// Extension to easily access app colors
object AppTheme {
    val colors: AppColors
        @Composable
        get() = LocalAppColors.current
}
