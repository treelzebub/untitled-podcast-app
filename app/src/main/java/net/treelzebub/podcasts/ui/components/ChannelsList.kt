package net.treelzebub.podcasts.ui.components

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import net.treelzebub.podcasts.Channel
import net.treelzebub.podcasts.ui.theme.TextStyles

@Preview
@Composable
@OptIn(ExperimentalFoundationApi::class)
fun ChannelsList(channels: List<Channel>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(channels, key = { it.id }) {
            ChannelItem(Modifier.animateItemPlacement(tween(durationMillis = 250)), it)
        }
    }
}

@Composable
fun ChannelItem(modifier: Modifier, channel: Channel) {
    ItemCard {
        Row(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .then(modifier),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                modifier = Modifier.fillMaxSize().weight(1.0f),
                model = channel.image,
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
                    text = channel.title
                )
                BasicText(
                    modifier = Modifier.padding(bottom = 2.dp),
                    style = TextStyles.CardDate,
                    text = channel.description
                )
                BasicText(
                    style = TextStyle(textAlign = TextAlign.Start),
                    overflow = TextOverflow.Ellipsis,
                    text = "${channel.episodeCount} episodes"
                )
            }
        }
    }
}

