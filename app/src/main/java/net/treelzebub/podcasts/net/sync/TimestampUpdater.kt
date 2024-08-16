package net.treelzebub.podcasts.net.sync

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.treelzebub.podcasts.Episode
import net.treelzebub.podcasts.data.PodcastsRepo
import net.treelzebub.podcasts.di.DefaultDispatcher
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Update our local podcasts with the latest unplayed episode timestamp, so we can sort descending
 * by it on our main subscriptions screen.
 */
@Singleton
class TimestampUpdater @Inject constructor(
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    private val repo: PodcastsRepo
) {
    private val scope = CoroutineScope(SupervisorJob() + defaultDispatcher)

    fun update() {
        scope.launch {
            val podcasts = repo.getPodcasts().map {
                val latest = repo.getUnplayedEpisodes(it.id).maxOf(Episode::date)
                it.copy(latest_episode_timestamp = latest)
            }
            repo.upsertPodcasts(podcasts)
        }
    }
}
