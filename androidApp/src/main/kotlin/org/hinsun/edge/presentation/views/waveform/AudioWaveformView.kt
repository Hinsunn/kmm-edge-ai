//
// File        : AudioWaveformView.kt
// Author      : Tran Van Hoai
// Date        : 21/10/26
//

package org.hinsun.edge.presentation.views.waveform

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.hinsun.edge.core.audio.AudioWaveformMonitor
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min

@Composable
fun AudioWaveformView() {
    val monitor = AudioWaveformMonitor
    val context = LocalContext.current
    val isMonitoring by monitor.isMonitoring.collectAsStateWithLifecycle()
    val magnitudes by monitor.fftMagnitudes.collectAsStateWithLifecycle()
    var permissionDenied by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        permissionDenied = !granted
        if (granted) monitor.startMonitoring()
    }

    // Unlike iOS the mic is released when leaving the tab, so it isn't held open in the background.
    DisposableEffect(Unit) {
        onDispose { monitor.stopMonitoring() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "Live Audio Waveform",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 16.dp),
        )

        Spacer(Modifier.weight(1f))

        WaveformBars(
            levels = magnitudes
                .filterIndexed { index, _ -> index % AudioWaveformMonitor.DOWNSAMPLE_FACTOR == 0 }
                .map { it / AudioWaveformMonitor.MAGNITUDE_LIMIT },
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(16.dp),
        )

        Spacer(Modifier.weight(1f))

        if (permissionDenied) {
            Text(
                "Microphone access denied",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }

        Button(
            onClick = {
                when {
                    isMonitoring -> monitor.stopMonitoring()
                    ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                            PackageManager.PERMISSION_GRANTED -> monitor.startMonitoring()
                    else -> permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isMonitoring) Color.Red else Color(0xFF007AFF),
                contentColor = Color.White,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(56.dp),
        ) {
            Icon(
                if (isMonitoring) Icons.Filled.Stop else Icons.Filled.GraphicEq,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                if (isMonitoring) "Stop" else "Start",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

/**
 * Bar chart that eases each bar toward its latest level every display frame.
 * The mic only delivers ~5 FFT updates per second, so animating per frame in a Canvas
 * keeps the motion smooth. Only the frame clock is state, so each tick redraws without recomposing.
 */
@Composable
private fun WaveformBars(
    levels: List<Float>,
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(Color(0xFFFF2D55), Color(0xFFAF52DE), Color(0xFF007AFF)),
) {
    val state = remember { BarState() }
    var frameNanos by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) withFrameNanos { frameNanos = it }
    }

    Canvas(modifier) {
        val heights = state.step(levels, frameNanos)
        if (heights.isEmpty()) return@Canvas
        val slot = size.width / heights.size
        val barWidth = slot * 0.6f
        val brush = Brush.verticalGradient(colors, startY = 0f, endY = size.height)
        heights.forEachIndexed { i, h ->
            val barHeight = max(2.dp.toPx(), h * size.height)
            drawRoundRect(
                brush = brush,
                topLeft = Offset(i * slot + (slot - barWidth) / 2, size.height - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(1.dp.toPx()),
            )
        }
    }
}

/** Per-frame smoothing state; a plain class so updating it while drawing doesn't recompose. */
private class BarState {
    private var heights = FloatArray(0)
    private var last = 0L

    fun step(targets: List<Float>, now: Long): FloatArray {
        if (heights.size != targets.size) heights = FloatArray(targets.size)
        val dt = if (last == 0L) 0f else min((now - last) / 1e9f, 0.1f)
        last = now
        for (i in heights.indices) {
            val target = targets[i].coerceIn(0f, 1f)
            // Rise fast, fall slower, like a VU meter. Frame-rate independent.
            val rate = if (target > heights[i]) 20f else 8f
            heights[i] += (target - heights[i]) * (1 - exp(-rate * dt))
        }
        return heights
    }
}
