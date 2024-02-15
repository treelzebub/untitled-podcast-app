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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.treelzebub.podcasts.App
import net.treelzebub.podcasts.BuildConfig
import net.treelzebub.podcasts.Database
import javax.inject.Inject

private class _DebugMode constructor(
    private val app: Application,
    private val db: Database,
    private val repo: PodcastsRepo
) {
    init {
        if (!BuildConfig.DEBUG) throw IllegalAccessException("Accessed Debug Mode in non-debug build!")
    }

    fun populateSubs() {
        CoroutineScope(Dispatchers.IO).launch {
            app.assets.open("test-feed-urls.txt").bufferedReader().use {
                it.forEachLine {
                    // Seems like a compiler bug: if I just try to call this suspend fun or even use `withContext()`
                    // I get a compilation error that I'm not in a coroutine context :shruggies:
                    // Dumb hack for debug mode, who cares.
                    CoroutineScope(Dispatchers.IO).launch { repo.fetchRssFeed(it, {}) }
                }
            }
        }
    }

    fun nukeSubs() {
        db.podcastsQueries.delete_all()
        db.episodesQueries.delete_all()
    }
}

@HiltViewModel
private class _DebugViewModel @Inject constructor(
    app: Application,
    db: Database,
    repo: PodcastsRepo
) : AndroidViewModel(app) {

    private val debug = _DebugMode(getApplication<App>(), db, repo)
    fun populateSubs() = debug.populateSubs()
    fun nukeSubs() = debug.nukeSubs()
}

@Composable
fun DebugMenu() {
    val vm = hiltViewModel<_DebugViewModel>()
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(imageVector = Icons.Outlined.MoreVert, contentDescription = "")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text(text = "Populate Subs") }, onClick = { vm.populateSubs() })
            DropdownMenuItem(text = { Text(text = "Nuke Subs") }, onClick = { vm.nukeSubs() })
        }
    }
}