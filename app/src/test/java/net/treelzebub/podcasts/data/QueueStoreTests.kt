package net.treelzebub.podcasts.data

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import net.treelzebub.podcasts.util.Pod01_Ep01
import net.treelzebub.podcasts.util.TestCoroutines
import net.treelzebub.podcasts.util.assertEmpty
import net.treelzebub.podcasts.util.queueStore
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals


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

    @Test fun `Add to Queue`() = runTest {
        val store = queueStore()
        val flow = store.stateFlow(TestCoroutines.scope, 60L)

        store.stateFlow.test {
            store.add(Pod01_Ep01) { throw IllegalStateException() }
            assertEquals(1, awaitItem().list.size)
        }
//        flow.test {
//            val a = awaitItem()
//            assertEquals(1, a.list.size)
//        }
    //        job.cancel()

    //        store.add(Pod01_Ep02) {}
    //        val twoItemQueue = store.stateFlow(TestCoroutines.scope, 10L).single()
    //        assertEquals(2, oneItemQueue.list.size)
    //        assertEquals(Pod01_Ep01, twoItemQueue.list[0])
    //        assertEquals(Pod01_Ep02, twoItemQueue.list[1])
//        }
    }
}