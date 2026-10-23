package com.github.damontecres.wholphin.util.playback

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.LoadControl

/**
 * Builds an ExoPlayer [LoadControl] for a user-selected target buffer size in MiB.
 *
 * High-bitrate files (>100 Mbps) make time-only buffers unsafe on ~1 GB TVs:
 * 50s at 150 Mbps is ~900 MB. When [targetBufferMiB] is > 0 we set an explicit
 * size cap and keep prioritize-time-over-size false so the size threshold wins
 * for high-bitrate content, while time thresholds still allow a deeper buffer
 * for lower bitrates.
 */
@OptIn(UnstableApi::class)
object PlayerBufferConfig {
    /** Media3 defaults (no custom LoadControl). */
    const val AUTO_MIB = 0

    /** Sensible default for high-bitrate spikes on ~1 GB TVs (~5s at 150 Mbps). */
    const val DEFAULT_TARGET_MIB = 96

    const val MIN_MIB = 0
    const val MAX_MIB = 192
    const val SLIDER_STEP_MIB = 16

    fun createLoadControl(targetBufferMiB: Int): LoadControl {
        val mib = targetBufferMiB.coerceIn(MIN_MIB, MAX_MIB)
        if (mib == AUTO_MIB) {
            return DefaultLoadControl()
        }
        return DefaultLoadControl
            .Builder()
            .setBufferDurationsMs(
                /* minBufferMs = */ 50_000,
                /* maxBufferMs = */ 120_000,
                /* bufferForPlaybackMs = */ 2_500,
                /* bufferForPlaybackAfterRebufferMs = */ 5_000,
            ).setTargetBufferBytes(mib * 1024 * 1024)
            .setPrioritizeTimeOverSizeThresholds(false)
            .build()
    }
}
