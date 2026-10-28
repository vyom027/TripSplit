package com.ljku.tripsplit.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = InkPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = SignalLime,
    onPrimaryContainer = Color(0xFF163300),
    secondary = BalancePositive,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD1FAE5),
    onSecondaryContainer = Color(0xFF065F46),
    tertiary = InkSecondaryLight,
    onTertiary = Color.White,
    background = CanvasLight,
    onBackground = InkPrimaryLight,
    surface = SurfaceLight,
    onSurface = InkPrimaryLight,
    surfaceVariant = SurfaceRaisedLight,
    onSurfaceVariant = InkSecondaryLight,
    outline = BorderLight,
    outlineVariant = BorderSubtleLight,
    error = BalanceNegative,
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF991B1B)
)

private val DarkColorScheme = darkColorScheme(
    primary = SignalLime,
    onPrimary = Color(0xFF0F1013),
    primaryContainer = SurfaceRaisedDark,
    onPrimaryContainer = Color(0xFFF9FAFB),
    secondary = BalancePositiveDark,
    onSecondary = Color(0xFF022C22),
    secondaryContainer = Color(0xFF064E3B),
    onSecondaryContainer = Color(0xFFA7F3D0),
    tertiary = InkSecondaryDark,
    onTertiary = Color(0xFF1F2937),
    background = CanvasDark,
    onBackground = InkPrimaryDark,
    surface = SurfaceDark,
    onSurface = InkPrimaryDark,
    surfaceVariant = SurfaceRaisedDark,
    onSurfaceVariant = InkSecondaryDark,
    outline = BorderDark,
    outlineVariant = BorderSubtleDark,
    error = BalanceNegativeDark,
    onError = Color(0xFF4C0519),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFCA5A5)
)

val LocalThemePreferences = staticCompositionLocalOf<ThemePreferences?> { null }

@Composable
fun TripSplitTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}