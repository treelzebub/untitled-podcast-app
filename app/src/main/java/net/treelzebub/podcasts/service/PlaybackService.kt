package net.treelzebub.podcasts.service

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
<<<<<<< Updated upstream
=======
import dagger.hilt.android.AndroidEntryPoint
import net.treelzebub.podcasts.data.PodcastsRepo
>>>>>>> Stashed changes
import net.treelzebub.podcasts.util.DeviceApi
import timber.log.Timber


@UnstableApi
class PlaybackService : MediaSessionService() {

    private companion object {
        const val NOTIF_ID = 0xd00d
        const val NOTIF_CHANNEL = "media.podspispops"
        const val SESSION_INTENT_REQUEST_CODE = 0xf00d
    }

<<<<<<< Updated upstream
=======
    @Inject
    lateinit var repo: PodcastsRepo

>>>>>>> Stashed changes
    private var _session: MediaSession? = null
    private val session: MediaSession
        get() = _session!!

    override fun onCreate() {
        super.onCreate()
        val intent = packageManager!!.getLaunchIntentForPackage(packageName)!!
            .let { sessionIntent ->
                PendingIntent.getActivity(this, SESSION_INTENT_REQUEST_CODE, sessionIntent, PendingIntent.FLAG_IMMUTABLE)
            }
        _session = MediaSession.Builder(this, buildPlayer())
            .setSessionActivity(intent)
            .build()
        setListener(PlaybackServiceListener())
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = _session

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = session.player
        if (!player.playWhenReady || player.mediaItemCount == 0) stopSelf()
    }

    override fun onDestroy() {
        session.run {
            player.release()
            release()
            _session = null
        }
        clearListener()
<<<<<<< Updated upstream
=======
        repo.cancelScope()
>>>>>>> Stashed changes
        super.onDestroy()
    }

    private fun buildPlayer(): Player {
        return ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                    .setUsage(C.USAGE_MEDIA)
                    .build(), true)
            .setSeekBackIncrementMs(10L)
            .setSeekForwardIncrementMs(5L)
            .build()
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
