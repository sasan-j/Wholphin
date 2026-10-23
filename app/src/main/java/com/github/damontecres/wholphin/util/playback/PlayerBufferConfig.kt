package com.github.damontecres.wholphin.util.playback

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.LoadControl
import com.github.damontecres.wholphin.preferences.PlayerBufferSize

/**
 * Builds an ExoPlayer [LoadControl] for the given buffer preset.
 *
 * High-bitrate files (>100 Mbps) make time-only buffers unsafe on ~1 GB TVs:
 * 50s at 150 Mbps is ~900 MB. These presets therefore set an explicit
 * [DefaultLoadControl.Builder.setTargetBufferBytes] cap and keep
 * [DefaultLoadControl.Builder.setPrioritizeTimeOverSizeThresholds] false so the
 * size threshold wins when buffering high-bitrate content, while time thresholds
 * still allow a deeper buffer for lower bitrates.
 */
@OptIn(UnstableApi::class)
object PlayerBufferConfig {
    /** ~96 MiB — about 5s at 150 Mbps; enough to absorb short spikes without OOM. */
    const val LARGE_TARGET_BUFFER_BYTES = 96 * 1024 * 1024

    /** ~160 MiB — deeper buffer for very bursty/high-bitrate sources; use with care on 1 GB devices. */
    const val EXTRA_LARGE_TARGET_BUFFER_BYTES = 160 * 1024 * 1024

    fun createLoadControl(bufferSize: PlayerBufferSize): LoadControl =
        when (bufferSize) {
            PlayerBufferSize.BUFFER_LARGE -> {
                DefaultLoadControl
                    .Builder()
                    .setBufferDurationsMs(
                        /* minBufferMs = */ 50_000,
                        /* maxBufferMs = */ 120_000,
                        /* bufferForPlaybackMs = */ 2_500,
                        /* bufferForPlaybackAfterRebufferMs = */ 5_000,
                    ).setTargetBufferBytes(LARGE_TARGET_BUFFER_BYTES)
                    .setPrioritizeTimeOverSizeThresholds(false)
                    .build()
            }

            PlayerBufferSize.BUFFER_EXTRA_LARGE -> {
                DefaultLoadControl
                    .Builder()
                    .setBufferDurationsMs(
                        /* minBufferMs = */ 80_000,
                        /* maxBufferMs = */ 240_000,
                        /* bufferForPlaybackMs = */ 5_000,
                        /* bufferForPlaybackAfterRebufferMs = */ 10_000,
                    ).setTargetBufferBytes(EXTRA_LARGE_TARGET_BUFFER_BYTES)
                    .setPrioritizeTimeOverSizeThresholds(false)
                    .build()
            }

            PlayerBufferSize.BUFFER_AUTO,
            PlayerBufferSize.UNRECOGNIZED,
            -> DefaultLoadControl()
        }
}
