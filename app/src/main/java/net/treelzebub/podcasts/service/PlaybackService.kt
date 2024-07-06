package net.treelzebub.podcasts.service

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import androidx.annotation.Px
import androidx.compose.runtime.Stable
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSession.MediaItemsWithStartPosition
import androidx.media3.session.MediaSessionService
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import net.treelzebub.podcasts.util.DeviceApi
import timber.log.Timber


@UnstableApi
class PlaybackService : MediaSessionService() {

    private companion object {
        const val NOTIF_ID = 0xd00d
        const val NOTIF_CHANNEL = "media.podspispops"
        @Px const val NOTIF_ICON_SIZE = 144
        const val SESSION_INTENT_REQUEST_CODE = 0xf00d
    }

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
            .setCallback(PlaybackSessionCallback())
            .setSessionActivity(intent)
            .build()
        setListener(PlaybackServiceListener())
        Timber.d("Service creation complete.")
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        Timber.d("onGetSession")
        return _session
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = session.player
        if (!player.playWhenReady || player.mediaItemCount == 0) stopSelf()
    }

    override fun onDestroy() {
        Timber.d("onDestroy")
        session.run {
            player.release()
            release()
            _session = null
        }
        clearListener()
        super.onDestroy()
    }

    private fun buildPlayer(): Player {
        return ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                    .setUsage(C.USAGE_MEDIA)
                    .build(), true
            ).build()
    }

    @Stable
    data class MediaItemsState(
        val list: List<MediaItem> = emptyList(),
        val startIndex: Int = 0,
        val startPositionMs: Long = 0L
    )

    private inner class PlaybackSessionCallback : MediaSession.Callback {

        private val mediaItemsState = MutableStateFlow(MediaItemsState())

        override fun onPlaybackResumption(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo
        ): ListenableFuture<MediaItemsWithStartPosition> {
            Timber.d("onPlaybackResumption")
            return with(mediaItemsState.value) {
                Futures.immediateFuture(MediaItemsWithStartPosition(list, startIndex, startPositionMs))
            }
        }

//        override fun onAddMediaItems(
//            mediaSession: MediaSession,
//            controller: MediaSession.ControllerInfo,
//            mediaItems: MutableList<MediaItem>
//        ): ListenableFuture<MutableList<MediaItem>> {
//            Timber.d("onAddMediaItems: $mediaItems")
//            mediaItemsState.update {
//                // todo where the hell do startIndex and startPositionMs come from?
//                it.copy(list = mediaItems)
//            }
//            return super.onAddMediaItems(mediaSession, controller, mediaItems)
//        }
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
