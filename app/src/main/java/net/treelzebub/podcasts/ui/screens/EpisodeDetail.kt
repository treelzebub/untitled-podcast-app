package net.treelzebub.podcasts.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import net.treelzebub.podcasts.ui.vm.EpisodeDetailViewModel

@Destination
@Composable
fun EpisodeDetail(id: String) {
    val vm: EpisodeDetailViewModel = hiltViewModel()
    val episode by remember { vm.state }.collectAsState()
    vm.getEpisode(id)

}