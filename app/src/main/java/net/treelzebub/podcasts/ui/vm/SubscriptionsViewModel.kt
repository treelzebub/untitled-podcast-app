package net.treelzebub.podcasts.ui.vm

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.treelzebub.podcasts.data.PodcastsRepo
import net.treelzebub.podcasts.net.sync.SubscriptionUpdater
import net.treelzebub.podcasts.ui.models.PodcastUi
import javax.inject.Inject


@HiltViewModel
class SubscriptionsViewModel @Inject constructor(
    private val repo: PodcastsRepo,
    private val subscriptionUpdater: SubscriptionUpdater
) : StatefulViewModel<SubscriptionsViewModel.State>(State()) {

    init {
        getAllPodcasts()
    }

    @Stable @Immutable
    data class State(
        val loading: Boolean = true,
        val podcasts: List<PodcastUi> = emptyList()
    )

    fun refresh() = subscriptionUpdater.updateAll({ loading(false) }, { _, _, _ -> TODO() })

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
