package net.treelzebub.podcasts.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.treelzebub.podcasts.Database
import net.treelzebub.podcasts.di.IoDispatcher
import net.treelzebub.podcasts.ui.models.EpisodeUi
import javax.inject.Inject


class QueueStore @Inject constructor(
    private val db: Database,
    @IoDispatcher private val ioDispatcher: CoroutineScope
) {
    fun queueFlow(): Flow<List<EpisodeUi>> {
        return flow { listOf<EpisodeUi>() }
    }
}