package com.example.mrdetective.presentation.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

val LocalAppColors = staticCompositionLocalOf { LightAppColors }

object AppTheme {
    val colors: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current
}

private fun AppColors.toMaterial(): ColorScheme =
    if (isDark) {
        darkColorScheme(
            primary = button,
            onPrimary = onButton,
            secondary = accent,
            onSecondary = onAccent,
            tertiary = primary,
            background = bg,
            onBackground = text,
            surface = surface,
            onSurface = text,
            surfaceVariant = surface2,
            onSurfaceVariant = text2,
            outline = outline,
            error = error,
            onError = bg,
        )
    } else {
        lightColorScheme(
            primary = button,
            onPrimary = onButton,
            secondary = accent,
            onSecondary = onAccent,
            tertiary = primary,
            background = bg,
            onBackground = text,
            surface = surface,
            onSurface = text,
            surfaceVariant = surface2,
            onSurfaceVariant = text2,
            outline = outline,
            error = error,
            onError = bg,
        )
    }

private val AppMaterialShapes =
    Shapes(
        small = AppShapes.control,
        medium = AppShapes.control,
        large = AppShapes.card,
    )

/**
 * Тема приложения.
 * darkTheme: по умолчанию следует системной настройке.
 * dynamicColor: опция Material You
 */
@Composable
fun ColdTrailTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val appColors = if (darkTheme) DarkAppColors else LightAppColors
    val colorScheme =
        if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        } else {
            appColors.toMaterial()
        }

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppMaterialShapes,
            content = content,
        )
    }
}
