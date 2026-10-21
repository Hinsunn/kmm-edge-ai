//
// File        : OrbOverlay.kt
// Author      : Tran Van Hoai
// Date        : 21/10/26
//

package org.hinsun.edge.presentation.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.pow

/** SwiftUI `.spring(response:dampingFraction:)` expressed as a Compose spring. */
internal fun <T> swiftSpring(response: Float, damping: Float) =
    spring<T>(dampingRatio = damping, stiffness = (2 * PI.toFloat() / response).pow(2))

/**
 * Port of iosApp `OrbOverlay`: dims the screen, pops the orb in with ripples, breathes, and closes on
 * tap, back, or after [durationMillis]. [orb] picks the orb version (v1 shader or v4 dotLottie).
 */
@Composable
fun OrbOverlay(
    durationMillis: Long = 10_000,
    orbSize: Dp = 120.dp,
    orb: @Composable (Modifier) -> Unit = { EnergyOrbV4(it) },
    onFinish: () -> Unit,
) {
    val bgOpacity = remember { Animatable(0f) }
    val orbScale = remember { Animatable(0.3f) }
    val orbOpacity = remember { Animatable(0f) }
    val breath = remember { Animatable(1f) }
    var isClosing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    suspend fun close() {
        if (isClosing) return
        isClosing = true
        coroutineScope {
            launch { bgOpacity.animateTo(0f, tween(350, easing = EaseIn)) }
            launch { orbScale.animateTo(0.6f, tween(350, easing = EaseIn)) }
            launch { orbOpacity.animateTo(0f, tween(350, easing = EaseIn)) }
        }
        onFinish()
    }

    BackHandler { scope.launch { close() } }

    LaunchedEffect(Unit) {
        // 1. Animation Opacity Background
        launch { bgOpacity.animateTo(0.85f, tween(400, easing = EaseOut)) }

        // 2. Scale and Opacity Orb
        launch {
            delay(150)
            launch { orbScale.animateTo(1f, swiftSpring(0.5f, 0.75f)) }
            orbOpacity.animateTo(1f, swiftSpring(0.5f, 0.75f))
        }

        // 3. Breathing: ease 1.0 -> 0.9, then loop 0.9 <-> 1.1
        launch {
            delay(650)
            breath.animateTo(0.9f, tween(800, easing = EaseInOut))
            breath.animateTo(1.1f, infiniteRepeatable(tween(1600, easing = EaseInOut), RepeatMode.Reverse))
        }

        // 4. Close after duration
        delay(durationMillis)
        close()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind { drawRect(Color.Black.copy(alpha = bgOpacity.value)) }
            .pointerInput(Unit) { detectTapGestures { scope.launch { close() } } },
        contentAlignment = Alignment.Center,
    ) {
        OrbRipples(
            startRadius = orbSize * 0.20f,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = orbOpacity.value.coerceIn(0f, 1f) },
        )

        orb(
            Modifier
                .size(orbSize)
                .graphicsLayer {
                    val scale = orbScale.value * breath.value
                    scaleX = scale
                    scaleY = scale
                    // The spring overshoots past 1.
                    alpha = orbOpacity.value.coerceIn(0f, 1f)
                }
        )
    }
}

private data class Ring(val radius: Float, val alpha: Float, val width: Float)

@Composable
private fun OrbRipples(
    startRadius: Dp,
    modifier: Modifier = Modifier,
    tint: Color = Color(0.80f, 0.82f, 0.86f), // silver
    lifetime: Float = 2.8f,
    interval: Float = 0.9f,
) {
    var elapsedNanos by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        val start = withFrameNanos { it }
        while (true) withFrameNanos { elapsedNanos = it - start }
    }

    Canvas(modifier) {
        val t = elapsedNanos / 1e9f
        if (t <= 0f) return@Canvas
        val start = startRadius.toPx()
        val maxRadius = hypot(size.width, size.height) * 0.5f
        val density = density

        // Fake glow with wide, faint strokes instead of a full-screen blur pass per frame.
        for (i in (t / interval).toInt() downTo 0) {
            val p = (t - i * interval) / lifetime
            if (p < 0f || p >= 1f) continue
            val eased = 1 - (1 - p).pow(3) // ease-out cubic: fast at the orb, slows far away
            val fadeIn = min(p / 0.08f, 1f) // emerge from the orb edge instead of popping
            val ring = Ring(
                radius = start + (maxRadius - start) * eased,
                alpha = fadeIn * (1 - p).pow(1.8f),
                width = (5 - 3.8f * eased) * density, // thins out as it spreads
            )
            drawRing(ring, tint.copy(alpha = ring.alpha * 0.10f), ring.width * 7)
            drawRing(ring, tint.copy(alpha = ring.alpha * 0.20f), ring.width * 3.5f)
            drawRing(ring, tint.copy(alpha = ring.alpha), ring.width)
            drawRing(ring, Color.White.copy(alpha = ring.alpha * 0.5f), ring.width * 0.35f)
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRing(ring: Ring, color: Color, width: Float) =
    drawCircle(color, radius = ring.radius, style = Stroke(width), blendMode = BlendMode.Plus)
