package net.treelzebub.podcasts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.media3.common.util.UnstableApi
import dagger.hilt.android.AndroidEntryPoint
import net.treelzebub.podcasts.data.PodcastsRepo
import net.treelzebub.podcasts.ui.PodcastsApp
import net.treelzebub.podcasts.ui.theme.PodcastsTheme
import javax.inject.Inject


@UnstableApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var podcastsRepo: PodcastsRepo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PodcastsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PodcastsApp()
                }
            }
        }
    }

    override fun onDestroy() {
        podcastsRepo.cancelScope()
        super.onDestroy()
    }
}
