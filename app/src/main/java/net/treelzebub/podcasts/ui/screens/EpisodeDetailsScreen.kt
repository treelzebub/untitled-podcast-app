package net.treelzebub.podcasts.ui.screens

import android.annotation.SuppressLint
import androidx.annotation.OptIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
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
import net.treelzebub.podcasts.ui.vm.EpisodeDetailsViewModel.Action.Download
import net.treelzebub.podcasts.ui.vm.EpisodeDetailsViewModel.Action.PlayPause
import net.treelzebub.podcasts.ui.vm.EpisodeDetailsViewModel.Action.Share
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
    val episodeState by remember { vm.episodeState }.collectAsStateWithLifecycle()
    val mutableState by remember { vm.mutableState }.collectAsStateWithLifecycle()
    val position by remember { vm.positionState }.collectAsStateWithLifecycle()

    if (DeviceApi.isMinTiramisu) RequestNotificationPermission()

    if (mutableState.loading || episodeState.episode == null) {
        LoadingBox()
    } else {
        EpisodeContent(
            episode = episodeState.episode!!,
            uiState = mutableState,
            position = position,
            actionHandler = vm.actionHandler
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
fun EpisodeDetailTopBar(
    modifier: Modifier = Modifier,
    actionHandler: (Action) -> Unit,
    isBookmarked: Boolean
) {
    val context = LocalContext.current
    val bookmarkIcon = if (isBookmarked) R.drawable.bookmark_filled else R.drawable.bookmark_empty
    val bookmarkDescription = "Bookmark button: " +
        if (isBookmarked) "episode is bookmarked" else "episode is not bookmarked"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 12.dp, top = 6.dp)
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Absolute.Right
    ) {
        Image(
            modifier = Modifier
                .size(32.dp)
                .clickable { actionHandler(ToggleBookmarked) },
            painter = painterResource(bookmarkIcon),
            contentDescription = bookmarkDescription
        )
        Spacer(Modifier.width(12.dp))
        Image(
            modifier = Modifier
                .size(32.dp)
                .clickable { actionHandler(Share(context)) },
            painter = painterResource(R.drawable.share),
            contentDescription = "Share button"
        )
    }
}

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun EpisodeContent(
    modifier: Modifier = Modifier,
    episode: EpisodeUi,
    uiState: EpisodeDetailsViewModel.MutableEpisodeState,
    position: String,
    actionHandler: (Action) -> Unit
) {
    // TODO handle with reusable theme values + contentPadding
    val outerPadding = 16.dp

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier),
        topBar = {
            EpisodeDetailTopBar(
                modifier = Modifier,
                actionHandler = actionHandler,
                uiState.isBookmarked
            )
        },
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

            MediaButtons(
                uiState = uiState,
                outerPadding = outerPadding,
                actionHandler = actionHandler
            )

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = outerPadding),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
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
            Spacer(modifier = Modifier.padding(vertical = 4.dp))
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
fun MediaButtons(
    uiState: EpisodeDetailsViewModel.MutableEpisodeState,
    outerPadding: Dp,
    actionHandler: (Action) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(outerPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        ButtonCircleBorderless(
            modifier = Modifier
                .weight(1.0f),
            res = R.drawable.notification_action_download,
            contentDescription = "Download episode button",
            onClick = { actionHandler(Download) }
        )
        ButtonCircleBorderless(
            modifier = Modifier
                .weight(1.0f),
            res = R.drawable.notification_action_playnext,
            contentDescription = "Add episode to queue button",
            onClick = { actionHandler(AddToQueue) }
        )
        ButtonCircleBorderless(
            modifier = Modifier
                .padding(6.dp)
                .weight(1.0f),
            res = if (uiState.isPlaying) R.drawable.notif_pause else R.drawable.notif_play,
            contentDescription = if (uiState.isPlaying) "Pause button" else "Play button",
            onClick = { actionHandler(PlayPause) }
        )
        ButtonCircleBorderless(
            modifier = Modifier
                .weight(1.0f),
            res = if (uiState.hasPlayed) androidx.media3.session.R.drawable.media3_icon_check_circle_filled else androidx.media3.session.R.drawable.media3_icon_check_circle_unfilled,
            contentDescription = if (uiState.hasPlayed) "Has played" else "Has not played",
            onClick = { actionHandler(ToggleHasPlayed) }
        )
    }
}

private operator fun TextUnit.plus(other: TextUnit): TextUnit {
    return (this.value + other.value).sp
}
