package net.treelzebub.podcasts.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.ramcosta.composedestinations.annotation.Destination
import net.treelzebub.podcasts.ui.components.LoadingBox
import net.treelzebub.podcasts.ui.models.EpisodeUi
import net.treelzebub.podcasts.ui.vm.EpisodeDetailViewModel
import net.treelzebub.podcasts.ui.vm.EpisodeDetailViewModel.EpisodeDetailAction.AddToQueue
import net.treelzebub.podcasts.ui.vm.EpisodeDetailViewModel.EpisodeDetailAction.Archive
import net.treelzebub.podcasts.ui.vm.EpisodeDetailViewModel.EpisodeDetailAction.Download
import net.treelzebub.podcasts.ui.vm.EpisodeDetailViewModel.EpisodeDetailAction.Fave
import net.treelzebub.podcasts.ui.vm.EpisodeDetailViewModel.EpisodeDetailAction.MarkPlayed
import net.treelzebub.podcasts.ui.vm.EpisodeDetailViewModel.EpisodeDetailAction.Play
import net.treelzebub.podcasts.ui.vm.EpisodeDetailViewModel.EpisodeDetailAction.Share

@Destination
@Composable
fun EpisodeDetail(episodeId: String) {
    val vm = hiltViewModel<EpisodeDetailViewModel, EpisodeDetailViewModel.Factory>(
        creationCallback = { factory -> factory.create(episodeId = episodeId) }
    )
    val state by remember { vm.state }.collectAsState()

    if (state.loading) {
        LoadingBox()
    } else if (state.episodeUi != null) {
        EpisodeContent(episode = state.episodeUi!!, actionHandler = vm.actionHandler)
    } // else illegal state
}

@Composable
fun EpisodeContent(
    modifier: Modifier = Modifier,
    episode: EpisodeUi,
    actionHandler: (EpisodeDetailViewModel.EpisodeDetailAction) -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize().then(modifier),
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Absolute.Right
            ) {
                Text(text = "♥", Modifier.padding(16.dp).clickable { actionHandler(Fave) })
                Text(
                    text = "\uD83D\uDCE4",
                    Modifier.padding(16.dp).clickable { actionHandler(Share) })
            }
        },
        bottomBar = {}
    ) { contentPadding ->
        Column(
            modifier = Modifier.padding(contentPadding)
        ) {
            AsyncImage(model = episode.imageUrl, contentDescription = "")
            Spacer(modifier = Modifier.padding(vertical = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "\uD83D\uDCBE",
                    modifier = Modifier.padding(18.dp).clickable { actionHandler(Download) },
                    fontSize = 24.sp
                )
                Text(
                    text = "➕",
                    modifier = Modifier.padding(18.dp).clickable { actionHandler(AddToQueue) },
                    fontSize = 24.sp
                )
                Button(
                    modifier = Modifier.padding(18.dp),
                    onClick = { actionHandler(Play) }
                ) {
                    Text(text = "▶", fontSize = 18.sp)
                }
                Text(
                    text = "✔",
                    modifier = Modifier.padding(24.dp).clickable { actionHandler(MarkPlayed) },
                    fontSize = 24.sp
                )
                Text(
                    text = "\uD83D\uDDC4\uFE0F",
                    modifier = Modifier.padding(24.dp).clickable { actionHandler(Archive) },
                    fontSize = 24.sp
                )
            }
            Spacer(modifier = Modifier.padding(vertical = 8.dp))
            Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(text = episode.displayDate)
                Spacer(modifier = Modifier.weight(1.0f))
                Text(text = episode.duration)
            }
            Spacer(modifier = Modifier.padding(vertical = 8.dp))
            Text(modifier = Modifier.padding(horizontal = 16.dp), text = episode.description)
        }
    }
}