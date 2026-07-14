package com.nuc.omeletteinputmethod.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// retain original schemes for settings app
private val LightColorScheme = lightColorScheme(
    primary = ModernPrimary,
    onPrimary = Color.White,
    primaryContainer = CardAlt1,
    onPrimaryContainer = ModernPrimaryVariant,
    secondary = ModernSecondary,
    onSecondary = Color.White,
    secondaryContainer = CardAlt2,
    onSecondaryContainer = Color(0xFF5D4E37),
    tertiary = ModernTertiary,
    onTertiary = Color.White,
    tertiaryContainer = CardAlt3,
    onTertiaryContainer = Color(0xFF4A3D5C),
    background = SurfaceWarm,
    onBackground = Color(0xFF1C1B1F),
    surface = SurfaceWhite,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = CardAlt1,
    onSurfaceVariant = Color(0xFF6B7280),
    outline = Color(0xFFD1D5DB),
    outlineVariant = Color(0xFFE5E7EB),
    error = Color(0xFFDC2626),
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8BB8D6),
    onPrimary = Color(0xFF1A3347),
    primaryContainer = Color(0xFF3D5A6E),
    onPrimaryContainer = Color(0xFFDCE8F0),
    secondary = Color(0xFFA8B2C4),
    onSecondary = Color(0xFF2D3340),
    secondaryContainer = Color(0xFF4A4F5C),
    onSecondaryContainer = Color(0xFFE0E4EC),
    tertiary = Color(0xFFC4B8D6),
    onTertiary = Color(0xFF332D42),
    tertiaryContainer = Color(0xFF5A4F6E),
    onTertiaryContainer = Color(0xFFF0ECF8),
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE5E1E6),
    surface = Color(0xFF252528),
    onSurface = Color(0xFFE5E1E6),
    surfaceVariant = Color(0xFF2C2C30),
    onSurfaceVariant = Color(0xFFC5C5C8),
    outline = Color(0xFF48484C),
    outlineVariant = Color(0xFF38383C)
)

@Composable
fun OmeletteIMETheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
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
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

@Composable
fun OmeletteIMEKeyboardTheme(
    themeManager: ThemeManager,
    content: @Composable () -> Unit
) {
    val skin by themeManager.currentSkin.collectAsState()
    val targetScheme = skin.toColorScheme()
    val animatedScheme = animateColorScheme(targetScheme)

    MaterialTheme(
        colorScheme = animatedScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

@Composable
private fun animateColorScheme(target: androidx.compose.material3.ColorScheme): androidx.compose.material3.ColorScheme {
    val animSpec = tween<Color>(500)
    val primary by animateColorAsState(target.primary, animSpec, "primary")
    val onPrimary by animateColorAsState(target.onPrimary, animSpec, "onPrimary")
    val primaryContainer by animateColorAsState(target.primaryContainer, animSpec, "primaryContainer")
    val onPrimaryContainer by animateColorAsState(target.onPrimaryContainer, animSpec, "onPrimaryContainer")
    val secondary by animateColorAsState(target.secondary, animSpec, "secondary")
    val onSecondary by animateColorAsState(target.onSecondary, animSpec, "onSecondary")
    val secondaryContainer by animateColorAsState(target.secondaryContainer, animSpec, "secondaryContainer")
    val onSecondaryContainer by animateColorAsState(target.onSecondaryContainer, animSpec, "onSecondaryContainer")
    val tertiary by animateColorAsState(target.tertiary, animSpec, "tertiary")
    val onTertiary by animateColorAsState(target.onTertiary, animSpec, "onTertiary")
    val tertiaryContainer by animateColorAsState(target.tertiaryContainer, animSpec, "tertiaryContainer")
    val onTertiaryContainer by animateColorAsState(target.onTertiaryContainer, animSpec, "onTertiaryContainer")
    val background by animateColorAsState(target.background, animSpec, "background")
    val onBackground by animateColorAsState(target.onBackground, animSpec, "onBackground")
    val surface by animateColorAsState(target.surface, animSpec, "surface")
    val onSurface by animateColorAsState(target.onSurface, animSpec, "onSurface")
    val surfaceVariant by animateColorAsState(target.surfaceVariant, animSpec, "surfaceVariant")
    val onSurfaceVariant by animateColorAsState(target.onSurfaceVariant, animSpec, "onSurfaceVariant")
    val outline by animateColorAsState(target.outline, animSpec, "outline")
    val outlineVariant by animateColorAsState(target.outlineVariant, animSpec, "outlineVariant")
    val error by animateColorAsState(target.error, animSpec, "error")
    val onError by animateColorAsState(target.onError, animSpec, "onError")

    return if (target.background.value < 0.5f) {
        darkColorScheme(
            primary, onPrimary, primaryContainer, onPrimaryContainer,
            secondary, onSecondary, secondaryContainer, onSecondaryContainer,
            tertiary, onTertiary, tertiaryContainer, onTertiaryContainer,
            background, onBackground, surface, onSurface,
            surfaceVariant, onSurfaceVariant, outline, outlineVariant,
            error, onError
        )
    } else {
        lightColorScheme(
            primary, onPrimary, primaryContainer, onPrimaryContainer,
            secondary, onSecondary, secondaryContainer, onSecondaryContainer,
            tertiary, onTertiary, tertiaryContainer, onTertiaryContainer,
            background, onBackground, surface, onSurface,
            surfaceVariant, onSurfaceVariant, outline, outlineVariant,
            error, onError
        )
    }
}