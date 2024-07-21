package net.treelzebub.podcasts.ui.screens

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import net.treelzebub.podcasts.ui.components.LoadingBox
import net.treelzebub.podcasts.ui.models.PodcastUi
import net.treelzebub.podcasts.ui.screens.destinations.PodcastDetailsScreenDestination
import net.treelzebub.podcasts.ui.theme.LightestPurple
import net.treelzebub.podcasts.ui.theme.TextStyles
import net.treelzebub.podcasts.ui.vm.SubscriptionsViewModel


@RootNavGraph(start = true)
@Destination
@Composable
fun SubscriptionsScreen(navigator: DestinationsNavigator) {
    val vm = hiltViewModel<SubscriptionsViewModel>()
    val state by remember { vm.state }.collectAsState()

    if (state.loading) {
        LoadingBox()
    } else {
        Scaffold() { contentPadding ->
            PodcastList(navigator, state.podcasts, contentPadding)
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun PodcastList(
    navigator: DestinationsNavigator,
    podcasts: List<PodcastUi>,
    contentPadding: PaddingValues
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
    ) {
        items(podcasts, key = { it.id }) {
            PodcastItem(Modifier.animateItemPlacement(tween(durationMillis = 250)), navigator, it, contentPadding)
        }
    }
}

@Composable
fun PodcastItem(
    modifier: Modifier,
    navigator: DestinationsNavigator,
    podcast: PodcastUi,
    contentPadding: PaddingValues
) {
    Row(
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .padding(contentPadding)
            .clickable { navigator.navigate(PodcastDetailsScreenDestination(podcast.id)) }
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxSize()
                .weight(1.0f),
            model = podcast.imageUrl,
            contentDescription = "Podcast Logo"
        )
        Column(
            Modifier
                .weight(3f)
                .wrapContentHeight()
                .padding(12.dp)
        ) {
            BasicText(
                modifier = Modifier.padding(bottom = 2.dp),
                style = TextStyles.CardSubtitle,
                text = podcast.title
            )
            BasicText(
                modifier = Modifier.padding(bottom = 2.dp),
                style = TextStyles.CardDescription,
                text = podcast.description
            )
            BasicText(
                style = TextStyles.CardDate,
                overflow = TextOverflow.Ellipsis,
                text = "Updated: " + podcast.lastBuildDate
            )
        }
    }
    HorizontalDivider(
        thickness = 0.5.dp,
        color = LightestPurple
    )
}
