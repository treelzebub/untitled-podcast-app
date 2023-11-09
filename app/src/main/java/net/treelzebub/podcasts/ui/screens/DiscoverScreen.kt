package net.treelzebub.podcasts.ui.screens

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.launch
import net.treelzebub.podcasts.net.models.Feed
import net.treelzebub.podcasts.ui.components.ItemCard
import net.treelzebub.podcasts.ui.theme.TextStyles
import net.treelzebub.podcasts.ui.vm.SearchFeedsViewModel

@Destination
@Composable
fun DiscoverScreen() {
    val scope = rememberCoroutineScope()
    val searchVm: SearchFeedsViewModel = hiltViewModel()
    var state by remember { mutableStateOf(SearchFeedsViewModel.SearchFeedsState.Initial) }
    val onSearch = { query: String -> searchVm.search(query) }
    val onSelect = { feed: Feed -> searchVm.select(feed) }

//        searchVm.state.collectLatest { state = it }
    Column(Modifier.fillMaxSize()) {
        SearchFeeds(onSearch)
        ResultsList(state.feeds, onSelect)
        Text("Discover")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchFeeds(
    onSearch: (String) -> Unit
) {
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

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun ResultsList(
    feeds: List<Feed>,
    onSelect: (Feed) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(feeds, key = { it.id }) {
            FeedItem(
                Modifier.animateItemPlacement(tween(durationMillis = 250)),
                it,
                onSelect
            )
        }
    }
}

@Composable
fun FeedItem(
    modifier: Modifier,
    feed: Feed,
    onSelect: (Feed) -> Unit
) {
    ItemCard(
        modifier = Modifier.clickable { onSelect(feed) }
    ) {
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
