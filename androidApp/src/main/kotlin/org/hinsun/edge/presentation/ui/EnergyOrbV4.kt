//
// File        : EnergyOrbV4.kt
// Author      : Tran Van Hoai
// Date        : 21/10/26
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

/**
 * Orb v4: plays `assets/energy_orb.lottie` (same file as iosApp, white smoke on transparent) on a loop.
 * Its image assets must stay marked embedded (`"e": 1`), see the note in iosApp `EnergyOrbV4.swift`.
 *
 * The library's `DotLottieAnimation` composable parses the file (5.9MB: JSON + 90 image frames) on the
 * main thread every time it enters composition, which froze the overlay for ~1s on each open. Like
 * iOS, one player is loaded off the main thread via [preloadEnergyOrbV4] and reused; frames are
 * software-rendered into a shared bitmap on each display frame.
 */
@Composable
fun EnergyOrbV4(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var orb by remember { mutableStateOf(shared) }
    var frame by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        // Opened before preload finished: wait for it instead of blocking the UI.
        val loaded = orb ?: loadEnergyOrbV4(context).also { orb = it }
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
        frame // Redraw on every rendered frame without recomposing.
        orb?.let { drawImage(it.image, dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt())) }
    }
}

/** Call early (e.g. when the screen showing the assistant appears) so the first tap opens the orb instantly. */
suspend fun preloadEnergyOrbV4(context: Context) {
    loadEnergyOrbV4(context)
}

private class LoadedOrb(val player: DotLottiePlayer, val bitmap: Bitmap) {
    val image: ImageBitmap = bitmap.asImageBitmap()
}

/** Render size, same as iosApp: the orb's frame in OrbOverlay is 200pt at most. */
private const val RENDER_SIZE_DP = 200

private var shared: LoadedOrb? = null
private val loadLock = Mutex()

private suspend fun loadEnergyOrbV4(context: Context): LoadedOrb = loadLock.withLock {
    shared ?: withContext(Dispatchers.Default) {
        val px = (RENDER_SIZE_DP * context.resources.displayMetrics.density).roundToInt()
        val bytes = context.assets.open("energy_orb.lottie").use { it.readBytes() }
        val player = DotLottiePlayer(Config(autoplay = true, loopAnimation = true))
        // The player renders straight into the bitmap's pixels, which stay locked for the app's lifetime.
        val bitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
        player.setSwTarget(DotLottieJni.nativeLockBitmapPixels(bitmap), px.toUInt(), px.toUInt())
        check(player.loadDotlottieData(bytes)) { "Failed to load energy_orb.lottie" }
        // Warm the first frame too, so the first open draws immediately.
        player.tick(System.nanoTime())
        DotLottieJni.nativeFlushBitmapPixels(bitmap)
        LoadedOrb(player, bitmap)
    }.also { shared = it }
}
