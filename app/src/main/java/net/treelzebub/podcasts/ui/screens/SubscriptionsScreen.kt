package net.treelzebub.podcasts.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.treelzebub.podcasts.ui.components.LoadingBox
import net.treelzebub.podcasts.ui.screens.destinations.PodcastDetailsScreenDestination
import net.treelzebub.podcasts.ui.vm.SubscriptionsViewModel

@RootNavGraph(start = true)
@Destination
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SubscriptionsScreen(navigator: DestinationsNavigator) {
    val scope = rememberCoroutineScope()
    val vm = hiltViewModel<SubscriptionsViewModel>()
    val state by remember { vm.state }.collectAsStateWithLifecycle()
    var refreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullToRefreshState()
    val refresh = {
        scope.launch {
            val start = System.currentTimeMillis()
            refreshing = true
            vm.refresh()
            refreshing = false
            val elapsed = System.currentTimeMillis() - start
            if (elapsed < 1000) delay(1000 - elapsed)
            pullRefreshState.endRefresh()
        }
    }

    if (state.loading) {
        LoadingBox()
    } else {
        Box(modifier = Modifier
            .fillMaxSize()
            .nestedScroll(pullRefreshState.nestedScrollConnection)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.Center,
                state = rememberLazyGridState()
            ) {
                items(state.podcasts) {
                    AsyncImage(
                        modifier = Modifier.animateItem(
                            placementSpec = spring(
                                stiffness = Spring.StiffnessMediumLow,
                                visibilityThreshold = IntOffset.VisibilityThreshold)
                        )
                            .fillMaxSize()
                            .padding(4.dp)
                            .shadow(elevation = 4.dp)
                            .clickable { navigator.navigate(PodcastDetailsScreenDestination(it.id)) },
                        model = it.imageUrl,
                        contentDescription = "Artwork for podcast ${it.title}"
                    )
                }
            }

            if (pullRefreshState.isRefreshing) {
                LaunchedEffect(Unit) {
                    refresh()
                }
            }

            PullToRefreshContainer(
                state = pullRefreshState,
                modifier = Modifier
                    .align(Alignment.TopCenter),
            )
        }
    }
}