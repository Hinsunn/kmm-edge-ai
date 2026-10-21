//
// File        : FloatingAssistant.kt
// Author      : Tran Van Hoai
// Date        : 21/10/26
//

package org.hinsun.edge.presentation.ui

import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Port of iosApp `FloatingAssistant`: a draggable [RobotMascot] that snaps to the nearest side on
 * release and remembers its side and height across launches (`@AppStorage` on iOS).
 */
@Composable
fun FloatingAssistant(
    size: Dp = 64.dp,
    margin: Dp = 6.dp,
    topReserved: Dp = 8.dp,
    bottomReserved: Dp = 90.dp,
    onTap: () -> Unit = {},
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("floatingAssistant", Context.MODE_PRIVATE) }
    var isLeft by remember { mutableStateOf(prefs.getString("side", "right") == "left") }
    var yRatio by remember { mutableFloatStateOf(prefs.getFloat("yRatio", 0.65f)) }
    var isDragging by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isDragging) 1.12f else 1f, swiftSpring(0.2f, 1f))

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val insets = WindowInsets.safeDrawing
        // Bounds of the mascot's top-left corner, in px.
        val sizePx = with(density) { size.toPx() }
        val minX = with(density) { margin.toPx() }
        val maxX = constraints.maxWidth - minX - sizePx
        val minY = insets.getTop(density) + with(density) { topReserved.toPx() }
        val maxY = max(minY, constraints.maxHeight - insets.getBottom(density) - with(density) { bottomReserved.toPx() } - sizePx)

        val baseX = if (isLeft) minX else maxX
        val baseY = minY + yRatio.coerceIn(0f, 1f) * (maxY - minY)

        val position = remember { Animatable(Offset(baseX, baseY), Offset.VectorConverter) }
        val scope = rememberCoroutineScope()

        // Settles after a drag, and follows size changes such as rotation.
        LaunchedEffect(baseX, baseY) {
            if (!isDragging) {
                position.animateTo(Offset(baseX, baseY), swiftSpring(0.45f, 0.72f))
            }
        }

        RobotMascot(
            size = size,
            modifier = Modifier
                .offset { IntOffset(position.value.x.roundToInt(), position.value.y.roundToInt()) }
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .pointerInput(Unit) { detectTapGestures { onTap() } }
                .pointerInput(minX, maxX, minY, maxY) {
                    val velocity = VelocityTracker()
                    detectDragGestures(
                        onDragStart = {
                            isDragging = true
                            velocity.resetTracking()
                        },
                        onDrag = { change, amount ->
                            change.consume()
                            val next = Offset(
                                (position.value.x + amount.x).coerceIn(minX, maxX),
                                (position.value.y + amount.y).coerceIn(minY, maxY),
                            )
                            // Track in screen space: the pointer's local position moves with the mascot.
                            velocity.addPosition(change.uptimeMillis, next)
                            scope.launch { position.snapTo(next) }
                        },
                        onDragEnd = {
                            // Like SwiftUI's predictedEndTranslation: a fling picks the side it heads to.
                            val projectedX = position.value.x + velocity.calculateVelocity().x * 0.2f
                            isLeft = projectedX + sizePx / 2 < constraints.maxWidth / 2f
                            yRatio = if (maxY > minY) (position.value.y - minY) / (maxY - minY) else 0f
                            prefs.edit {
                                putString("side", if (isLeft) "left" else "right")
                                putFloat("yRatio", yRatio)
                            }
                            isDragging = false
                            scope.launch {
                                position.animateTo(
                                    Offset(if (isLeft) minX else maxX, minY + yRatio * (maxY - minY)),
                                    swiftSpring(0.45f, 0.72f),
                                )
                            }
                        },
                        onDragCancel = { isDragging = false },
                    )
                },
        )
    }
}
