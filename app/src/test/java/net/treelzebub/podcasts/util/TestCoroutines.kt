package net.treelzebub.podcasts.util

import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope

object TestCoroutines {
    val scheduler = TestCoroutineScheduler()
    val dispatcher = StandardTestDispatcher(scheduler)
    val scope = TestScope(dispatcher)
}
