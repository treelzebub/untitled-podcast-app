package net.treelzebub.podcasts.service

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.media3.common.C.TIME_UNSET
import androidx.media3.common.DeviceInfo
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.Tracks
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.FlowCollector


fun Player.state(): PlayerState = PlayerStateImpl(this)

interface PlayerState {
    val player: Player

    val timeline: Timeline

    val mediaItemIndex: Int

    val tracks: Tracks

    val currentMediaItem: MediaItem?

    val mediaMetadata: MediaMetadata

    val playlistMetadata: MediaMetadata

    val isLoading: Boolean

    @get:Player.State
    val playbackState: Int

    @get:Player.PlaybackSuppressionReason
    val playbackSuppressionReason: Int

    val playerError: PlaybackException?

    val playbackParameters: PlaybackParameters

    val maxSeekToPreviousPosition: Long

    val volume: Float

    val deviceVolume: Int

    val isDeviceMuted: Boolean

    val deviceInfo: DeviceInfo

    suspend fun listenPosition(collector: FlowCollector<Long>)

    fun dispose()
}

private class PlayerStateImpl(override val player: Player) : PlayerState {

    override var timeline: Timeline by mutableStateOf(player.currentTimeline)
        private set

    override var mediaItemIndex: Int by mutableIntStateOf(player.currentMediaItemIndex)
        private set

    override var tracks: Tracks by mutableStateOf(player.currentTracks)
        private set

    override var currentMediaItem: MediaItem? by mutableStateOf(player.currentMediaItem)
        private set

    override var mediaMetadata: MediaMetadata by mutableStateOf(player.mediaMetadata)
        private set

    override var playlistMetadata: MediaMetadata by mutableStateOf(player.playlistMetadata)
        private set

    override var isLoading: Boolean by mutableStateOf(player.isLoading)
        private set

    @get:Player.State
    override var playbackState: Int by mutableIntStateOf(player.playbackState)
        private set

    @get:Player.PlaybackSuppressionReason
    override var playbackSuppressionReason: Int by mutableIntStateOf(player.playbackSuppressionReason)
        private set

    override var playerError: PlaybackException? by mutableStateOf(player.playerError)
        private set

    override var playbackParameters: PlaybackParameters by mutableStateOf(player.playbackParameters)
        private set


    override var maxSeekToPreviousPosition: Long by mutableLongStateOf(player.maxSeekToPreviousPosition)
        private set

    override var volume: Float by mutableFloatStateOf(player.volume)
        private set

    override var deviceVolume: Int by mutableIntStateOf(player.deviceVolume)
        private set

    override var isDeviceMuted: Boolean by mutableStateOf(player.isDeviceMuted)
        private set

    override var deviceInfo: DeviceInfo by mutableStateOf(player.deviceInfo)
        private set

    override suspend fun listenPosition(collector: FlowCollector<Long>) = listener.listenPosition(collector)

    override fun dispose() {
        listener.active = false
        player.removeListener(listener)
    }

    private val listener = object : Player.Listener {
        private val interval = 1000L
        private val state = this@PlayerStateImpl
        var active = true

        suspend fun listenPosition(collector: FlowCollector<Long>) {
            val initialDelay = interval - (player.currentPosition % interval)
            delay(initialDelay)

            while (active) {
                if (
                    player.isCommandAvailable(Player.COMMAND_GET_CURRENT_MEDIA_ITEM) &&
                    player.contentDuration != TIME_UNSET
                ) {
                    collector.emit(player.currentPosition)
                }
                delay(interval) // Update every second
            }
        }

        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            state.timeline = timeline
            state.mediaItemIndex = player.currentMediaItemIndex
        }

        override fun onTracksChanged(tracks: Tracks) {
            state.tracks = tracks
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            state.currentMediaItem = mediaItem
        }

        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            state.mediaMetadata = mediaMetadata
        }

        override fun onPlaylistMetadataChanged(mediaMetadata: MediaMetadata) {
            state.playlistMetadata = mediaMetadata
        }

        override fun onIsLoadingChanged(isLoading: Boolean) {
            state.isLoading = isLoading
        }

        override fun onPlaybackStateChanged(@Player.State playbackState: Int) {
            state.playbackState = playbackState
        }

        override fun onPlaybackSuppressionReasonChanged(playbackSuppressionReason: Int) {
            state.playbackSuppressionReason = playbackSuppressionReason
        }

        override fun onPlayerErrorChanged(error: PlaybackException?) {
            state.playerError = error
        }

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            state.mediaItemIndex = player.currentMediaItemIndex
        }

        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
            state.playbackParameters = playbackParameters
        }

        override fun onMaxSeekToPreviousPositionChanged(maxSeekToPreviousPositionMs: Long) {
            state.maxSeekToPreviousPosition = maxSeekToPreviousPositionMs
        }

        override fun onVolumeChanged(volume: Float) {
            state.volume = volume
        }

        override fun onDeviceInfoChanged(deviceInfo: DeviceInfo) {
            state.deviceInfo = deviceInfo
        }

        override fun onDeviceVolumeChanged(volume: Int, muted: Boolean) {
            state.deviceVolume = volume
            state.isDeviceMuted = muted
        }
    }

    init {
        listener.active = true
        player.addListener(listener)
    }
}
