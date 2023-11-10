package net.treelzebub.podcasts.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.ramcosta.composedestinations.annotation.Destination
import net.treelzebub.podcasts.ui.components.EpisodesList
import net.treelzebub.podcasts.ui.components.LoadingBox
import net.treelzebub.podcasts.ui.models.EpisodeUi
import net.treelzebub.podcasts.ui.models.PodcastUi
import net.treelzebub.podcasts.ui.theme.TextStyles
import net.treelzebub.podcasts.ui.vm.PodcastDetailsViewModel

@Destination
@Composable
fun PodcastDetailsScreen(link: String) {
    val vm: PodcastDetailsViewModel = hiltViewModel()
    val state by remember { vm.state }.collectAsState()
    vm.getPodcastAndEpisodes(link)

    if (state.loading) {
        LoadingBox()
    } else {
        PodcastDetails(state.podcast!!, state.episodes)
    }
}

@Composable
private fun PodcastDetails(podcast: PodcastUi, episodes: List<EpisodeUi>) {
    Column(Modifier.fillMaxSize()) {
        PodcastHeader(podcast)
        EpisodesList(
            modifier = Modifier.weight(4f),
            episodes = episodes
        )
    }
}

@Composable
private fun PodcastHeader(podcast: PodcastUi) {
    Row(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(12.dp)
    ) {
        AsyncImage(
            modifier = Modifier
                .wrapContentSize()
                .weight(1.0f)
                .padding(end = 12.dp),
            model = podcast.imageUrl,
            contentDescription = "Podcast Logo"
        )
        Column(
            modifier = Modifier.weight(3.0f),
            verticalArrangement = Arrangement.Center
        ) {
            BasicText(
                modifier = Modifier.padding(bottom = 2.dp),
                style = TextStyles.CardTitle,
                text = podcast.title
            )
            BasicText(
                modifier = Modifier.wrapContentHeight(),
                style = TextStyle(textAlign = TextAlign.Start),
                overflow = TextOverflow.Ellipsis,
                text = podcast.description
            )
        }
    }
}
