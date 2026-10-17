package org.hinsun.edge.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp

//
// File        : KMMEdgeAITheme.kt
// Author      : Tran Van Hoai
// Date        : 10/10/26
//

@Composable
fun KMMEdgeAITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val appFontFamily = getAppFontFamily()
    val typography = buildTypography(appFontFamily)
    val radius = Radius(
        extraSmall = Dp(4f),
        small = Dp(8f),
        medium = Dp(12f),
        large = Dp(16f),
        extraLarge = Dp(20f)
    )

    MaterialTheme(
        typography = typography,
        colorScheme = if (darkTheme) DarkColorTheme else LightColorTheme,
        shapes = Shapes(
            extraSmall = RoundedCornerShape(radius.extraSmall),
            small = RoundedCornerShape(radius.small),
            medium = RoundedCornerShape(radius.medium),
            large = RoundedCornerShape(radius.large),
            extraLarge = RoundedCornerShape(radius.extraLarge),
        ),
    ) {
        ProvideTextStyle(value = typography.bodyMedium, content = content)
    }
}