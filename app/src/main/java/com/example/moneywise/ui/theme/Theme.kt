package com.example.moneywise.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Tema Money Wise dengan warna-warna yang sesuai untuk aplikasi keuangan
private val LightColors = lightColorScheme(
    primary = Color(0xFF087F23),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB9F6CA),
    onPrimaryContainer = Color(0xFF002200),
    secondary = Color(0xFF006064),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCF8FF),
    onSecondaryContainer = Color(0xFF001F22),
    tertiary = Color(0xFF4527A0),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFEADDFF),
    onTertiaryContainer = Color(0xFF1D0060),
    background = Color(0xFFF5F5F5),
    onBackground = Color(0xFF191C1A),
    surface = Color(0xFFFCFDF7),
    onSurface = Color(0xFF191C1A),
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF4CAF50),
    onPrimary = Color(0xFF003800),
    primaryContainer = Color(0xFF036B07),
    onPrimaryContainer = Color(0xFFB9F6CA),
    secondary = Color(0xFF4DB6AC),
    onSecondary = Color(0xFF00363A),
    secondaryContainer = Color(0xFF004F56),
    onSecondaryContainer = Color(0xFFCCF8FF),
    tertiary = Color(0xFFD0BCFF),
    onTertiary = Color(0xFF381E72),
    tertiaryContainer = Color(0xFF4F378B),
    onTertiaryContainer = Color(0xFFEADDFF),
    background = Color(0xFF191C1A),
    onBackground = Color(0xFFE1E3DE),
    surface = Color(0xFF111412),
    onSurface = Color(0xFFE1E3DE),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690000)
)

@Composable
fun MoneyWiseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}