package org.hinsun.edge.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp

//
// File        : Radius.kt
// Author      : Tran Van Hoai
// Date        : 10/10/26
//

@Immutable
data class Radius(
    val extraSmall: Dp,
    val small: Dp,
    val medium: Dp,
    val large: Dp,
    val extraLarge: Dp,
)