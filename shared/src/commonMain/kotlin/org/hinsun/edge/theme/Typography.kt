package org.hinsun.edge.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import org.jetbrains.compose.resources.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kmmedgeai.shared.generated.resources.Res
import kmmedgeai.shared.generated.resources.andika_bold
import kmmedgeai.shared.generated.resources.andika_regular
import kmmedgeai.shared.generated.resources.mulish_black
import kmmedgeai.shared.generated.resources.mulish_bold
import kmmedgeai.shared.generated.resources.mulish_extra_bold
import kmmedgeai.shared.generated.resources.mulish_extra_light
import kmmedgeai.shared.generated.resources.mulish_light
import kmmedgeai.shared.generated.resources.mulish_medium
import kmmedgeai.shared.generated.resources.mulish_regular
import kmmedgeai.shared.generated.resources.mulish_semi_bold

//
// File        : Typography.kt
// Author      : Tran Van Hoai
// Date        : 10/10/26
//
@Composable
fun getAppFontFamily(): FontFamily {
    val andikaFontFamily = FontFamily(
        Font(Res.font.andika_regular, weight = FontWeight.Normal),
        Font(Res.font.andika_bold, weight = FontWeight.Bold),
    )

    val mulishFontFamily = FontFamily(
        Font(Res.font.mulish_extra_light, weight = FontWeight.ExtraLight),
        Font(Res.font.mulish_light, weight = FontWeight.Light),
        Font(Res.font.mulish_regular, weight = FontWeight.Normal),
        Font(Res.font.mulish_medium, weight = FontWeight.Medium),
        Font(Res.font.mulish_semi_bold, weight = FontWeight.SemiBold),
        Font(Res.font.mulish_bold, weight = FontWeight.Bold),
        Font(Res.font.mulish_extra_bold, weight = FontWeight.ExtraBold),
        Font(Res.font.mulish_black, weight = FontWeight.Black)
    )

    return mulishFontFamily
}

fun buildTypography(fontFamily: FontFamily): androidx.compose.material3.Typography {
    return androidx.compose.material3.Typography(
        bodyMedium = TextStyle(
            fontFamily = fontFamily,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        )
    )
}