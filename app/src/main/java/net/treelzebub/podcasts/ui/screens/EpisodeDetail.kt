package net.treelzebub.podcasts.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import net.treelzebub.podcasts.ui.vm.EpisodeDetailViewModel

@Destination
@Composable
fun EpisodeDetail(episodeId: String) {
    val vm = hiltViewModel<EpisodeDetailViewModel, EpisodeDetailViewModel.Factory>(
        creationCallback = { factory -> factory.create(episodeId = episodeId) }
    )
    val episode by remember { vm.state }.collectAsState()

    Text(text = "Episode Details")
}