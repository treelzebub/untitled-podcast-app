package net.treelzebub.podcasts.ui.screens

import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import net.treelzebub.podcasts.ui.components.PodcastList
import net.treelzebub.podcasts.ui.models.PodcastUi
import androidx.hilt.navigation.compose.hiltViewModel
import net.treelzebub.podcasts.ui.vm.PodcastsViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf

@RootNavGraph(start = true)
@Destination
@Composable
fun SubscriptionsScreen() {
    val vm = hiltViewModel<PodcastsViewModel>()
    val podcasts by remember { vm.podcasts }.collectAsState(initial = emptyList())
    PodcastList(podcasts)
}
