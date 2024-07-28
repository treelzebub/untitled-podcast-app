package net.treelzebub.podcasts.ui.screens

import android.annotation.SuppressLint
import androidx.annotation.OptIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import coil.compose.AsyncImage
import com.ramcosta.composedestinations.annotation.Destination
import net.treelzebub.podcasts.R
import net.treelzebub.podcasts.platform.RequestNotificationPermission
import net.treelzebub.podcasts.ui.components.ButtonCircleBorderless
import net.treelzebub.podcasts.ui.components.LoadingBox
import net.treelzebub.podcasts.ui.models.EpisodeUi
import net.treelzebub.podcasts.ui.theme.TextStyles
import net.treelzebub.podcasts.ui.vm.EpisodeDetailsViewModel
import net.treelzebub.podcasts.ui.vm.EpisodeDetailsViewModel.Action
import net.treelzebub.podcasts.ui.vm.EpisodeDetailsViewModel.Action.AddToQueue
import net.treelzebub.podcasts.ui.vm.EpisodeDetailsViewModel.Action.Archive
import net.treelzebub.podcasts.ui.vm.EpisodeDetailsViewModel.Action.Download
import net.treelzebub.podcasts.ui.vm.EpisodeDetailsViewModel.Action.PlayPause
import net.treelzebub.podcasts.ui.vm.EpisodeDetailsViewModel.Action.ToggleBookmarked
import net.treelzebub.podcasts.ui.vm.EpisodeDetailsViewModel.Action.ToggleHasPlayed
import net.treelzebub.podcasts.util.DeviceApi


@SuppressLint("UnsafeOptInUsageError")
@Destination
@Composable
fun EpisodeDetailsScreen(episodeId: String) {
    val vm = hiltViewModel<EpisodeDetailsViewModel, EpisodeDetailsViewModel.Factory>(
        creationCallback = { factory -> factory.create(episodeId) }
    )
    val episode by remember { vm.episode }.collectAsStateWithLifecycle(null)
    val uiState by remember { vm.uiState }.collectAsStateWithLifecycle()
    val player by remember { vm.player }
    val position by remember { vm.positionState }.collectAsStateWithLifecycle()

    if (DeviceApi.isMinTiramisu) RequestNotificationPermission()

    if (uiState.loading || episode == null || player == null) {
        LoadingBox()
    } else {
        EpisodeContent(
            episode = episode!!,
            uiState = uiState,
            position = position,
            actionHandler = vm.actionHandler
        )
    }
}

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun EpisodeContent(
    modifier: Modifier = Modifier,
    episode: EpisodeUi,
    uiState: EpisodeDetailsViewModel.UiState,
    position: String,
    actionHandler: (Action) -> Unit
) {
    // TODO move all to reusable theme values
    val buttonPadding = 18.dp
    val outerPadding = 16.dp
    val fontSize = 24.sp

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier),
        topBar = { EpisodeDetailTopBar(modifier = Modifier, actionHandler = actionHandler) },
        bottomBar = {}
    ) { contentPadding ->
        Column(
            modifier = Modifier.padding(contentPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .size(192.dp)
                    .clip(
                        shape = RoundedCornerShape(6.dp)
                    ),
                model = episode.imageUrl,
                contentDescription = ""
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                ButtonCircleBorderless(
                    res = R.drawable.notification_action_download,
                    contentDescription = "Download episode button",
                    onClick = { actionHandler(Download) }
                )
                ButtonCircleBorderless(
                    res = R.drawable.discover_add,
                    contentDescription = "Add episode to queue button",
                    onClick = { actionHandler(AddToQueue) }
                )
                ButtonCircleBorderless(
                    res = if (uiState.isPlaying) R.drawable.notif_pause else R.drawable.notif_play,
                    contentDescription = if (uiState.isPlaying) "Pause button" else "Play button",
                    onClick = { actionHandler(PlayPause) }
                )
                Text(
                    text = "\u2714\uFE0F",
                    modifier = Modifier
                        .padding(buttonPadding)
                        .clickable { actionHandler(ToggleHasPlayed) },
                    fontStyle = if (uiState.hasPlayed) FontStyle.Italic else FontStyle.Normal,
                    fontSize = fontSize
                )
                Text(
                    text = "\uD83D\uDDC4\uFE0F",
                    fontStyle = if (uiState.isArchived) FontStyle.Italic else FontStyle.Normal,
                    modifier = Modifier
                        .padding(buttonPadding)
                        .clickable { actionHandler(Archive) },
                    fontSize = fontSize + 2.sp
                )
            }

            Text(
                modifier = Modifier
                    .wrapContentWidth()
                    .padding(vertical = 4.dp, horizontal = outerPadding),
                style = TextStyles.CardTitle,
                text = episode.title
            )

            Row(modifier = Modifier.padding(horizontal = outerPadding)) {
                Text(
                    modifier = Modifier.wrapContentWidth(),
                    style = TextStyles.CardDate,
                    text = episode.displayDate
                )
                Spacer(modifier = Modifier.weight(1.0f))
                Text(
                    modifier = Modifier.wrapContentWidth(),
                    style = TextStyles.CardDate,
                    text = position
                )
            }
            Spacer(modifier = Modifier.padding(vertical = 2.dp))
            Column(modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())) {
                Text(
                    modifier = Modifier.padding(horizontal = outerPadding),
                    style = TextStyles.CardDescription,
                    text = episode.description
                )
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun EpisodeDetailTopBar(modifier: Modifier = Modifier, actionHandler: (Action) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
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
                .clickable { actionHandler(Action.Share) },
            fontSize = 24.sp,
            text = "\uD83D\uDCE4"
        )
    }
}

private operator fun TextUnit.plus(other: TextUnit): TextUnit {
    return (this.value + other.value).sp
}
