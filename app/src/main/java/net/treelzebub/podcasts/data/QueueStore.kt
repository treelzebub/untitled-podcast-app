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
import java.io.FileNotFoundException
import java.io.IOException
import javax.inject.Inject


class QueueStore @Inject constructor(
    private val app: Application,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val serializer: StringSerializer<List<EpisodeUi>>
) {

    companion object {
        private const val NAME = "queue.json"
    }

    private val _stateFlow = MutableStateFlow<List<EpisodeUi>>(emptyList())

    fun stateFlow(
        scope: CoroutineScope,
        timeout: Long,
        initialValue: List<EpisodeUi> = emptyList()
    ): SharedFlow<List<EpisodeUi>> {
        return _stateFlow.stateIn(scope, SharingStarted.WhileSubscribed(timeout), initialValue)
    }

    suspend fun persist(onError: (Throwable) -> Unit) {
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

    suspend fun load(onError: (Throwable) -> Unit) {
        try {
            val queue = withContext(ioDispatcher) {
                app.openFileInput(NAME).bufferedReader().useLines {
                    val str = it.joinToString("")
                    serializer.deserialize<List<EpisodeUi>>(str)
                }
            }
            _stateFlow.emit(queue)
        } catch (e: IOException) {
            onError(e)
        } catch (e: JsonDataException) {
            onError(e)
        }
    }

    fun add(episodeUi: EpisodeUi) {
        update { list ->
            list.toMutableList().apply { add(episodeUi) }
        }
    }

    fun add(index: Int, episodeUi: EpisodeUi) {
        update { list ->
            if (index !in list.indices) throw IndexOutOfBoundsException("Invalid index: $index")
            list.toMutableList().apply { add(index, episodeUi) }
        }
    }

    fun reorder(from: Int, to: Int) {
        _stateFlow.update { list ->
            list.toMutableList().apply {
                if (from in indices && to in indices) {
                    add(to, removeAt(from))
                } else throw IndexOutOfBoundsException("Invalid index: from=$from, to=$to")
            }
        }
    }

    fun remove(episodeId: String) {
        update { list ->
            val episode = list.find { it.id == episodeId } ?: throw NoSuchElementException("No episode with id: $episodeId")
            list.toMutableList().apply { remove(episode) }
        }
    }

    fun remove(index: Int) {
        update { list ->
            if (index !in list.indices) throw IndexOutOfBoundsException()
            list.toMutableList().apply { removeAt(index) }
        }
    }

    private fun update(block: (List<EpisodeUi>) -> List<EpisodeUi>) = _stateFlow.update(block)
}
