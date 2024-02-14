package net.treelzebub.podcasts.ui.screens

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import net.treelzebub.podcasts.R
import net.treelzebub.podcasts.net.models.Feed
import net.treelzebub.podcasts.ui.components.ItemCard
import net.treelzebub.podcasts.ui.theme.TextStyles
import net.treelzebub.podcasts.ui.vm.DiscoverViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun DiscoverScreen(navigator: DestinationsNavigator) {
  val searchVm: DiscoverViewModel = hiltViewModel()
  var state by remember { mutableStateOf(DiscoverViewModel.SearchFeedsState.Initial) }
  val onSearch = { query: String? ->
    val trimmed = query?.trim()
    if (!trimmed.isNullOrBlank()) searchVm.search(trimmed)
  }
  val onSelect = { feed: Feed -> searchVm.select(feed) }

  LaunchedEffect(Unit) {
    searchVm.state.collect { state = it }
  }

  var text by remember { mutableStateOf("") }
  var active by remember { mutableStateOf(false) }

  val tempPreviousSearches = listOf("test", "lazy", "gaming", "okay", "testing", "search")

  Column(modifier = Modifier.fillMaxSize()) {
    SearchBar(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp, 0.dp, 12.dp, 12.dp),
      query = text,
      onQueryChange = {
        val changed = it.ifBlank { "" }
        text = changed
      },
      onSearch = {
        active = false
        onSearch(it)
      },
      active = active,
      onActiveChange = { active = it },
      placeholder = { Text("Search Podcasts") },
      leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
      trailingIcon = {
        Icon(
          modifier = Modifier.clickable { text = "" },
          imageVector = Icons.Default.Clear, contentDescription = null
        )
      }
    ) {
      LazyColumn(modifier = Modifier) {
        items(tempPreviousSearches) { item ->
          PreviousSearch(
            text = item,
            onClick = {
              active = false
              text = item
              onSearch(item)
            }
          )
        }
      }
    }
    ResultsList(state.feeds, onSelect)
  }
}

@Composable
fun PreviousSearch(text: String, onClick: (String) -> Unit) {
  Row(modifier = Modifier.padding(4.dp)) {
    Icon(painterResource(id = R.drawable.previous_search), contentDescription = "")
    Text(
      modifier = Modifier.clickable { onClick(text) },
      text = text
    )
  }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun ResultsList(
  feeds: List<Feed>,
  onSelect: (Feed) -> Unit
) {
  LazyColumn(
    modifier = Modifier,
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
