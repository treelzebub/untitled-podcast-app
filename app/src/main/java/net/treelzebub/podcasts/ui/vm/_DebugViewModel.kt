package net.treelzebub.podcasts.ui.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import net.treelzebub.podcasts.App
import net.treelzebub.podcasts.Database
import net.treelzebub.podcasts.data.PodcastsRepo
import net.treelzebub.podcasts.data._DebugMode
import javax.inject.Inject

@HiltViewModel
class _DebugViewModel @Inject constructor(
    app: Application,
    db: Database,
    repo: PodcastsRepo
) : AndroidViewModel(app) {

    private val debug = _DebugMode(getApplication<App>(), db, repo)
    fun populateSubs() = debug.populateSubs()
    fun nukeSubs() = debug.nukeSubs()
}