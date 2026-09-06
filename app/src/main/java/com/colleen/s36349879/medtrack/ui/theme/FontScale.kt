package com.colleen.s36349879.medtrack.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

/**
 * Multiplies the current font scale by [scale] for everything in [content].
 *
 * Compose scales every `.sp` value by [Density.fontScale] automatically, so overriding
 * [LocalDensity] here is enough to grow text app-wide — both `MaterialTheme.typography`
 * usages and the many screens that hardcode `fontSize = Nsp` inline — without editing
 * every screen individually. Multiplies on top of the device's own accessibility font
 * scale rather than replacing it, so a patient who also has a large OS font size gets both.
 */
@Composable
fun ScaledFontProvider(scale: Float, content: @Composable () -> Unit) {
    val current = LocalDensity.current
    val scaled = Density(density = current.density, fontScale = current.fontScale * scale)
    CompositionLocalProvider(LocalDensity provides scaled, content = content)
}
