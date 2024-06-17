package net.treelzebub.podcasts.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.treelzebub.podcasts.ui.models.EpisodeUi
import javax.inject.Inject


class QueueStore @Inject constructor(
    private val repo: PodcastsRepo,
) {
    fun queueFlow(): Flow<List<EpisodeUi>> {
        return flow { listOf<EpisodeUi>() }
    }
}
