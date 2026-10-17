package org.hinsun.edge.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp

//
// File        : Spacing.kt
// Author      : Tran Van Hoai
// Date        : 10/10/26
//

@Immutable
data class Spacing(
    val extraSmall: Dp,
    val small: Dp,
    val medium: Dp,
    val large: Dp,
    val extraLarge: Dp,
)

val CompactMode: Spacing = Spacing(
    extraSmall = Dp(4f),
    small = Dp(8f),
    medium = Dp(12f),
    large = Dp(16f),
    extraLarge = Dp(20f),
)

val StandardMode: Spacing = Spacing(
    extraSmall = Dp(6f),
    small = Dp(12f),
    medium = Dp(18f),
    large = Dp(24f),
    extraLarge = Dp(30f),
)

val ComfortableMode: Spacing = Spacing(
    extraSmall = Dp(8f),
    small = Dp(12f),
    medium = Dp(18f),
    large = Dp(24f),
    extraLarge = Dp(30f),
)

inline val LocalSpacing
    get() = staticCompositionLocalOf { StandardMode }