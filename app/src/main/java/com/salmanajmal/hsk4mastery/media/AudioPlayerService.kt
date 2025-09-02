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
     * Play an MP3 file bundled in the APK under app/src/main/assets/audio/{words|sentences}/...
     * @param filename File name including extension (e.g., "中国.mp3" or "中国_ex1.mp3")
     * @param isSentence Whether to look under the sentences subfolder (true) or words (false)
     * @param speed Playback speed
     */
    @Synchronized
    fun play(filename: String, isSentence: Boolean, speed: Float) {
        val subfolder = if (isSentence) "sentences" else "words"
        val fullAssetPath = "audio/$subfolder/$filename"
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

    fun playWord(hanzi: String, speed: Float = 1.0f) {
        play("${hanzi}.mp3", isSentence = false, speed = speed)
    }

    fun playSentence(hanzi: String, exampleIndex1Based: Int = 1, speed: Float = 1.0f) {
        play("${hanzi}_ex${exampleIndex1Based}.mp3", isSentence = true, speed = speed)
    }

    fun stop() {
        try { ensurePlayer().stop() } catch (_: Throwable) {}
    }

    fun release() {
        try { player?.release() } catch (_: Throwable) {}
        player = null
    }
}
