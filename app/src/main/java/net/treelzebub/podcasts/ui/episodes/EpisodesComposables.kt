package net.treelzebub.podcasts.ui.episodes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import net.treelzebub.podcasts.data.ui.ChannelUi
import net.treelzebub.podcasts.data.ui.EpisodeUi
import net.treelzebub.podcasts.ui.theme.TextStyles

@Composable
fun EpisodesList(channel: ChannelUi?) {
    channel ?: return
    Column(Modifier.fillMaxSize()) {
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
                model = channel.image,
                contentDescription = "Podcast Logo"
            )
            Column(
                modifier = Modifier.weight(3.0f),
                verticalArrangement = Arrangement.Center
            ) {
                BasicText(
                    modifier = Modifier.padding(bottom = 2.dp),
                    style = TextStyles.CardTitle,
                    text = channel.title
                )
                BasicText(
                    modifier = Modifier.wrapContentHeight(),
                    style = TextStyle(textAlign = TextAlign.Start),
                    overflow = TextOverflow.Ellipsis,
                    text = channel.description
                )
            }
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
        ) {
            channel.episodes.forEach {
                item { EpisodeItem(it) }
            }
        }
    }
}

@Composable
fun EpisodeItem(item: EpisodeUi) {
    Card(
        Modifier
            .padding(start = 12.dp, top = 0.dp, end = 12.dp, bottom = 6.dp)
            .height(IntrinsicSize.Min)
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
