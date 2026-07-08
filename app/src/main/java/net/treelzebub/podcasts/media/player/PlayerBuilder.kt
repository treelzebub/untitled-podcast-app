package net.treelzebub.podcasts.media.player

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters

/**
 * Utility class to build ExoPlayer instances with standard configuration
 */
@OptIn(UnstableApi::class)
object PlayerBuilder {
    
    fun buildPlayer(context: Context, listener: Player.Listener): ExoPlayer {
        return ExoPlayer.Builder(context)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                    .setUsage(C.USAGE_MEDIA)
                    .build(), true
            )
            .setSeekBackIncrementMs(10_000L)
            .setSeekForwardIncrementMs(5_000L)
            .setHandleAudioBecomingNoisy(true)
            .setSeekParameters(SeekParameters.CLOSEST_SYNC)
            .build()
            .also { player ->
                player.addListener(listener)
            }
    }
}