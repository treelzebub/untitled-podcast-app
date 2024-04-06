package net.treelzebub.podcasts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import net.treelzebub.podcasts.ui.PodcastsApp
import net.treelzebub.podcasts.ui.screens.DebugScreen
import net.treelzebub.podcasts.ui.theme.PodcastsTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PodcastsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (BuildConfig.DEBUG) {
                        DebugScreen()
                    } else {
                        PodcastsApp()
                    }
                }
            }
        }
    }
}
