//
// File        : AudioWaveformMonitor.kt
// Author      : Tran Van Hoai
// Date        : 21/10/26
//

package org.hinsun.edge.core.audio

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * Android port of iosApp `AudioWaveformMonitor`: reads the mic and publishes the magnitudes of the
 * lowest [SAMPLE_AMOUNT] frequency bins of each [BUFFER_SIZE]-sample buffer, same scale as vDSP's DFT.
 */
object AudioWaveformMonitor {

    /** a. Amount of frequency bins to keep after performing the FFT */
    const val SAMPLE_AMOUNT = 200

    /** b. Reduce the number of plotted points */
    const val DOWNSAMPLE_FACTOR = 8

    /** c. Handle high spikes distortion in the chart */
    const val MAGNITUDE_LIMIT = 100f

    private const val SAMPLE_RATE = 44_100
    private const val BUFFER_SIZE = 8192

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var job: Job? = null

    private val _isMonitoring = MutableStateFlow(false)
    val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()

    private val _fftMagnitudes = MutableStateFlow(FloatArray(SAMPLE_AMOUNT))
    val fftMagnitudes: StateFlow<FloatArray> = _fftMagnitudes.asStateFlow()

    /** Caller must hold RECORD_AUDIO; without it AudioRecord fails to initialize and this is a no-op. */
    fun startMonitoring() {
        if (job != null) return
        _isMonitoring.value = true
        // AudioRecord setup and startRecording block for a noticeable time; keep them off the caller (UI) thread.
        job = scope.launch {
            val record = createRecord() ?: run {
                _isMonitoring.value = false
                return@launch
            }
            record.startRecording()
            val buffer = FloatArray(BUFFER_SIZE)
            try {
                while (isActive) {
                    val read = record.read(buffer, 0, BUFFER_SIZE, AudioRecord.READ_BLOCKING)
                    if (read < 0) break
                    if (read > 0) _fftMagnitudes.value = performFFT(buffer, read)
                }
            } finally {
                record.stop()
                record.release()
                // Clear the visualization; set here so a buffer finishing after stop can't repaint it.
                _fftMagnitudes.value = FloatArray(SAMPLE_AMOUNT)
            }
        }
    }

    private fun createRecord(): AudioRecord? {
        val minBufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_FLOAT
        )
        if (minBufferSize <= 0) {
            Log.w("AudioWaveformMonitor", "No audio input available ($minBufferSize)")
            return null
        }

        val record = try {
            AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_FLOAT,
                // Bytes: room for a few buffers so reads never drop samples.
                max(minBufferSize, BUFFER_SIZE * Float.SIZE_BYTES * 2)
            )
        } catch (e: SecurityException) {
            Log.w("AudioWaveformMonitor", "Microphone access denied")
            return null
        }
        if (record.state != AudioRecord.STATE_INITIALIZED) {
            Log.w("AudioWaveformMonitor", "AudioRecord failed to initialize")
            record.release()
            return null
        }
        return record
    }

    fun stopMonitoring() {
        job?.cancel()
        job = null
        _isMonitoring.value = false
    }

    // ponytail: direct DFT of only the kept bins (200 x 8192 MACs per buffer, ~5 buffers/s, well under
    // a millisecond budget per frame on device); switch to a radix-2 FFT if SAMPLE_AMOUNT grows a lot.
    private fun performFFT(samples: FloatArray, count: Int): FloatArray =
        FloatArray(SAMPLE_AMOUNT) { k ->
            // Bin k of an N-point DFT; samples past `count` are the zero padding iOS adds.
            val angle = -2 * PI * k / BUFFER_SIZE
            val stepRe = cos(angle)
            val stepIm = sin(angle)
            var twiddleRe = 1.0
            var twiddleIm = 0.0
            var re = 0.0
            var im = 0.0
            for (n in 0 until count) {
                val sample = samples[n]
                re += sample * twiddleRe
                im += sample * twiddleIm
                val nextRe = twiddleRe * stepRe - twiddleIm * stepIm
                twiddleIm = twiddleRe * stepIm + twiddleIm * stepRe
                twiddleRe = nextRe
            }
            min(hypot(re, im).toFloat(), MAGNITUDE_LIMIT)
        }
}
