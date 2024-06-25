package net.treelzebub.podcasts.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import net.treelzebub.podcasts.util.Pod01_Ep01
import net.treelzebub.podcasts.util.Pod01_Ep02
import net.treelzebub.podcasts.util.Pod02_Ep01
import net.treelzebub.podcasts.util.Pod02_Ep02
import net.treelzebub.podcasts.util.Pod02_Ep03
import net.treelzebub.podcasts.util.TestCoroutines
import net.treelzebub.podcasts.util.queueStore
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull


@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class QueueStoreTests {

    @Before
    fun setUp() {
        Dispatchers.setMain(TestCoroutines.dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Add to Queue`() = runTest {
        val store = queueStore()
        val flow = store.stateFlow

        store.add(Pod01_Ep01) {}
        assertEquals(1, flow.value.list.size)
        assertEquals(Pod01_Ep01, flow.value.list.first())

        store.add(Pod01_Ep02) {}
        assertEquals(2, flow.value.list.size)
        assertEquals(Pod01_Ep01, flow.value.list[0])
        assertEquals(Pod01_Ep02, flow.value.list[1])

        store.add(1, Pod02_Ep01) {}
        assertEquals(3, flow.value.list.size)
        assertEquals(Pod01_Ep01, flow.value.list[0])
        assertEquals(Pod02_Ep01, flow.value.list[1])
        assertEquals(Pod01_Ep02, flow.value.list[2])

        var exception: Throwable? = null
        var message = "I did not change"
        store.add(12, Pod02_Ep01) {
            exception = it
            message = "Invalid index: 12"
        }
        assertIs<IndexOutOfBoundsException>(exception)
        assertEquals("Invalid index: 12", message)
    }

    @Test
    fun `Persist and Load`() = runTest {
        val store = queueStore()
        val queue = PodcastQueue(listOf(Pod02_Ep01, Pod02_Ep02, Pod02_Ep03))

        store.set(queue) {}
        val fromDisk = store.load {}
        assertNotNull(fromDisk)
        assertEquals(queue, fromDisk)
    }

    @Test
    fun reorder() = runTest {
        val store = queueStore()
        val queue = PodcastQueue(listOf(Pod01_Ep01, Pod01_Ep02, Pod02_Ep01, Pod02_Ep02, Pod02_Ep03))

        store.set(queue) {}
        store.reorder(2, 4) {}

        val reordered = PodcastQueue(listOf(Pod01_Ep01, Pod01_Ep02, Pod02_Ep02, Pod02_Ep03, Pod02_Ep01))
        val value = store.stateFlow.value
        assertEquals(reordered, value)

        // Index out of bounds invokes onError and does not reorder queue.
        var exception: Throwable? = null
        var message = "I did not change"
        store.reorder(12, 1) {
            exception = it
            message = it.message!!
        }
        assertEquals(reordered, value)
        assertIs<IndexOutOfBoundsException>(exception)
        assertEquals("Invalid index: from=12, to=1", message)
    }

    @Test
    fun remove() = runTest {
        val store = queueStore()
        val queue = PodcastQueue(listOf(Pod01_Ep01, Pod01_Ep02, Pod02_Ep01, Pod02_Ep02, Pod02_Ep03))
        val flow = store.stateFlow

        store.set(queue) {}
        store.remove(Pod01_Ep02.id) {}
        val removed1 = PodcastQueue(listOf(Pod01_Ep01, Pod02_Ep01, Pod02_Ep02, Pod02_Ep03))
        assertEquals(removed1, flow.value)

        store.remove(Pod02_Ep03.id) {}
        val removed2 = PodcastQueue(listOf(Pod01_Ep01, Pod02_Ep01, Pod02_Ep02))
        assertEquals(removed2, flow.value)

        var exception1: Throwable? = null
        var message1 = "I did not change"
        store.remove(Pod02_Ep03.id) {
            exception1 = it
            message1 = it.message!!
        }
        assertIs<NoSuchElementException>(exception1)
        assertEquals("No episode with id: ${Pod02_Ep03.id}", message1)

        var exception2: Throwable? = null
        var message2 = "I did not change"
        store.remove(4) {
            exception2 = it
            message2 = it.message!!
        }
        assertIs<IndexOutOfBoundsException>(exception2)
        assertEquals("Invalid index: 4", message2)
    }

    @Test
    fun removeByPodcastId() = runTest {
        val store = queueStore()
        val queue = PodcastQueue(listOf(Pod01_Ep01, Pod01_Ep02, Pod02_Ep01, Pod02_Ep02, Pod02_Ep03))
        val flow = store.stateFlow

        store.set(queue) {}

        var message = "I won't change till the second removeByPodcastId invocation."
        store.removeByPodcastId(Pod02_Ep01.podcastId) {
            // Explodes on non-null assertion if invoked.
            message = it.message!!
        }
        val removed3 = PodcastQueue(listOf(Pod01_Ep01, Pod01_Ep02))
        assertEquals("I won't change till the second removeByPodcastId invocation.", message)
        assertEquals(removed3, flow.value)

        var exception: Throwable? = null
        store.removeByPodcastId(Pod02_Ep01.podcastId) {
            exception = it
            message = it.message!!
        }
        assertIs<NoSuchElementException>(exception)
        assertEquals("No episodes in queue with podcastId: ${Pod02_Ep01.podcastId}", message)
    }
}
