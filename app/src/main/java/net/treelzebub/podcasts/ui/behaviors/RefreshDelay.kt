package net.treelzebub.podcasts.ui.behaviors

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay

fun runAtLeast(minimumDuration: Long, block: suspend () -> Unit): suspend () -> Unit = {
    val start = System.currentTimeMillis()
    block()
    val elapsed = System.currentTimeMillis() - start
    if (elapsed < minimumDuration) delay(minimumDuration - elapsed)
}

@Composable
fun PullToRefreshLaunchedEffect(
    key: Any?,
    minimumDuration: Long = 1_000L,
    block: suspend () -> Unit
) {
    LaunchedEffect(key) {
        runAtLeast(minimumDuration) { block() }
    }
}
