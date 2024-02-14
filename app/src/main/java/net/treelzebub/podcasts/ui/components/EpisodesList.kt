package net.treelzebub.podcasts.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import net.treelzebub.podcasts.ui.models.EpisodeUi
import net.treelzebub.podcasts.ui.screens.destinations.NowPlayingScreenDestination
import net.treelzebub.podcasts.ui.theme.TextStyles

@Composable
fun EpisodesList(
    navigator: DestinationsNavigator,
    modifier: Modifier,
    episodes: List<EpisodeUi>
) {
    Column(
        Modifier
            .fillMaxSize()
            .then(modifier)
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            episodes.forEach {
                item { EpisodeItem(navigator, it) }
            }
        }
    }
}

@Composable
fun EpisodeItem(navigator: DestinationsNavigator, item: EpisodeUi) {
    ItemCard(
        modifier = Modifier.clickable {
            navigator.navigate(NowPlayingScreenDestination(item.id))
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
                text = item.title
            )
            BasicText(modifier = Modifier.padding(bottom = 2.dp), style = TextStyles.CardDate, text = item.date)
            BasicText(
                modifier = Modifier.height(72.dp),
                style = TextStyle(textAlign = TextAlign.Start),
                overflow = TextOverflow.Ellipsis,
                text = item.description
            )
        }
    }
}
