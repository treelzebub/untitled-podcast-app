package net.treelzebub.podcasts.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.treelzebub.podcasts.media.PodcastPlayer

@Composable
fun TempMediaPlayer(play: () -> Unit, stop: () -> Unit, isPlaying: Boolean) {
    val text = if (isPlaying) "Pause" else "Play"
    Column(verticalArrangement = Arrangement.Center) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                modifier = Modifier.padding(end = 6.dp),
                onClick = play
            ) {
                Text(text)
            }
            Button(
                onClick = stop
            ) {
                Text("Stop")
            }
        }
    }
}