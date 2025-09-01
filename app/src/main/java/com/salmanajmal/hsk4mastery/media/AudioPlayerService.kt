package com.salmanajmal.hsk4mastery.media

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import android.content.Context

@Singleton
class AudioPlayerService @Inject constructor(
    @ApplicationContext private val appContext: Context,
) {
    private val player: ExoPlayer by lazy { ExoPlayer.Builder(appContext).build() }

    fun setSpeed(speed: Float) {
        try {
            player.playbackParameters = PlaybackParameters(speed)
        } catch (_: Throwable) {}
    }

    fun playAsset(relativePath: String, speed: Float = 1.0f) {
        val path = if (relativePath.startsWith("asset:///")) relativePath else "asset:///$relativePath"
        val item = MediaItem.fromUri(Uri.parse(path))
        try {
            player.stop()
        } catch (_: Throwable) {}
        player.setMediaItem(item)
        player.prepare()
        setSpeed(speed)
        player.playWhenReady = true
    }

    fun playWord(hanzi: String, speed: Float = 1.0f) {
        val relative = "audio/words/${hanzi}.mp3"
        playAsset(relative, speed)
    }

    fun playSentence(hanzi: String, exampleIndex1Based: Int = 1, speed: Float = 1.0f) {
        val relative = "audio/sentences/${hanzi}_ex${exampleIndex1Based}.mp3"
        playAsset(relative, speed)
    }

    fun stop() {
        try { player.stop() } catch (_: Throwable) {}
    }

    fun release() {
        try { player.release() } catch (_: Throwable) {}
    }
}
