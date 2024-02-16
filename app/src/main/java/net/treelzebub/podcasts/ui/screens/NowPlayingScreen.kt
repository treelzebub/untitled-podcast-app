package net.treelzebub.podcasts.ui.screens

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.ui.PlayerControlView
import com.google.common.util.concurrent.MoreExecutors
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import net.treelzebub.podcasts.service.PlaybackService
import net.treelzebub.podcasts.ui.vm.NowPlayingViewModel

@Destination
@Composable
fun NowPlayingScreen(navigator: DestinationsNavigator, episodeId: String) {
    val vm = hiltViewModel<NowPlayingViewModel>()
    var lifecycle by remember { mutableStateOf(Lifecycle.Event.ON_CREATE) }
    val lifecycleOwner = LocalLifecycleOwner.current

    val context = LocalContext.current.applicationContext
    val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
    val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
    val state by remember { vm.state }.collectAsState()

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            lifecycle = event
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        AndroidView(
            factory = { context -> playerView(context) },
            update = { playerView ->
                Log.d("Test", "Lifecycle: $lifecycle")
                when (lifecycle) {
                    Lifecycle.Event.ON_START -> {
                        controllerFuture.addListener({
                            val player = controllerFuture.get()
                            player.prepare()
                            player.playWhenReady = true
                            playerView.player = player
                        }, MoreExecutors.directExecutor())
                    }
                    Lifecycle.Event.ON_RESUME -> {
                        controllerFuture.addListener({
                            val player = controllerFuture.get()
                            state.mediaItem?.let { item ->
                                if (player.currentMediaItem == item) return@addListener
                                player.setMediaItem(item)
                            }
                        }, MoreExecutors.directExecutor())
                    }
//                    Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> playerView.onWindowFocusChanged(false)
//                    Lifecycle.Event.ON_RESUME -> playerView.onWindowFocusChanged(true)
                    else -> Unit
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16 / 9f)
        )

        vm.play(episodeId)
    }
}

@OptIn(UnstableApi::class)
private fun playerView(context: Context): PlayerControlView {
    return PlayerControlView(context).apply {
        setShowPreviousButton(false)
        setShowNextButton(false)
        showTimeoutMs = 0
    }
}