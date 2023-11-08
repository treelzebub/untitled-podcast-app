package net.treelzebub.podcasts.data

import net.treelzebub.podcasts.ui.models.PodcastUi
import javax.inject.Inject

class PodcastsRepo @Inject constructor(
    private val db: DatabaseManager
) {

    fun listenForPodcasts(listener: (List<PodcastUi>) -> Unit) {
        db.listenForPodcasts {
            val raw = db.getAllPodcasts()
            val podcasts = raw.map {
                PodcastUi(
                    it.link, it.title, it.description.orEmpty(), it.email.orEmpty(),
                    it.image_url.orEmpty(), it.last_fetched, it.rss_link
                )
            }
            listener(podcasts)
        }
    }
}