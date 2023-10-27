package net.treelzebub.podcasts

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import net.treelzebub.podcasts.ui.NowPlayingViewModel
import net.treelzebub.podcasts.ui.compose.TempMediaPlayer
import net.treelzebub.podcasts.ui.theme.PodcastsTheme

class MainActivity : ComponentActivity() {

    private val tempEpisodeUrl =
        "https://c10.patreonusercontent.com/4/patreon-media/p/post/29138346/9efb46bf6afd4a069218d5a7e800dcea/eyJhIjoxLCJwIjoxfQ%3D%3D/1.mp3?token-time=1699056000&token-hash=qiBgM1FPKkd3BSHdb7_XY7stv1tKaiIgceUC5NfzVs4%3D"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val vm = NowPlayingViewModel(this, Uri.parse(tempEpisodeUrl))

        setContent {
            PodcastsTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    var isPlaying by remember { mutableStateOf(false) }
                    vm.listen { isPlaying = it.isPlaying }
                    TempMediaPlayer(vm.play, vm.stop, isPlaying)
                }
            }
        }
    }
}
