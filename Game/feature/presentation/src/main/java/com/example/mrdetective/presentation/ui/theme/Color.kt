package com.example.mrdetective.presentation.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Immutable

@Immutable
data class AppColors(
    val bg: Color,
    val surface: Color,
    val surface2: Color,
    val primary: Color,
    val accent: Color,
    val onAccent: Color,
    val text: Color,
    val text2: Color,
    val error: Color,
    val success: Color,
    val outline: Color,
    val button: Color,
    val onButton: Color,
    val isDark: Boolean,
)

val LightAppColors =
    AppColors(
        bg = Color(0xFFF4F0E6),
        surface = Color(0xFFFFFCF5),
        surface2 = Color(0xFFE6DAC3),
        primary = Color(0xFF34291F),
        accent = Color(0xFFE0B570),
        onAccent = Color(0xFF2A1A00),
        text = Color(0xFF34291F),
        text2 = Color(0xFF75634E),
        error = Color(0xFFA8322A),
        success = Color(0xFF426638),
        outline = Color(0xFF79664E),
        button = Color(0xFF39427D),
        onButton = Color(0xFFFFFFFF),
        isDark = false,
    )

val DarkAppColors =
    AppColors(
        bg = Color(0xFF211B16),
        surface = Color(0xFF30271F),
        surface2 = Color(0xFF403428),
        primary = Color(0xFF7F6B53),
        accent = Color(0xFF8A6833),
        onAccent = Color(0xFFFFF0D8),
        text = Color(0xFFF1E6CF),
        text2 = Color(0xFFE6DFD6),
        error = Color(0xFFF2B8B5),
        success = Color(0xFFA4C98D),
        outline = Color(0xFF9C8663),
        button = Color(0xFF5162B3),
        onButton = Color(0xFFFFFFFF),
        isDark = true,
    )
