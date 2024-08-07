package net.treelzebub.podcasts.data

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.treelzebub.podcasts.BuildConfig
import net.treelzebub.podcasts.Database
import net.treelzebub.podcasts.di.IoDispatcher
import javax.inject.Inject


@HiltViewModel
private class _DebugViewModel @Inject constructor(
    private val app: Application,
    private val db: Database,
    private val repo: PodcastsRepo,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : AndroidViewModel(app) {

    init {
        if (!BuildConfig.DEBUG) throw IllegalAccessException("Accessed Debug Mode in non-debug build!")
    }

    fun mySubs() = fetch("test-my-subs.txt")

    fun testSubs() = fetch("test-feed-urls.txt")

    fun nukeSubs() {
        db.podcastsQueries.delete_all()
        assert(db.episodesQueries.get_all().executeAsList().isEmpty())
    }

    private fun fetch(file: String) {
        val scope = CoroutineScope(ioDispatcher)
        app.assets.open(file).bufferedReader().use {
            it.forEachLine {
                scope.launch { repo.fetchRssFeed(it) {} }
            }
        }
    }
}

@Composable
fun DebugMenu() {
    val vm = hiltViewModel<_DebugViewModel>()
    var expanded by remember { mutableStateOf(false) }

    fun toggleAnd(fn: () -> Unit) {
        expanded = !expanded
        fn()
    }

    Box {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(imageVector = Icons.Outlined.MoreVert, contentDescription = "")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text(text = "My Subs") }, onClick = { toggleAnd { vm.mySubs() } })
            DropdownMenuItem(text = { Text(text = "Test Subs") }, onClick = { toggleAnd { vm.testSubs() } })
            DropdownMenuItem(text = { Text(text = "Nuke Subs") }, onClick = { toggleAnd { vm.nukeSubs() } })
        }
    }
}
