package net.treelzebub.podcasts.service

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C.TIME_UNSET
import androidx.media3.common.DeviceInfo
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.media3.common.text.CueGroup
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import java.util.Locale


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

    val availableCommands: Player.Commands

    val trackSelectionParameters: TrackSelectionParameters

    @get:Player.State
    val playbackState: Int

    val playWhenReady: Boolean

    @get:Player.PlaybackSuppressionReason
    val playbackSuppressionReason: Int

    val isPlaying: Boolean

    @get:Player.RepeatMode
    val repeatMode: Int

    val shuffleModeEnabled: Boolean

    val playerError: PlaybackException?

    val playbackParameters: PlaybackParameters

    val seekBackIncrement: Long

    val seekForwardIncrement: Long

    val maxSeekToPreviousPosition: Long

    val audioAttributes: AudioAttributes

    val volume: Float

    val deviceInfo: DeviceInfo

    val deviceVolume: Int

    val isDeviceMuted: Boolean

    val videoSize: VideoSize

    val cues: CueGroup

    suspend fun listenPosition(collector: FlowCollector<String>)

    fun dispose()
}

private class PlayerStateImpl(override val player: Player) : PlayerState {

    override suspend fun listenPosition(collector: FlowCollector<String>) = listener.listenPosition(collector)

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

    override var availableCommands: Player.Commands by mutableStateOf(player.availableCommands)
        private set

    override var trackSelectionParameters: TrackSelectionParameters by mutableStateOf(player.trackSelectionParameters)
        private set

    @get:Player.State
    override var playbackState: Int by mutableIntStateOf(player.playbackState)
        private set

    override var playWhenReady: Boolean by mutableStateOf(player.playWhenReady)
        private set

    @get:Player.PlaybackSuppressionReason
    override var playbackSuppressionReason: Int by mutableIntStateOf(player.playbackSuppressionReason)
        private set

    override var isPlaying: Boolean by mutableStateOf(player.isPlaying)
        private set

    @get:Player.RepeatMode
    override var repeatMode: Int by mutableIntStateOf(player.repeatMode)
        private set

    override var shuffleModeEnabled: Boolean by mutableStateOf(player.shuffleModeEnabled)
        private set

    override var playerError: PlaybackException? by mutableStateOf(player.playerError)
        private set

    override var playbackParameters: PlaybackParameters by mutableStateOf(player.playbackParameters)
        private set

    override var seekBackIncrement: Long by mutableLongStateOf(player.seekBackIncrement)
        private set

    override var seekForwardIncrement: Long by mutableLongStateOf(player.seekForwardIncrement)
        private set

    override var maxSeekToPreviousPosition: Long by mutableLongStateOf(player.maxSeekToPreviousPosition)
        private set

    override var audioAttributes: AudioAttributes by mutableStateOf(player.audioAttributes)
        private set

    override var volume: Float by mutableFloatStateOf(player.volume)
        private set

    override var deviceInfo: DeviceInfo by mutableStateOf(player.deviceInfo)
        private set

    override var deviceVolume: Int by mutableIntStateOf(player.deviceVolume)
        private set

    override var isDeviceMuted: Boolean by mutableStateOf(player.isDeviceMuted)
        private set

    override var videoSize: VideoSize by mutableStateOf(player.videoSize)
        private set

    override var cues: CueGroup by mutableStateOf(player.currentCues)
        private set

    private val listener = object : Player.Listener {
        private val state = this@PlayerStateImpl

        private var active = true
        private val positionFlow = flow {
            while (active) {
                //format

                emit(player.currentPosition)
                delay(300)
            }
        }

        suspend fun listenPosition(collector: FlowCollector<String>) {
            positionFlow.collect {
                val initialDelay = 1000 - (player.currentPosition % 1000)
                delay(initialDelay)

                while (true) {
                    val str = if (
                        !player.isCommandAvailable(Player.COMMAND_GET_CURRENT_MEDIA_ITEM) ||
                        player.contentDuration == TIME_UNSET
                    ) {
                        "00:00 / 00:00"
                    } else {
                        formatPosition(player.currentPosition, player.contentDuration)
                    }
                    collector.emit(str)
                    delay(1000) // Update every second
                }
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

        override fun onAvailableCommandsChanged(availableCommands: Player.Commands) {
            state.availableCommands = availableCommands
        }

        override fun onTrackSelectionParametersChanged(parameters: TrackSelectionParameters) {
            state.trackSelectionParameters = parameters
        }

        override fun onPlaybackStateChanged(@Player.State playbackState: Int) {
            state.playbackState = playbackState
        }

        override fun onPlayWhenReadyChanged(
            playWhenReady: Boolean,
            @Player.PlayWhenReadyChangeReason reason: Int
        ) {
            state.playWhenReady = playWhenReady
        }

        override fun onPlaybackSuppressionReasonChanged(playbackSuppressionReason: Int) {
            state.playbackSuppressionReason = playbackSuppressionReason
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            state.isPlaying = isPlaying
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            state.repeatMode = repeatMode
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            state.shuffleModeEnabled = shuffleModeEnabled
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

        override fun onSeekBackIncrementChanged(seekBackIncrementMs: Long) {
            state.seekBackIncrement = seekBackIncrementMs
        }

        override fun onSeekForwardIncrementChanged(seekForwardIncrementMs: Long) {
            state.seekForwardIncrement = seekForwardIncrementMs
        }

        override fun onMaxSeekToPreviousPositionChanged(maxSeekToPreviousPositionMs: Long) {
            state.maxSeekToPreviousPosition = maxSeekToPreviousPositionMs
        }

        override fun onAudioAttributesChanged(audioAttributes: AudioAttributes) {
            state.audioAttributes = audioAttributes
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

        override fun onVideoSizeChanged(videoSize: VideoSize) {
            state.videoSize = videoSize
        }

        override fun onCues(cues: CueGroup) {
            state.cues = cues
        }
    }

    init {
        player.addListener(listener)
    }

    override fun dispose() {
        player.removeListener(listener)
    }
}

private fun formatPosition(current: Long, total: Long): String {
    val cHours = (current / (1000 * 60 * 60)) % 24
    val cMins = (current / (1000 * 60)) % 60
    val cSecs = (current / 1000) % 60

    val tHours = (total / (1000 * 60 * 60)) % 24
    val tMins = (total / (1000 * 60)) % 60
    val tSecs = (total / 1000) % 60

    return when {
        tHours > 0 -> String.format(
            Locale.getDefault(),
            "%02d:%02d:%02d / %02d:%02d:%02d",
            cHours,
            cMins,
            cSecs,
            tHours,
            tMins,
            tSecs
        )

        else -> String.format(Locale.getDefault(), "%02d:%02d / %02d:%02d", cMins, cSecs, tMins, tSecs)
    }
}
