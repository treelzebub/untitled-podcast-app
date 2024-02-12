package net.treelzebub.podcasts.ui.components

import android.util.Log
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import net.treelzebub.podcasts.ui.models.PodcastUi
import net.treelzebub.podcasts.ui.screens.destinations.PodcastDetailsScreenDestination
import net.treelzebub.podcasts.ui.theme.TextStyles

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun PodcastList(
    navigator: DestinationsNavigator,
    podcasts: List<PodcastUi>
) {
    Log.d("TEST", "Composing ${podcasts.size} podcasts.")
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(podcasts, key = { it.link }) {
            PodcastItem(Modifier.animateItemPlacement(tween(durationMillis = 250)), navigator, it)
        }
    }
}

@Composable
fun PodcastItem(
    modifier: Modifier,
    navigator: DestinationsNavigator,
    podcast: PodcastUi
) {
    Row(
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable { navigator.navigate(PodcastDetailsScreenDestination(podcast.link)) }
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
                style = TextStyles.CardDate,
                text = podcast.description
            )
            BasicText(
                style = TextStyle(textAlign = TextAlign.Start),
                overflow = TextOverflow.Ellipsis,
                text = ""
            )
        }
    }
    Divider(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color.DarkGray))
}

