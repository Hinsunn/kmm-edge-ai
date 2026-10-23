//
// File        : EnergyOrbV4V2V2.kt
// Author      : Tran Van Hoai
// Date        : 23/10/26
//

package org.hinsun.edge.presentation.ui

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import com.dotlottie.dlplayer.Config
import com.dotlottie.dlplayer.DotLottiePlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt
import com.lottiefiles.dotlottie.core.jni.DotLottiePlayer as DotLottieJni
import androidx.core.graphics.createBitmap

/** Test copy of [EnergyOrbV4] playing `energy_orb_v2.lottie` (glass orb, background keyed out). */
@Composable
fun EnergyOrbV4V2(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var orb by remember { mutableStateOf(shared) }
    var frame by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        // Opened before preload finished: wait for it instead of blocking the UI.
        val loaded = orb ?: loadEnergyOrbV4V2(context).also { orb = it }
        while (true) {
            withFrameNanos { now ->
                if (loaded.player.tick(now)) {
                    DotLottieJni.nativeFlushBitmapPixels(loaded.bitmap)
                    frame++
                }
            }
        }
    }

    Canvas(modifier.aspectRatio(1f)) {
        frame
        orb?.let {
            drawImage(
                it.image,
                dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt())
            )
        }
    }
}

/** Call early (e.g. when the screen showing the assistant appears) so the first tap opens the orb instantly. */
suspend fun preloadEnergyOrbV4V2(context: Context) {
    loadEnergyOrbV4V2(context)
}

private class LoadedOrbV2(val player: DotLottiePlayer, val bitmap: Bitmap) {
    val image: ImageBitmap = bitmap.asImageBitmap()
}

/** Render size, same as iosApp: the orb's frame in OrbOverlay is 200pt at most. */
private const val RENDER_SIZE_DP = 200

private var shared: LoadedOrbV2? = null
private val loadLock = Mutex()

private suspend fun loadEnergyOrbV4V2(context: Context): LoadedOrbV2 = loadLock.withLock {
    shared ?: withContext(Dispatchers.Default) {
        val px = (RENDER_SIZE_DP * context.resources.displayMetrics.density).roundToInt()
        val bytes = context.assets.open("energy_orb_v2.lottie").use { it.readBytes() }
        val player = DotLottiePlayer(Config(autoplay = true, loopAnimation = true))
        // The player renders straight into the bitmap's pixels, which stay locked for the app's lifetime.
        val bitmap = createBitmap(px, px)
        player.setSwTarget(DotLottieJni.nativeLockBitmapPixels(bitmap), px.toUInt(), px.toUInt())
        check(player.loadDotlottieData(bytes)) { "Failed to load energy_orb_v2.lottie" }
        // Warm the first frame too, so the first open draws immediately.
        player.tick(System.nanoTime())
        DotLottieJni.nativeFlushBitmapPixels(bitmap)
        LoadedOrbV2(player, bitmap)
    }.also { shared = it }
}
