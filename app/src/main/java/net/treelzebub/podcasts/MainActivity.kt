package net.treelzebub.podcasts

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onEach
import net.treelzebub.podcasts.net.models.Feed
import net.treelzebub.podcasts.ui.components.EpisodesList
import net.treelzebub.podcasts.ui.components.PodcastList
import net.treelzebub.podcasts.ui.components.TabsBar
import net.treelzebub.podcasts.ui.models.EpisodeUi
import net.treelzebub.podcasts.ui.models.PodcastUi
import net.treelzebub.podcasts.ui.screens.SubscriptionsScreen
import net.treelzebub.podcasts.ui.theme.PodcastsTheme
import net.treelzebub.podcasts.ui.vm.PodcastsViewModel
import net.treelzebub.podcasts.ui.vm.SearchFeedsViewModel
import net.treelzebub.podcasts.ui.vm.SearchFeedsViewModel.SearchFeedsState

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val searchVm: SearchFeedsViewModel = viewModel()
            var state by remember { mutableStateOf(SearchFeedsState.Initial) }
            val onSearch = { query: String -> searchVm.search(query) }
            val onSelect = { feed: Feed -> searchVm.select(feed) }

            LaunchedEffect(searchVm.state) {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    searchVm.state.collectLatest { state = it }
                }
            }
            PodcastsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(bottomBar = { TabsBar() }) {
                        SubscriptionsScreen()
                    }
                }
            }
        }
    }
}
