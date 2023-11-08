package net.treelzebub.podcasts.ui

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import net.treelzebub.podcasts.net.models.Feed
import net.treelzebub.podcasts.ui.components.ItemCard
import net.treelzebub.podcasts.ui.theme.TextStyles

@Composable
fun SearchScreen(onSearch: (String) -> Unit) {
    val scaffold =
    Scaffold(
        modifier = Modifier.fillMaxSize(),

    ) {

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchFeeds(onSearch: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }
    Box(
        Modifier
            .wrapContentHeight()
            .semantics { isTraversalGroup = true }) {
        SearchBar(
            modifier = Modifier.fillMaxWidth(),
            query = text,
            onQueryChange = { text = it },
            onSearch = {
                active = false
                onSearch(it)
            },
            active = active,
            onActiveChange = { active = it },
            placeholder = { Text("Search Podcasts") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = { Icon(Icons.Default.MoreVert, contentDescription = null) }
        ) {}
    }
}

fun ResultsList(results: List<Feed>) {
    FeedsList(feeds = results)
}

@OptIn(ExperimentalFoundationApi::class)
fun FeedsList(feeds: List<Feed>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(feeds, key = { it.id }) {
            FeedItem(Modifier.animateItemPlacement(tween(durationMillis = 250)), it)
        }
    }
}

@Composable
fun FeedItem(modifier: Modifier, feed: Feed) {
    ItemCard {
        Row(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .then(modifier),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1.0f),
                model = feed.image,
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
                    text = feed.title
                )
                BasicText(
                    modifier = Modifier.padding(bottom = 2.dp),
                    style = TextStyles.CardDate,
                    text = feed.description
                )
                BasicText(
                    style = TextStyle(textAlign = TextAlign.Start),
                    overflow = TextOverflow.Ellipsis,
                    text = "${feed.episodeCount} episodes"
                )
            }
        }
    }
}
