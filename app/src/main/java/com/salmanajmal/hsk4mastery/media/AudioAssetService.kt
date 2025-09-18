package com.salmanajmal.hsk4mastery.media

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resolves audio asset URIs for both Chinese (zh) and English (en) generated assets.
 * Expected asset structure under app/src/main/assets:
 *  - audio/words/{hanzi}.mp3
 *  - audio/sentences/{hanzi}_ex1.mp3
 *  - audio/meanings/{hanzi}.mp3 (if needed later)
 *  - audio/translations/{hanzi}_ex1.mp3 (if needed later)
 *  - audio_en/meanings/{hanzi}.mp3
 *  - audio_en/translations/{hanzi}_ex1.mp3
 */
@Singleton
class AudioAssetService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Build an asset Uri for an audio file. Language defaults to zh.
     * @param filename e.g. "我.mp3" or "我_ex1.mp3"
     * @param language "zh" or "en"
     * @param category optional category folder inside audio(_en) root (e.g., "meanings", "translations", "words", "sentences")
     */
    fun getAudioAssetUri(filename: String, language: String = "zh", category: String? = null): Uri {
        val root = if (language.lowercase() == "en") "audio_en" else "audio"
        val path = if (category.isNullOrBlank()) "$root/$filename" else "$root/$category/$filename"
        // Validate existence (best-effort); if missing we still return a Uri so caller can handle error on playback
        try { context.assets.openFd(path).close() } catch (_: Throwable) {}
        return Uri.parse("asset:///$path")
    }
}
