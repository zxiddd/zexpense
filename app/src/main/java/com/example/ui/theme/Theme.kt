package com.example.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = BlueLight,
    onPrimary = SurfaceWhite,
    primaryContainer = BlueDark,
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = EmeraldSuccess,
    onSecondary = SurfaceWhite,
    secondaryContainer = Color(0xFF064E3B),
    onSecondaryContainer = EmeraldLight,
    tertiary = AmberWarning,
    background = Slate900,
    surface = Slate800,
    surfaceVariant = Slate700,
    onBackground = SurfaceWhite,
    onSurface = SurfaceWhite,
    onSurfaceVariant = Slate200,
    outline = Slate600
)

private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = SurfaceWhite,
    primaryContainer = TealLight,
    onPrimaryContainer = TealDark,
    secondary = EmeraldSuccess,
    onSecondary = SurfaceWhite,
    secondaryContainer = EmeraldLight,
    onSecondaryContainer = Color(0xFF065F46),
    tertiary = AmberWarning,
    background = BackgroundLight,
    surface = SurfaceWhite,
    surfaceVariant = Color(0xFFEAF1EE),
    onBackground = TextMain,
    onSurface = TextMain,
    onSurfaceVariant = TextMuted,
    outline = Slate200
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
