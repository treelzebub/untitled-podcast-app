package net.treelzebub.podcasts.service

import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.Player.COMMAND_SEEK_TO_PREVIOUS
import androidx.media3.common.Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSession.ConnectionResult.AcceptedResultBuilder
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionCommand.COMMAND_CODE_SESSION_SET_RATING
import androidx.media3.session.SessionError
import androidx.media3.session.SessionResult
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import net.treelzebub.podcasts.R
import net.treelzebub.podcasts.service.PlaybackService.Companion.ACTION_BOOKMARK
import net.treelzebub.podcasts.service.PlaybackService.Companion.ACTION_NEXT
import net.treelzebub.podcasts.service.PlaybackService.Companion.ACTION_PLAYBACK_SPEED
import net.treelzebub.podcasts.service.PlaybackService.Companion.ACTION_PREVIOUS
import net.treelzebub.podcasts.service.PlaybackService.Companion.ACTION_SEEK_BACK
import net.treelzebub.podcasts.service.PlaybackService.Companion.ACTION_SEEK_FORWARD


private typealias CommandLayoutPair = Pair<MutableList<SessionCommand>, ImmutableList<CommandButton>>

@OptIn(UnstableApi::class)
class PodcastSessionCallback : MediaSession.Callback {

    override fun onConnect(session: MediaSession, controller: MediaSession.ControllerInfo): MediaSession.ConnectionResult {
        val connectionResult = super.onConnect(session, controller)
        val (sessionCommands, customLayout) = buildLayout(session.player)

        val result = AcceptedResultBuilder(session)
            .setAvailablePlayerCommands(connectionResult.availablePlayerCommands
                .buildUpon()
                .removeAll(COMMAND_SEEK_TO_PREVIOUS, COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                .build()
            )
            .setAvailableSessionCommands(connectionResult.availableSessionCommands
                .buildUpon()
                .remove(COMMAND_CODE_SESSION_SET_RATING)
                .addSessionCommands(sessionCommands)
                    .build())
            .setCustomLayout(customLayout)
            .build()
        return result
    }

    override fun onCustomCommand(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
        customCommand: SessionCommand,
        args: Bundle
    ): ListenableFuture<SessionResult> {
        val handled = when (customCommand.customAction) {
            ACTION_SEEK_BACK -> { session.player.seekBack(); true }
            ACTION_SEEK_FORWARD -> { session.player.seekForward(); true }
            ACTION_PREVIOUS -> { session.player.seekToPrevious(); true }
            ACTION_NEXT -> { session.player.seekToNext(); true }
            ACTION_PLAYBACK_SPEED -> {
                val currentSpeed = session.player.playbackParameters.speed
                val newSpeed = when (currentSpeed) {
                    0.5f -> 1f
                    1f -> 1.5f
                    1.5f -> 2f
                    else -> 0.5f
                }
                session.player.setPlaybackSpeed(newSpeed)
                session.setCustomLayout(buildLayout(session.player).second)
                true
            }
            ACTION_BOOKMARK -> {
                // TODO
                true
            }
            else -> false
        }
        val result = if (handled) SessionResult.RESULT_SUCCESS else SessionError.ERROR_BAD_VALUE
        return Futures.immediateFuture(SessionResult(result))
    }

    private fun buildLayout(player: Player): CommandLayoutPair {
        val currentSpeedIcon = when (val currentSpeed = player.playbackParameters.speed) {
            0.5f -> R.drawable.auto_0_5
            1f -> R.drawable.auto_1
            1.5f -> R.drawable.auto_1_5
            2f -> R.drawable.auto_2
            else -> throw IllegalStateException("Unhandled speed: $currentSpeed")
        }

        val seekBackCommand = SessionCommand(ACTION_SEEK_BACK, Bundle.EMPTY)
        val seekBackButton = CommandButton.Builder()
            .setDisplayName("seek back")
            .setSessionCommand(seekBackCommand)
            .setIconResId(R.drawable.notif_seek_back)
            .build()
        val prevCommand = SessionCommand(ACTION_PREVIOUS, Bundle.EMPTY)
        val prevButton = CommandButton.Builder()
            .setDisplayName("previous")
            .setSessionCommand(prevCommand)
            .setIconResId(androidx.media3.session.R.drawable.media3_icon_previous)
            .build()
        val nextCommand = SessionCommand(ACTION_NEXT, Bundle.EMPTY)
        val nextButton = CommandButton.Builder()
            .setDisplayName("next")
            .setSessionCommand(nextCommand)
            .setIconResId(androidx.media3.session.R.drawable.media3_icon_next)
            .build()
        val seekForwardCommand = SessionCommand(ACTION_SEEK_FORWARD, Bundle.EMPTY)
        val seekForwardButton = CommandButton.Builder()
            .setDisplayName("seek forward")
            .setSessionCommand(seekForwardCommand)
            .setIconResId(R.drawable.notif_seek_forward)
            .build()
        val playbackSpeedCommand = SessionCommand(ACTION_PLAYBACK_SPEED, Bundle.EMPTY)
        val playbackSpeedButton = CommandButton.Builder()
            .setDisplayName("playback speed")
            .setSessionCommand(playbackSpeedCommand)
            .setIconResId(currentSpeedIcon)
            .build()
        val bookmarkCommand = SessionCommand(ACTION_BOOKMARK, Bundle.EMPTY)
        val bookmarkButton = CommandButton.Builder()
            .setDisplayName("bookmark")
            .setSessionCommand(bookmarkCommand)
            .setIconResId(R.drawable.auto_star)
            .build()

        val sessionCommands = mutableListOf(
            seekBackCommand,
            prevCommand,
            //nextCommand,
            seekForwardCommand,
            playbackSpeedCommand,
            //bookmarkCommand
        )
        val customLayout = ImmutableList.of(
            seekBackButton,
            //prevButton,
            //nextButton,
            seekForwardButton,
            playbackSpeedButton,
            //bookmarkButton
        )

        return sessionCommands to customLayout
    }
}