package com.salmanajmal.hsk4mastery.media

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.PlaybackParameters
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioPlayerService @Inject constructor(
    @ApplicationContext private val appContext: Context,
) {
    private val dataSourceFactory by lazy { DefaultDataSource.Factory(appContext) }
    private var player: ExoPlayer? = null

    @Synchronized
    private fun ensurePlayer(): ExoPlayer {
        val existing = player
        if (existing != null) return existing
        val attrs = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()
        val newPlayer = ExoPlayer.Builder(appContext)
            .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
            .build()
            .apply {
                setAudioAttributes(attrs, /* handleAudioFocus= */ true)
            }
        player = newPlayer
        return newPlayer
    }

    fun setSpeed(speed: Float) {
        try {
            ensurePlayer().playbackParameters = PlaybackParameters(speed)
        } catch (_: Throwable) {}
    }

    /**
     * Core low-level playback: plays a file within a resolved asset path.
     * @param fullAssetPath relative path inside assets folder (e.g. audio/words/我.mp3)
     */
    @Synchronized
    private fun playInternal(fullAssetPath: String, speed: Float) {
        try {
            val p = ensurePlayer()
            // 1) PAUSE & STOP: Ensure playback is halted and state is reset
            try { p.pause() } catch (_: Throwable) {}
            try { p.stop() } catch (_: Throwable) {}

            // 2) CLEAR: Ensure no residual media items remain
            try { p.clearMediaItems() } catch (_: Throwable) {}

            // 3) SET NEW SOURCE: Validate asset exists and create media item
            // Validate existence; openFd works for uncompressed assets like MP3
            appContext.assets.openFd(fullAssetPath).use { _ -> }

            val uri = Uri.parse("asset:///$fullAssetPath")
            val mediaItem = MediaItem.fromUri(uri)

            // Use explicit ProgressiveMediaSource for MP3 assets
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(mediaItem)
            p.setMediaSource(mediaSource, /* startPositionMs= */ 0)

            // 4) CONFIGURE: Apply per-playback configuration such as speed
            p.playbackParameters = PlaybackParameters(speed)

            // 5) PREPARE & PLAY
            p.prepare()
            p.play()
        } catch (e: IOException) {
            Log.e("AudioPlayerService", "Could not find asset: $fullAssetPath", e)
        } catch (t: Throwable) {
            Log.e("AudioPlayerService", "Failed to play asset: $fullAssetPath", t)
        }
    }

    /**
     * New descriptive play method that selects the correct asset subfolder based on [audioType]
     * and expects [filename] to be the final file name (e.g., "我们.mp3", "我们_ex1.mp3", "我们_en.mp3").
     *
     * Asset layout (relative to src/main/assets or packaged assets):
     * - Chinese Word:        audio/words/{hanzi}.mp3
     * - Chinese Sentence:    audio/sentences/{hanzi}_ex1.mp3
     * - English Meaning:     audio/en_meanings/{hanzi}_en.mp3
     * - English Translation: audio/en_translations/{hanzi}_en_ex1.mp3
     */
    fun play(audioType: AudioType, filename: String, speed: Float = 1.0f) {
        val clean = filename.trim().removePrefix("/").replace("\\", "/")
        val folder = when (audioType) {
            AudioType.CHINESE_WORD -> "audio/words"
            AudioType.CHINESE_SENTENCE -> "audio/sentences"
            AudioType.ENGLISH_MEANING -> "audio/en_meanings"
            AudioType.ENGLISH_TRANSLATION -> "audio/en_translations"
        }
        val full = if (clean.startsWith("audio/")) clean else "$folder/$clean"
        playInternal(full, speed)
    }

    /**
     * Backward-compatible overload. Prefer using the AudioType-based variant.
     */
    @Deprecated("Use play(AudioType, filename, speed) instead")
    fun play(filename: String, isSentence: Boolean = false, speed: Float = 1.0f) {
        val clean = filename.trim().removePrefix("/").replace("\\", "/")
        val lower = clean.lowercase()
        val inferredType = when {
            // English specific patterns
            lower.contains("/en_meanings/") || lower.endsWith("_en.mp3") -> AudioType.ENGLISH_MEANING
            lower.contains("/en_translations/") || lower.contains("_en_ex") -> AudioType.ENGLISH_TRANSLATION
            // Sentence patterns
            isSentence || lower.contains("/sentences/") || lower.contains("_ex") -> AudioType.CHINESE_SENTENCE
            else -> AudioType.CHINESE_WORD
        }
        play(inferredType, clean, speed)
    }

    fun playWord(hanzi: String, speed: Float = 1.0f) {
        // Delegate to new API for consistency
        play(AudioType.CHINESE_WORD, filename = "$hanzi.mp3", speed = speed)
    }

    fun playSentence(hanzi: String, exampleIndex1Based: Int = 1, speed: Float = 1.0f) {
        play(AudioType.CHINESE_SENTENCE, filename = "${hanzi}_ex${exampleIndex1Based}.mp3", speed = speed)
    }

    fun playMeaning(hanzi: String, language: String = "en", speed: Float = 1.0f) {
        if (language.lowercase() == "en") {
            // New folder and naming scheme for English meanings
            play(AudioType.ENGLISH_MEANING, filename = "${hanzi}_en.mp3", speed = speed)
        } else {
            // Preserve old zh path if ever used
            playInternal("audio/meanings/${hanzi}.mp3", speed)
        }
    }

    fun playTranslation(hanzi: String, exampleIndex1Based: Int = 1, language: String = "en", speed: Float = 1.0f) {
        if (language.lowercase() == "en") {
            // New folder and naming scheme for English translations
            play(AudioType.ENGLISH_TRANSLATION, filename = "${hanzi}_en_ex${exampleIndex1Based}.mp3", speed = speed)
        } else {
            // Preserve old zh path if ever used
            playInternal("audio/translations/${hanzi}_ex${exampleIndex1Based}.mp3", speed)
        }
    }

    fun stop() {
        try { ensurePlayer().stop() } catch (_: Throwable) {}
    }

    fun release() {
        try { player?.release() } catch (_: Throwable) {}
        player = null
    }
}
