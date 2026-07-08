package net.treelzebub.podcasts.ui.vm

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.treelzebub.podcasts.data.PodcastsRepo
import net.treelzebub.podcasts.net.sync.SubscriptionUpdater
import net.treelzebub.podcasts.net.sync.TimestampUpdater
import net.treelzebub.podcasts.ui.models.PodcastUi
import javax.inject.Inject


@HiltViewModel
class SubscriptionsViewModel @Inject constructor(
    private val repo: PodcastsRepo,
    private val subscriptionUpdater: SubscriptionUpdater,
    private val timestampUpdater: TimestampUpdater
) : StatefulViewModel<SubscriptionsViewModel.State>(State()) {

    init {
        getAllPodcasts()
    }

    @Stable @Immutable
    data class State(
        val loading: Boolean = true,
        val refreshing: Boolean = false,
        val podcasts: List<PodcastUi> = emptyList()
    )

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(refreshing = true) }
            subscriptionUpdater.updateAll()
            // TODO optimize. Should this happen whenever an episode is marked as played?
            timestampUpdater.update()
            _state.update { it.copy(refreshing = false) }
        }
    }

    private fun loading(loading: Boolean) = _state.update { it.copy(loading = loading) }

    private fun getAllPodcasts() {
        viewModelScope.launch {
            loading(true)
            repo.getPodcastUis().collect { currentState ->
                _state.update { it.copy(loading = false, podcasts = currentState) }
            }
        }
    }
}
