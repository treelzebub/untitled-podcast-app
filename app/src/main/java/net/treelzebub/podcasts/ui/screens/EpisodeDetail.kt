package net.treelzebub.podcasts.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.treelzebub.podcasts.platform.RequestNotificationPermission
import net.treelzebub.podcasts.ui.components.LoadingBox
import net.treelzebub.podcasts.ui.vm.EpisodeDetailAction
import net.treelzebub.podcasts.ui.vm.EpisodeDetailAction.AddToQueue
import net.treelzebub.podcasts.ui.vm.EpisodeDetailAction.Archive
import net.treelzebub.podcasts.ui.vm.EpisodeDetailAction.Download
import net.treelzebub.podcasts.ui.vm.EpisodeDetailAction.PlayPause
import net.treelzebub.podcasts.ui.vm.EpisodeDetailAction.ToggleBookmarked
import net.treelzebub.podcasts.ui.vm.EpisodeDetailAction.ToggleHasPlayed
import net.treelzebub.podcasts.ui.vm.EpisodeDetailViewModel
import net.treelzebub.podcasts.util.DeviceApi
import java.util.Locale


@SuppressLint("UnsafeOptInUsageError")
@Destination
@Composable
fun EpisodeDetail(episodeId: String) {
    val vm = hiltViewModel<EpisodeDetailViewModel, EpisodeDetailViewModel.Factory>(
        creationCallback = { factory -> factory.create(episodeId) }
    )
    val uiState by remember { vm.uiState }.collectAsStateWithLifecycle()
    val episodeState by remember { vm.episodeState }.collectAsStateWithLifecycle()
    val player by remember { vm.player }

    if (DeviceApi.isMinTiramisu) RequestNotificationPermission()

    if (uiState.loading) {
        LoadingBox()
    } else if (episodeState.isPopulated && player != null) {
        EpisodeContent(
            uiState = uiState,
            episodeState = episodeState,
            player = player!!,
            actionHandler = vm.actionHandler
        )
    }
}

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun EpisodeContent(
    modifier: Modifier = Modifier,
    uiState: EpisodeDetailViewModel.UiState,
    episodeState: EpisodeDetailViewModel.EpisodeState,
    player: Player,
    actionHandler: (EpisodeDetailAction) -> Unit
) {
    // TODO move all to reusable theme values
    val buttonPadding = 18.dp
    val outerPadding = 16.dp
    val fontSize = 24.sp

    val coroutineScope = rememberCoroutineScope()
    var position by remember { mutableStateOf("") }

    LaunchedEffect("position") {
        coroutineScope.launch {
            val interval = 1000L
            val player = player
            val initialDelay = interval - (player.currentPosition % interval)
            delay(initialDelay)

            while (true) {
                if (player.isPlaying) {
                    position = formatPosition(player.currentPosition, player.contentDuration)
                }
                delay(interval)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().then(modifier),
        topBar = { EpisodeDetailTopBar(modifier = Modifier, actionHandler = actionHandler) },
        bottomBar = {}
    ) { contentPadding ->
        Column(
            modifier = Modifier.padding(contentPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                modifier = Modifier
                    .padding(buttonPadding)
                    .size(256.dp)
                    .clip(
                        shape = RoundedCornerShape(8.dp)
                    ),
                model = episodeState.imageUrl,
                contentDescription = ""
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "\uD83D\uDCBE",
                    modifier = Modifier.padding(buttonPadding).clickable { actionHandler(Download) },
                    fontSize = fontSize
                )
                Text(
                    text = "➕",
                    modifier = Modifier.padding(buttonPadding).clickable { actionHandler(AddToQueue) },
                    fontSize = fontSize
                )
                Button(
                    modifier = Modifier.padding(buttonPadding),
                    onClick = { actionHandler(PlayPause) }
                ) {
                    Text(text = if (uiState.isPlaying) "⏸" else "▶", fontSize = fontSize)
                }
                Text(
                    text = "\u2714\uFE0F",
                    modifier = Modifier.padding(buttonPadding).clickable { actionHandler(ToggleHasPlayed) },
                    fontStyle = if (uiState.hasPlayed) FontStyle.Italic else FontStyle.Normal,
                    fontSize = fontSize
                )
                Text(
                    text = "\uD83D\uDDC4\uFE0F",
                    fontStyle = if (uiState.isArchived) FontStyle.Italic else FontStyle.Normal,
                    modifier = Modifier.padding(buttonPadding).clickable { actionHandler(Archive) },
                    fontSize = fontSize + 2.sp
                )
            }
            Spacer(modifier = Modifier.padding(vertical = 4.dp))

            Row(modifier = Modifier.padding(horizontal = outerPadding)) {
                Text(text = episodeState.displayDate.orEmpty())
                Spacer(modifier = Modifier.weight(1.0f))
                Text(text = position)
            }
            Spacer(modifier = Modifier.padding(vertical = 2.dp))
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                Text(modifier = Modifier.padding(horizontal = outerPadding), text = episodeState.description.orEmpty())
            }
        }
    }
}

@Composable
fun EpisodeDetailTopBar(modifier: Modifier = Modifier, actionHandler: (EpisodeDetailAction) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().then(modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Absolute.Right
    ) {
        Text(
            modifier = Modifier
                .padding(16.dp)
                .clickable { actionHandler(ToggleBookmarked) },
            fontSize = 24.sp,
            text = "♥"
        )
        Text(
            modifier = Modifier
                .padding(16.dp)
                .clickable { actionHandler(EpisodeDetailAction.Share) },
            fontSize = 24.sp,
            text = "\uD83D\uDCE4"
        )
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
            Locale.getDefault(), "%02d:%02d:%02d / %02d:%02d:%02d",
            cHours, cMins, cSecs,
            tHours, tMins, tSecs
        )

        else -> String.format(
            Locale.getDefault(), "%02d:%02d / %02d:%02d",
            cMins, cSecs, tMins, tSecs
        )
    }
}

private operator fun TextUnit.plus(other: TextUnit): TextUnit {
    return (this.value + other.value).sp
}
