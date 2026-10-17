package org.hinsun.edge.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import kotlin.math.pow

//
// File        : ColorPalette.kt
// Author      : Tran Van Hoai
// Date        : 10/10/26
//

val Primary = Color(0xFF2D8200)

// Calculate Relative Luminance
fun Color.onPrimaryByRelativeLuminance(): Color {
    val r = if (red <= 0.03928f) red / 12.92f else ((red + 0.055f) / 1.055f).pow(2.4f)
    val g = if (green <= 0.03928f) green / 12.92f else ((green + 0.055f) / 1.055f).pow(2.4f)
    val b = if (blue <= 0.03928f) blue / 12.92f else ((blue + 0.055f) / 1.055f).pow(2.4f)
    val luminance = 0.2126f * r + 0.7152f * g + 0.0722f * b
    return if (luminance > 0.179f) Color.Black else Color.White
}

var LightColorTheme = lightColorScheme(
    primary = Primary,
    onPrimary = Primary.onPrimaryByRelativeLuminance(),
)

var DarkColorTheme = darkColorScheme(
    primary = Primary,
    onPrimary = Primary.onPrimaryByRelativeLuminance()
)