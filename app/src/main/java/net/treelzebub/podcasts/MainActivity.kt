package net.treelzebub.podcasts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.AndroidEntryPoint
import net.treelzebub.podcasts.data.ui.ChannelUi
import net.treelzebub.podcasts.ui.episodes.EpisodesList
import net.treelzebub.podcasts.ui.episodes.EpisodesViewModel
import net.treelzebub.podcasts.ui.theme.PodcastsTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
<<<<<<< Updated upstream
=======
        setContent {
            val vm: SearchFeedsViewModel = viewModel()
            var state by rememberSaveable { mutableStateOf(SearchFeedsState.Initial) }
            
            LaunchedEffect(vm.state) {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    vm.state.collectLatest { state = it }
                }
            }
>>>>>>> Stashed changes

        setContent {
            PodcastsTheme {
<<<<<<< Updated upstream
                val vm: EpisodesViewModel = viewModel()
                var channels by remember {
                    mutableStateOf(listOf<ChannelUi>())
=======
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        SearchFeeds { vm.search(it) }
                        FeedsList(state.feeds)
                    }
>>>>>>> Stashed changes
                }
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    // var isPlaying by remember { mutableStateOf(false) }
                    // vm.listenForPlayerEvents { isPlaying = it.isPlaying }
                    // TempMediaPlayer(vm.play, vm.stop, isPlaying)
                    vm.listenForEpisodes { channels = it }
                    EpisodesList(channel = channels.firstOrNull())
                }
                vm.test(this)
            }
        }
    }
}
