package net.treelzebub.podcasts.service

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import androidx.compose.runtime.mutableLongStateOf
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.bundleOf
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import net.treelzebub.podcasts.data.PodcastsRepo
import net.treelzebub.podcasts.di.IoDispatcher
import net.treelzebub.podcasts.di.MainDispatcher
import net.treelzebub.podcasts.util.DeviceApi
import timber.log.Timber
import javax.inject.Inject


@UnstableApi
@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    companion object {
        const val KEY_EPISODE_ID = "episode-id"
        private const val NOTIF_ID = 0xd00d
        private const val NOTIF_CHANNEL = "media.podspispops"
        private const val SESSION_INTENT_REQUEST_CODE = 0xf00d
    }

    @Inject
    @IoDispatcher
    lateinit var ioDispatcher: CoroutineDispatcher

    @Inject
    @MainDispatcher
    lateinit var mainDispatcher: CoroutineDispatcher

    @Inject
    lateinit var repo: PodcastsRepo

    private val scope by lazy { CoroutineScope(SupervisorJob() + ioDispatcher) }
    private var _session: MediaSession? = null
    private val session: MediaSession
        get() = _session!!
    private val playbackPosition = mutableLongStateOf(0L)
    private val isPlayingListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (!isPlaying) persistPosition()
        }
    }

    override fun onCreate() {
        super.onCreate()
        val player = buildPlayer()
        val intent = packageManager!!.getLaunchIntentForPackage(packageName)!!
            .let { sessionIntent ->
                PendingIntent.getActivity(
                    this,
                    SESSION_INTENT_REQUEST_CODE,
                    sessionIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            }
        _session = MediaSession.Builder(this, player)
            .setSessionActivity(intent)
            .setSessionExtras(bundleOf())
            .build()
        setListener(PlaybackServiceListener())

        // Player must always be accessed from main thread
        scope.launch(mainDispatcher) {
            player.state().listenPosition {
                Timber.d("PlaybackService updated playback position: $it")
                playbackPosition.longValue = it
            }
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = _session

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = session.player
        if (!player.playWhenReady || player.mediaItemCount == 0) stopSelf()
    }

    override fun onDestroy() {
        session.run {
            player.release()
            player.removeListener(isPlayingListener)
            release()
            _session = null
        }
        clearListener()
        scope.cancel()
        repo.cancelScope()
        super.onDestroy()
    }

    private fun buildPlayer(): Player {
        return ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                    .setUsage(C.USAGE_MEDIA)
                    .build(), true
            )
            .setSeekBackIncrementMs(10L)
            .setSeekForwardIncrementMs(5L)
            .build()
            .also { it.addListener(isPlayingListener) }
    }

    private fun persistPosition() {
        val episodeId = session.sessionExtras.getString(KEY_EPISODE_ID)
            ?: throw IllegalStateException("Session has no episodeId in extras")
        repo.updatePosition(episodeId, playbackPosition.longValue)
    }

    private inner class PlaybackServiceListener : Listener {

        override fun onForegroundServiceStartNotAllowedException() {
            Timber.d("onForegroundServiceStartNotAllowedException!")
            if (DeviceApi.isMinTiramisu && checkSelfPermission(POST_NOTIFICATIONS) != PERMISSION_GRANTED) {
                Timber.e(IllegalStateException("Notif permission denied! No stairway!"))
                return
            }

            val notificationManagerCompat = NotificationManagerCompat.from(this@PlaybackService)
            ensureNotificationChannel(notificationManagerCompat)
            val builder =
                NotificationCompat.Builder(this@PlaybackService, NOTIF_CHANNEL)
                    // .setSmallIcon(androidx.media3.session.R.drawable.media3_notification_small_icon)
                    .setContentTitle("TEMP Notif Name")
                    .setStyle(NotificationCompat.BigTextStyle().bigText("TEMP Content Text"))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(false)
            notificationManagerCompat.notify(NOTIF_ID, builder.build())
        }

        private fun ensureNotificationChannel(notificationManagerCompat: NotificationManagerCompat) {
            if (notificationManagerCompat.getNotificationChannel(NOTIF_CHANNEL) != null) return
            val channel = NotificationChannel(NOTIF_CHANNEL, "TEMP Notif name", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManagerCompat.createNotificationChannel(channel)
        }
    }
}
