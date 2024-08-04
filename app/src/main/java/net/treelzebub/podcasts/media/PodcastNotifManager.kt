package net.treelzebub.podcasts.media

import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.annotation.Px
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.ui.PlayerNotificationManager
import androidx.media3.ui.PlayerNotificationManager.BitmapCallback
import androidx.media3.ui.PlayerNotificationManager.NotificationListener
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.Size
import com.google.common.util.concurrent.ListenableFuture
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.treelzebub.podcasts.R
import net.treelzebub.podcasts.di.IoDispatcher
import net.treelzebub.podcasts.di.MainDispatcher


@UnstableApi
class PodcastNotificationManager @AssistedInject constructor(
    app: Application,
    @Assisted sessionToken: SessionToken,
    @Assisted listener: NotificationListener,
    @MainDispatcher mainDispatcher: CoroutineDispatcher,
    @IoDispatcher ioDispatcher: CoroutineDispatcher
) {

    private companion object {

        const val NOTIF_ID = 0xd00d
        const val NOTIF_CHANNEL = "media.podspispops"

        @Px
        const val NOTIF_ICON_SIZE = 144
    }

    @AssistedFactory
    interface Factory {
        fun create(sessionToken: SessionToken, listener: NotificationListener): PodcastNotificationManager
    }

    private val job = SupervisorJob()
    private val scope = CoroutineScope(mainDispatcher + job)

    // todo migration: https://developer.android.com/media/media3/session/control-playback#automatic-state
//    private val playAction = NotificationCompat.Action.Builder(
//        R.drawable.notification_action_play,
//        app.getString(R.string.notif_play),
//        PendingIntent.getBroadcast(app, ACTION_PREVIOUS, )
//    ).extend(CarExtender).build()
//
//        private val pauseAction = NotificationCompat.Action.Builder(R.drawable.notif_pause, app.getString(R.string.notif_pause), MediaButtonReceiver.buildMediaButtonPendingIntent(app, ACTION_PAUSE))
//            .build()
//    private val skipBackAction = NotificationCompat.Action.Builder(R.drawable.notif_seek_back, app.getString(R.string.notif_seek_back), MediaButtonReceiver)
//    private val skipForwardAction = NotificationCompat.Action.Builder(R.drawable.notif_seek_forward, app.getString(R.string.notif_seek_forward), MediaButtonReceiver.buildMediaButtonPendingIntent(app, ACTION_SKIP_TO_NEXT))
//    private val stopPendingIntent = MediaButtonReceiver.buildMediaButtonPendingIntent(app, ACTION_STOP)
    private val customActionsReceiver = object : PlayerNotificationManager.CustomActionReceiver {
        override fun createCustomActions(context: Context, instanceId: Int): MutableMap<String, NotificationCompat.Action> {
            return mutableMapOf()
        }

        override fun getCustomActions(player: Player): MutableList<String> {
            TODO("Not yet implemented")
        }

        override fun onCustomAction(player: Player, action: String, intent: Intent) {
            TODO("Not yet implemented")
        }
    }
    private val controllerFuture = MediaController.Builder(app, sessionToken).buildAsync()
    private val manager = PlayerNotificationManager.Builder(app, NOTIF_ID, NOTIF_CHANNEL)
        .setChannelNameResourceId(R.string.media_notification_channel)
        .setChannelDescriptionResourceId(R.string.media_notification_channel_description)
        .setCustomActionReceiver(customActionsReceiver)
        .setPlayActionIconResourceId(R.drawable.notif_play)
        .setPauseActionIconResourceId(R.drawable.notif_pause)
        .setNextActionIconResourceId(R.drawable.notification_action_playnext_large) // TODO mdpi
        .setPreviousActionIconResourceId(R.drawable.notification_action_playlast_large) // TODO mdpi
        .setFastForwardActionIconResourceId(R.drawable.notif_seek_forward)
        .setRewindActionIconResourceId(R.drawable.notif_seek_back)
        .setMediaDescriptionAdapter(DescriptionAdapter(app, controllerFuture, scope, ioDispatcher))
        .setNotificationListener(listener)
        .setSmallIconResourceId(R.drawable.notification_action_play)
        .build().apply {
            setUsePlayPauseActions(true)
            setUseRewindAction(true)
            setUseFastForwardAction(true)
            setUseRewindActionInCompactView(true)
            setUseFastForwardActionInCompactView(true)
            setUseRewindActionInCompactView(true)
            setUseFastForwardActionInCompactView(true)
        }

    fun show(player: Player) = manager.setPlayer(player)

    fun hide() = manager.setPlayer(null)


    private inner class DescriptionAdapter(
        private val app: Application,
        private val controller: ListenableFuture<MediaController>,
        private val scope: CoroutineScope,
        private val ioDispatcher: CoroutineDispatcher
    ) : PlayerNotificationManager.MediaDescriptionAdapter {

        private val cache = mutableMapOf<Uri, Bitmap>()

        override fun createCurrentContentIntent(player: Player): PendingIntent? = controller.get().sessionActivity

        override fun getCurrentContentText(player: Player) = ""

        override fun getCurrentContentTitle(player: Player) = controller.get().mediaMetadata.title.toString()

        override fun getCurrentLargeIcon(player: Player, callback: BitmapCallback): Bitmap? {
            val iconUri = controller.get().mediaMetadata.artworkUri
            val existing = cache[iconUri]
            return if (existing == null) {
                scope.launch {
                    iconUri?.let { resolveUriAsBitmap(it, callback) }
                }
                null // TODO return placeholder
            } else existing
        }

        private suspend fun resolveUriAsBitmap(uri: Uri, callback: BitmapCallback): Bitmap? {
            return withContext(ioDispatcher) {
                val request = ImageRequest.Builder(app)
                    .data(uri)
                    .size(Size(NOTIF_ICON_SIZE, NOTIF_ICON_SIZE))
                    .build()
                app.imageLoader.execute(request).drawable?.toBitmap().also {
                    it?.let {
                        cache[uri] = it
                        callback.onBitmap(it)
                    }
                }
            }
        }
    }
}
