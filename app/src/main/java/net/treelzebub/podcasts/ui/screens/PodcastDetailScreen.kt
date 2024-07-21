package net.treelzebub.podcasts.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import net.treelzebub.podcasts.ui.components.ItemCard
import net.treelzebub.podcasts.ui.components.LoadingBox
import net.treelzebub.podcasts.ui.models.EpisodeUi
import net.treelzebub.podcasts.ui.models.PodcastUi
import net.treelzebub.podcasts.ui.screens.destinations.EpisodeDetailDestination
import net.treelzebub.podcasts.ui.theme.TextStyles
import net.treelzebub.podcasts.ui.vm.PodcastDetailsViewModel
import timber.log.Timber


@Destination
@Composable
fun PodcastDetailsScreen(
    navigator: DestinationsNavigator,
    podcastId: String
) {
    val vm = hiltViewModel<PodcastDetailsViewModel, PodcastDetailsViewModel.Factory>(
        creationCallback = { factory -> factory.create(podcastId = podcastId) }
    )
    val state by remember { vm.uiState }.collectAsStateWithLifecycle()
    val onDelete: () -> Unit = {
        vm.deletePodcast()
        navigator.popBackStack()
    }
    val showPlayed: () -> Unit = { vm.onToggleShowPlayed() }

    if (!state.loading && state.podcast == null) {
        Timber.e(IllegalStateException("PodcastDetailScreen State: not loading, null podcast."))
        navigator.navigateUp()
    } else if (state.loading) {
        LoadingBox()
    } else {
        PodcastDetails(
            navigator = navigator,
            state = state,
            onToggleShowPlayed = showPlayed,
            onDelete = onDelete
        )
    }
}

@Composable
private fun PodcastDetails(
    navigator: DestinationsNavigator,
    state: PodcastDetailsViewModel.State,
    onToggleShowPlayed: () -> Unit,
    onDelete: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            PodcastHeader(
                podcast = state.podcast!!,
                showPlayed = state.showPlayed,
                onToggleShowPlayed = onToggleShowPlayed,
                onDelete = onDelete
            )
        }
        items(items = state.episodes, key = { it.id }) {
            EpisodeItem(navigator, it)
        }
    }
}

@Composable
private fun PodcastHeader(
    podcast: PodcastUi,
    showPlayed: Boolean,
    onToggleShowPlayed: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val toggle = { expanded = !expanded }

    Row(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        AsyncImage(
            modifier = Modifier
                .wrapContentSize()
                .weight(1.0f)
                .padding(12.dp),
            model = podcast.imageUrl,
            contentDescription = "Podcast Logo"
        )
        Column(
            modifier = Modifier.weight(3.0f).padding(vertical = 12.dp),
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
        Box(modifier = Modifier.padding(top = 12.dp)) {
            Icon(
                modifier = Modifier.clickable(onClick = toggle),
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "More menu"
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(
                    text = { Text(text = "Show Played") },
                    leadingIcon = {
                        val icon = if (showPlayed) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle
                        Icon(icon, contentDescription = "")
                    },
                    onClick = {
                        toggle()
                        onToggleShowPlayed()
                    }
                )
                DropdownMenuItem(text = { Text(text = "Delete") }, onClick = onDelete)
            }
        }
    }
}

@Composable
// Extension on LazyItemScope makes importing animateItem() possible.
private fun LazyItemScope.EpisodeItem(navigator: DestinationsNavigator, episode: EpisodeUi) {
    ItemCard(
        modifier = Modifier.animateItem(
            placementSpec = spring(
                stiffness = Spring.StiffnessMediumLow,
                visibilityThreshold = IntOffset.VisibilityThreshold
            )
        ).clickable {
            navigator.navigate(EpisodeDetailDestination(episode.id))
        }
    ) {
        Column(
            Modifier
                .weight(3.0f)
                .wrapContentHeight()
                .fillMaxWidth()
                .padding(start = 12.dp, top = 6.dp, end = 12.dp, bottom = 6.dp)
        ) {
            BasicText(
                modifier = Modifier.padding(bottom = 2.dp),
                style = TextStyles.CardSubtitle,
                text = episode.title
            )
            BasicText(
                modifier = Modifier.padding(bottom = 2.dp),
                style = TextStyles.CardDate,
                text = episode.displayDate
            )
            BasicText(
                modifier = Modifier.height(72.dp),
                style = TextStyle(textAlign = TextAlign.Start),
                overflow = TextOverflow.Ellipsis,
                text = episode.description
            )
        }
    }
}
