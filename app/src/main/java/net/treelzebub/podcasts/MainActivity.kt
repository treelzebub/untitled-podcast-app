package net.treelzebub.podcasts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
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
import kotlinx.coroutines.flow.collectLatest
import net.treelzebub.podcasts.ui.FeedsList
import net.treelzebub.podcasts.ui.SearchFeeds
import net.treelzebub.podcasts.ui.theme.PodcastsTheme
import net.treelzebub.podcasts.ui.vm.SearchFeedsViewModel
import net.treelzebub.podcasts.ui.vm.SearchFeedsViewModel.SearchFeedsState

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: SearchFeedsViewModel = viewModel()
            var state by remember { mutableStateOf(SearchFeedsState.Initial) }
            LaunchedEffect(vm.state) {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    vm.state.collectLatest { state = it }
                }
            }

            PodcastsTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        SearchFeeds { vm.search(it) }
                        FeedsList(feeds = state.feeds)
                    }
                }
            }
        }
    }
}
