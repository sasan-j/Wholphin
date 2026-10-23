package com.github.damontecres.wholphin.util.profile

import com.github.damontecres.wholphin.preferences.AudioPassthroughPreferences
import com.github.damontecres.wholphin.preferences.PlaybackOverrides

/**
 * Codecs that have a user-facing passthrough toggle.
 * Codecs not listed here stay advertised whenever audio is not forced to stereo downmix.
 *
 * Both [Codec.Audio.DTS] and [Codec.Audio.DCA] are core-DTS codec names used by Jellyfin;
 * they follow the DTS toggle. DTS-HD MA is usually still tagged as dts/dca (profile only),
 * so it cannot be filtered separately in the DeviceProfile codec list — the DTS-HD setting
 * is kept for UI/eARC guidance. TrueHD / MLP require eARC.
 */
val audioPassthroughToggleCodecs: Map<String, (AudioPassthroughPreferences) -> Boolean> =
    mapOf(
        Codec.Audio.AC3 to { it.ac3 },
        Codec.Audio.EAC3 to { it.eac3 },
        Codec.Audio.DTS to { it.dts },
        Codec.Audio.DCA to { it.dts },
        Codec.Audio.TRUEHD to { it.truehd },
        Codec.Audio.MLP to { it.mlp },
    )

/**
 * ARC-friendly defaults: DD (AC3) and DTS fit basic HDMI ARC.
 * E-AC3 (DD+) is off by default — many AVRs only accept DD/DTS over ARC, not DD+.
 * Lossless / HD formats stay off so the server converts instead of advertising
 * direct-play that Android may over-report but ARC cannot carry.
 */
fun defaultAudioPassthroughPreferences(): AudioPassthroughPreferences =
    AudioPassthroughPreferences
        .newBuilder()
        .setAc3(true)
        .setEac3(false)
        .setDts(true)
        .setDtsHd(false)
        .setTruehd(false)
        .setMlp(false)
        .setConfigured(true)
        .build()

/**
 * Resolves effective passthrough prefs. Unconfigured installs (proto3 all-false) get ARC
 * defaults, respecting the legacy [PlaybackOverrides.ac3Supported] toggle when present.
 */
fun PlaybackOverrides.effectiveAudioPassthrough(): AudioPassthroughPreferences {
    val passthrough = audioPassthrough
    if (passthrough.configured) return passthrough
    return if (ac3Supported) {
        defaultAudioPassthroughPreferences()
    } else {
        defaultAudioPassthroughPreferences()
            .toBuilder()
            .setAc3(false)
            .setEac3(false)
            .build()
    }
}

fun PlaybackOverrides.allowedAudioCodecs(downMixAudio: Boolean = downmixStereo): Array<String> {
    if (downMixAudio) {
        return arrayOf(
            Codec.Audio.AAC,
            Codec.Audio.MP2,
            Codec.Audio.MP3,
        )
    }
    val passthrough = effectiveAudioPassthrough()
    return supportedAudioCodecs
        .filter { codec ->
            val toggle = audioPassthroughToggleCodecs[codec]
            toggle == null || toggle(passthrough)
        }.toTypedArray()
}

/** Ordered codecs for HLS MPEG-TS transcoding; first match the device allows wins. */
val hlsMpegTsAudioCodecs =
    arrayOf(
        Codec.Audio.AC3,
        Codec.Audio.EAC3,
        Codec.Audio.AAC,
        Codec.Audio.MP3,
    )

/** Ordered codecs for HLS fMP4 transcoding. */
val hlsFmp4AudioCodecs =
    arrayOf(
        Codec.Audio.AC3,
        Codec.Audio.EAC3,
        Codec.Audio.DTS,
        Codec.Audio.TRUEHD,
        Codec.Audio.AAC,
        Codec.Audio.MP3,
        Codec.Audio.ALAC,
        Codec.Audio.FLAC,
        Codec.Audio.OPUS,
    )

fun Array<String>.filterAllowed(allowed: Array<String>): Array<String> = filter { it in allowed }.toTypedArray()
