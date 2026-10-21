//
// File        : RobotMascot.kt
// Author      : Tran Van Hoai
// Date        : 21/10/26
//

package org.hinsun.edge.presentation.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

private val eyeColor = Color(0.80f, 0.97f, 0.86f)
private val glowColor = Color(0.45f, 0.72f, 1.0f)
private val outlineColor = Color(0.80f, 0.82f, 0.90f)
private val headColors = listOf(Color.White, Color(0.97f, 0.98f, 1.0f), Color(0.93f, 0.95f, 0.98f))
private val rimColors = listOf(
    Color(0.35f, 0.78f, 1.00f),
    Color(0.62f, 0.50f, 1.00f),
    Color(1.00f, 0.45f, 0.78f),
    Color(1.00f, 0.68f, 0.30f),
    Color(0.95f, 0.90f, 0.35f),
    Color(0.40f, 0.90f, 0.60f),
    Color(0.35f, 0.78f, 1.00f),
)

/** Port of iosApp `RobotMascot`: a floating, gently swaying robot head that blinks at random. */
@Composable
fun RobotMascot(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    floatAmount: Float = 1f,
) {
    val eyeScale = remember { Animatable(1f) }
    var frameNanos by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) withFrameNanos { frameNanos = it }
    }
    LaunchedEffect(Unit) {
        while (true) {
            delay(Random.nextLong(1_800, 5_000))
            val times = if (Random.nextInt(10) < 3) 2 else 1 // 30% double blink
            repeat(times) { i ->
                eyeScale.animateTo(0.08f, tween(70, easing = EaseIn))
                delay(20)
                eyeScale.animateTo(1f, tween(100, easing = EaseOut))
                if (i < times - 1) delay(140)
            }
        }
    }

    Canvas(modifier.size(size)) {
        val t = frameNanos / 1e9
        val w = this.size.width
        val s = sin(t * 1.3).toFloat()
        val dy = s * w * 0.035f * floatAmount
        val dx = sin(t * 0.8 + 1.2).toFloat() * w * 0.02f * floatAmount
        val rot = sin(t * 0.9 + 0.5).toFloat() * 1.5f * floatAmount

        // Ground glow
        val glowScale = 1 + 0.10f * s * floatAmount
        val glowSize = Size(w * 0.46f * glowScale, w * 0.07f * glowScale)
        drawOval(
            color = glowColor.copy(alpha = 0.14f + 0.04f * s),
            topLeft = Offset(center.x - glowSize.width / 2, center.y + w * 0.46f - glowSize.height / 2),
            size = glowSize,
        )

        translate(dx, dy) {
            rotate(rot) {
                drawRobot(w, eyeScale.value)
            }
        }
    }
}

private fun DrawScope.drawRobot(w: Float, eyeScale: Float) {
    // Ears
    for (dir in listOf(-1f, 1f)) {
        drawPanel(rectAt(dir * w * 0.44f, w * 0.03f, w * 0.10f, w * 0.22f), w * 0.05f, w * 0.01f)
    }
    // Antenna base
    drawPanel(rectAt(0f, -w * 0.37f, w * 0.15f, w * 0.14f), w * 0.06f, w * 0.01f)

    // Head, with the soft blue drop shadow of the iOS version
    val head = rectAt(0f, 0f, w * 0.84f, w * 0.70f)
    drawIntoCanvas { canvas ->
        val paint = Paint().apply {
            color = Color.White
            asFrameworkPaint().setShadowLayer(w * 0.10f, 0f, w * 0.05f, glowColor.copy(alpha = 0.25f).toArgb())
        }
        canvas.drawRoundRect(head.left, head.top, head.right, head.bottom, w * 0.32f, w * 0.32f, paint)
    }
    drawPanel(head, w * 0.32f, w * 0.012f)

    // Visor
    val visor = rectAt(0f, w * 0.02f, w * 0.70f, w * 0.50f)
    val visorRadius = CornerRadius(w * 0.25f)
    drawRoundRect(
        brush = Brush.verticalGradient(listOf(Color(0xFF2B2B2B), Color(0xFF0D0D0D)), visor.top, visor.bottom),
        topLeft = visor.topLeft,
        size = visor.size,
        cornerRadius = visorRadius,
    )
    val rim = Brush.sweepGradient(rimColors, visor.center)
    // Wide faint pass stands in for the blurred rim glow.
    drawInsetStroke(visor, visorRadius, rim, w * 0.05f, alpha = 0.35f)
    drawInsetStroke(visor, visorRadius, rim, w * 0.02f)

    // Eyes
    for (dir in listOf(-1f, 1f)) {
        val eyeW = w * 0.085f
        val eyeH = w * 0.17f * eyeScale
        val x = dir * (w * 0.07f + eyeW / 2)
        drawRoundRect(
            color = eyeColor.copy(alpha = 0.35f),
            topLeft = rectAt(x, w * 0.02f, eyeW * 1.5f, eyeH + eyeW * 0.5f).topLeft,
            size = Size(eyeW * 1.5f, eyeH + eyeW * 0.5f),
            cornerRadius = CornerRadius(eyeW),
        )
        val eye = rectAt(x, w * 0.02f, eyeW, eyeH)
        drawRoundRect(eyeColor, eye.topLeft, eye.size, CornerRadius(eyeW / 2))
    }
}

/** Rect of the given size centered at an offset from the canvas center. */
private fun DrawScope.rectAt(dx: Float, dy: Float, width: Float, height: Float) =
    Rect(Offset(center.x + dx, center.y + dy) - Offset(width / 2, height / 2), Size(width, height))

private fun DrawScope.drawPanel(rect: Rect, radius: Float, border: Float) {
    drawRoundRect(
        brush = Brush.linearGradient(headColors, rect.topLeft, rect.bottomRight),
        topLeft = rect.topLeft,
        size = rect.size,
        cornerRadius = CornerRadius(radius),
    )
    drawInsetStroke(rect, CornerRadius(radius), Brush.linearGradient(listOf(outlineColor, outlineColor)), border, 0.8f)
}

/** SwiftUI `strokeBorder`: the stroke sits fully inside the shape. */
private fun DrawScope.drawInsetStroke(rect: Rect, radius: CornerRadius, brush: Brush, width: Float, alpha: Float = 1f) {
    val inset = width / 2
    drawRoundRect(
        brush = brush,
        topLeft = rect.topLeft + Offset(inset, inset),
        size = Size(rect.width - width, rect.height - width),
        cornerRadius = CornerRadius(radius.x - inset),
        alpha = alpha,
        style = Stroke(width),
    )
}
