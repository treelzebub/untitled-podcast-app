package net.treelzebub.podcasts.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import net.treelzebub.podcasts.ui.components.PodcastList
import net.treelzebub.podcasts.ui.vm.PodcastsViewModel

@RootNavGraph(start = true)
@Destination
@Composable
fun SubscriptionsScreen(navigator: DestinationsNavigator) {
    val vm = hiltViewModel<PodcastsViewModel>()
    val podcasts by remember { vm.podcasts }.collectAsState(initial = emptyList())
    PodcastList(podcasts)
}
