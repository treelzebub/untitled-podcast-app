package net.treelzebub.podcasts.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import net.treelzebub.podcasts.ui.components.LoadingBox
import net.treelzebub.podcasts.ui.screens.destinations.PodcastDetailsScreenDestination
import net.treelzebub.podcasts.ui.vm.SubscriptionsViewModel

@RootNavGraph(start = true)
@Destination
@Composable
fun SubscriptionsScreenAlt(navigator: DestinationsNavigator) {
    val vm = hiltViewModel<SubscriptionsViewModel>()
    val state by remember { vm.state }.collectAsStateWithLifecycle()

    if (state.loading) {
        LoadingBox()
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.Center
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
    }
}