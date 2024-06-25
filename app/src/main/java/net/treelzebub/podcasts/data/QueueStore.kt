package net.treelzebub.podcasts.data

import android.app.Application
import android.content.Context
import com.squareup.moshi.JsonDataException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import net.treelzebub.podcasts.di.IoDispatcher
import net.treelzebub.podcasts.ui.models.EpisodeUi
import net.treelzebub.podcasts.util.ErrorHandler
import java.io.FileNotFoundException
import java.io.IOException
import javax.inject.Inject


data class PodcastQueue(val list: List<EpisodeUi> = emptyList())

class QueueStore @Inject constructor(
    private val app: Application,
    private val serializer: StringSerializer<PodcastQueue>,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    companion object {
        private const val NAME = "queue.json"
    }

    private val _stateFlow = MutableStateFlow(PodcastQueue())

    fun stateFlow(
        scope: CoroutineScope,
        timeout: Long,
        initialValue: PodcastQueue = PodcastQueue()
    ): SharedFlow<PodcastQueue> {
        return _stateFlow.stateIn(scope, SharingStarted.WhileSubscribed(timeout), initialValue)
    }

    suspend fun persist(onError: ErrorHandler) {
        try {
            withContext(ioDispatcher) {
                _stateFlow.collectLatest {
                    val json = serializer.serialize(it)
                    app.openFileOutput(NAME, Context.MODE_PRIVATE).use { stream ->
                        stream.write(json.encodeToByteArray())
                    }
                }
            }
        } catch (e: AssertionError) {
            // Moshi toJson puked
            onError(e)
        } catch (e: IOException) {
            onError(e)
        } catch (e: FileNotFoundException) {
            onError(e)
        }
    }

    suspend fun load(onError: ErrorHandler) {
        try {
            val queue = withContext(ioDispatcher) {
                app.openFileInput(NAME).bufferedReader().useLines {
                    val str = it.joinToString("")
                    serializer.deserialize<PodcastQueue>(str)
                }
            }
            _stateFlow.emit(queue)
        } catch (e: IOException) {
            onError(e)
        } catch (e: JsonDataException) {
            onError(e)
        }
    }

    suspend fun add(episodeUi: EpisodeUi, onError: ErrorHandler) {
        update(onError) { queue ->
            queue.copy(list = queue.list.toMutableList().apply { add(episodeUi) })
        }
    }

    suspend fun add(index: Int, episodeUi: EpisodeUi, onError: ErrorHandler) {
        update(onError) { queue ->
            val list = queue.list
            if (index !in list.indices) throw IndexOutOfBoundsException("Invalid index: $index")
            queue.copy(list = list.toMutableList().apply { add(index, episodeUi) })
        }
    }

    suspend fun reorder(from: Int, to: Int, onError: ErrorHandler) {
        update(onError) { queue ->
            val list = queue.list
            queue.copy(list = list.toMutableList().apply {
                if (from in indices && to in indices) {
                    add(to, removeAt(from))
                } else throw IndexOutOfBoundsException("Invalid index: from=$from, to=$to")
            })
        }
    }

    suspend fun remove(episodeId: String, onError: ErrorHandler) {
        update(onError) { queue ->
            val list = queue.list
            val episode = list.find { it.id == episodeId } ?: throw NoSuchElementException("No episode with id: $episodeId")
            queue.copy(list = list.toMutableList().apply { remove(episode) })
        }
    }

    suspend fun remove(index: Int, onError: ErrorHandler) {
        update(onError) { queue ->
            val list = queue.list
            if (index !in list.indices) throw IndexOutOfBoundsException()
            queue.copy(list = list.toMutableList().apply { removeAt(index) })
        }
    }

    suspend fun removeByPodcastId(podcastId: String, onError: ErrorHandler) {
        update(onError) { queue ->
            queue.copy(list = queue.list.toMutableList().apply {
                removeAll { it.podcastId == podcastId }
            })
        }
    }

    private suspend fun update(onError: ErrorHandler, block: (PodcastQueue) -> PodcastQueue) {
        _stateFlow.update(block)
        persist(onError)
    }
}
