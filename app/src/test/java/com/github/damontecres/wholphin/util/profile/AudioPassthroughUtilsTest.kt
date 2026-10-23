package com.github.damontecres.wholphin.util.profile

import com.github.damontecres.wholphin.preferences.PlaybackOverrides
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AudioPassthroughUtilsTest {
    @Test
    fun `ARC defaults allow AC3 EAC3 and DTS but not TrueHD or DTS-HD`() {
        val overrides =
            PlaybackOverrides
                .newBuilder()
                .setAudioPassthrough(defaultAudioPassthroughPreferences())
                .build()

        val allowed = overrides.allowedAudioCodecs().toSet()
        assertTrue(Codec.Audio.AC3 in allowed)
        assertTrue(Codec.Audio.EAC3 in allowed)
        assertTrue(Codec.Audio.DTS in allowed)
        assertTrue(Codec.Audio.DCA in allowed)
        assertTrue(Codec.Audio.AAC in allowed)
        assertFalse(Codec.Audio.TRUEHD in allowed)
        assertFalse(Codec.Audio.MLP in allowed)
    }

    @Test
    fun `downmix stereo only allows stereo-safe codecs`() {
        val overrides =
            PlaybackOverrides
                .newBuilder()
                .setDownmixStereo(true)
                .setAudioPassthrough(defaultAudioPassthroughPreferences())
                .build()

        val allowed = overrides.allowedAudioCodecs().toSet()
        assertTrue(allowed == setOf(Codec.Audio.AAC, Codec.Audio.MP2, Codec.Audio.MP3))
    }

    @Test
    fun `enabling TrueHD advertises it for direct play`() {
        val overrides =
            PlaybackOverrides
                .newBuilder()
                .setAudioPassthrough(
                    defaultAudioPassthroughPreferences().toBuilder().setTruehd(true).build(),
                ).build()

        assertTrue(Codec.Audio.TRUEHD in overrides.allowedAudioCodecs().toSet())
    }

    @Test
    fun `unconfigured passthrough falls back to ARC defaults when legacy AC3 was enabled`() {
        val overrides =
            PlaybackOverrides
                .newBuilder()
                .setAc3Supported(true)
                .build()

        val allowed = overrides.allowedAudioCodecs().toSet()
        assertTrue(Codec.Audio.AC3 in allowed)
        assertTrue(Codec.Audio.DTS in allowed)
        assertFalse(Codec.Audio.TRUEHD in allowed)
    }
}
