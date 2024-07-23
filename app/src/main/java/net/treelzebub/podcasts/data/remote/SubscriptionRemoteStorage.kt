package net.treelzebub.podcasts.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import net.treelzebub.podcasts.data.PodcastsRepo
import net.treelzebub.podcasts.data.StringSerializer
import net.treelzebub.podcasts.net.models.SubscriptionDto
import javax.inject.Inject

class SubscriptionRemoteStorage @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val repo: PodcastsRepo,
    private val serializer: StringSerializer<SubscriptionDto>
) : RemoteStorage<SubscriptionDto> {

    override fun save(data: SubscriptionDto) {
        val subs = repo.getAllRssLinks()
        val str = serializer.serialize(subs)
        firestore.collection("subs").add(str)
    }

    override fun load(): SubscriptionDto {

    }

    private fun identify() {
//        auth.token
    }
}
