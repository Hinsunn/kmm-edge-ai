//
// File        : OrbOverlay.kt
// Author      : Tran Van Hoai
// Date        : 21/10/26
//

package org.hinsun.edge.presentation.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.hinsun.edge.core.audio.AudioWaveformMonitor
import org.hinsun.edge.presentation.views.waveform.BarState
import kotlin.math.PI
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.time.Duration.Companion.milliseconds

internal fun <T> swiftSpring(response: Float, damping: Float) =
    spring<T>(dampingRatio = damping, stiffness = (2 * PI.toFloat() / response).pow(2))

@Composable
fun OrbOverlay(
    durationMillis: Long = 10_000,
    orbSize: Dp = 160.dp,
    orb: @Composable (Modifier) -> Unit = { EnergyOrbV4(it) },
    onFinish: () -> Unit,
) {
    val bgOpacity = remember { Animatable(0f) }
    val orbScale = remember { Animatable(0.3f) }
    val orbOpacity = remember { Animatable(0f) }
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

    val context = LocalContext.current
    val magnitudes by AudioWaveformMonitor.fftMagnitudes.collectAsStateWithLifecycle()
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) AudioWaveformMonitor.startMonitoring()
        }
    DisposableEffect(Unit) {
        // startMonitoring returns immediately (setup runs in the background), so the overlay shows without delay
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            AudioWaveformMonitor.startMonitoring()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
        onDispose { AudioWaveformMonitor.stopMonitoring() }
    }

    LaunchedEffect(Unit) {
        // 1. Animation Opacity Background
        launch { bgOpacity.animateTo(0.85f, tween(400, easing = EaseOut)) }

        // 2. Scale and Opacity Orb
        launch {
            delay(150.milliseconds)
            launch { orbScale.animateTo(1f, swiftSpring(0.5f, 0.75f)) }
            orbOpacity.animateTo(1f, swiftSpring(0.5f, 0.75f))
        }

        // 3. Close after duration
        delay(durationMillis.milliseconds)
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

        // Bars grow outward from the orb edge, following the mic spectrum
        RadialBars(
            levels = magnitudes
                .filterIndexed { index, _ -> index % AudioWaveformMonitor.DOWNSAMPLE_FACTOR == 0 }
                .map { it / AudioWaveformMonitor.MAGNITUDE_LIMIT },
            innerRadius = orbSize * 0.55f,
            maxLength = orbSize * 0.6f,
            modifier = Modifier
                .size(orbSize * 3)
                .graphicsLayer {
                    scaleX = orbScale.value
                    scaleY = orbScale.value
                    alpha = orbOpacity.value.coerceIn(0f, 1f)
                },
        )

        orb(
            Modifier
                .size(orbSize)
                .graphicsLayer {
                    val scale = orbScale.value
                    scaleX = scale
                    scaleY = scale
                    alpha = orbOpacity.value.coerceIn(0f, 1f)
                }
        )
    }
}

private const val BAR_NOISE_FLOOR = 3.0f
private const val BAR_GAIN = 0.6f

private fun barLevel(magnitude: Float): Float {
    val x = (magnitude - BAR_NOISE_FLOOR)
        .coerceAtLeast(0f) / AudioWaveformMonitor.MAGNITUDE_LIMIT * BAR_GAIN
    return sqrt(x.coerceAtMost(1f))
}

@Composable
private fun RadialBars(
    levels: List<Float>,
    innerRadius: Dp,
    maxLength: Dp,
    modifier: Modifier = Modifier,
    tint: Color = Color(0.80f, 0.82f, 0.86f), // silver
) {
    val state = remember { BarState() }
    var frameNanos by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) withFrameNanos { frameNanos = it }
    }

    Canvas(modifier) {
        // Mirror the spectrum so the ring closes without a seam between high and low bins
        val heights = state.step(levels + levels.reversed(), frameNanos)
        if (heights.isEmpty()) return@Canvas
        val inner = innerRadius.toPx()
        val maxLen = maxLength.toPx()
        val step = 360f / heights.size
        val barWidth = maxOf(2.dp.toPx(), inner * (2 * PI.toFloat() / heights.size) * 0.45f)
        val corner = CornerRadius(1.dp.toPx())

        heights.forEachIndexed { i, h ->
            val length = 5.dp.toPx() + h * maxLen
            rotate(i * step - 90f, pivot = center) {
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(center.x + inner, center.y - barWidth / 2),
                    size = Size(length, barWidth),
                    cornerRadius = corner,
                )
            }
        }
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
            if (p !in 0f..<1f) continue
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

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRing(
    ring: Ring,
    color: Color,
    width: Float
) = drawCircle(color, radius = ring.radius, style = Stroke(width), blendMode = BlendMode.Plus)
