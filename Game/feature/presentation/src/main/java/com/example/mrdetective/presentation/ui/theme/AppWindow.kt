package com.example.mrdetective.presentation.ui.theme

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.computeWindowSizeClass

internal val LocalAppWindow = staticCompositionLocalOf { WindowSizeClass(0, 0) }

internal val WindowSizeClass.isMediumWidth: Boolean
    get() = isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

internal val WindowSizeClass.isShort: Boolean
    get() = !isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND)

@Composable
internal fun AppWindow(content: @Composable () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val window = WindowSizeClass.BREAKPOINTS_V2.computeWindowSizeClass(maxWidth.value, maxHeight.value)
        CompositionLocalProvider(LocalAppWindow provides window, content = content)
    }
}
