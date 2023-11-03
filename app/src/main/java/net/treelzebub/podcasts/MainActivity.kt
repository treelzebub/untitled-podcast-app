package net.treelzebub.podcasts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.AndroidEntryPoint
import net.treelzebub.podcasts.net.models.Feed
import net.treelzebub.podcasts.ui.FeedsList
import net.treelzebub.podcasts.ui.SearchFeeds
import net.treelzebub.podcasts.ui.models.ChannelUi
import net.treelzebub.podcasts.ui.components.EpisodesList
import net.treelzebub.podcasts.ui.vm.EpisodesViewModel
import net.treelzebub.podcasts.ui.theme.PodcastsTheme
import net.treelzebub.podcasts.ui.vm.SearchFeedsViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PodcastsTheme {
//                val vm: EpisodesViewModel = viewModel()
//                var channels by remember {
//                    mutableStateOf(listOf<ChannelUi>())
//                }
                val vm: SearchFeedsViewModel = viewModel()
                var feeds by remember { mutableStateOf(listOf<Feed>()) }
                vm.observe { feeds = it.feeds }

                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    // var isPlaying by remember { mutableStateOf(false) }
                    // vm.listenForPlayerEvents { isPlaying = it.isPlaying }
                    // TempMediaPlayer(vm.play, vm.stop, isPlaying)
//                    vm.listenForEpisodes { channels = it }
//                    EpisodesList(channel = channels.firstOrNull())
                    Column(modifier = Modifier.fillMaxSize()) {
                        SearchFeeds(onSearch = {
                            vm.search(it)
                        })
                        FeedsList(feeds = feeds)
                    }
                }
//                vm.test(this)
            }
        }
    }
}
