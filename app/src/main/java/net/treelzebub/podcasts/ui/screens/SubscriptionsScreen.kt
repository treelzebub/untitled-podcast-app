package net.treelzebub.podcasts.ui.screens

import android.util.Patterns
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import net.treelzebub.podcasts.R
import net.treelzebub.podcasts.ui.components.AddFeedDialog
import net.treelzebub.podcasts.ui.components.LoadingBox
import net.treelzebub.podcasts.ui.components.PodcastList
import net.treelzebub.podcasts.ui.vm.SubscriptionsViewModel
import net.treelzebub.podcasts.util.toast


private fun validateUrl(url: String): Boolean = Patterns.WEB_URL.matcher(url).matches()

@UnstableApi
@RootNavGraph(start = true)
@Destination
@Composable
fun SubscriptionsScreen(navigator: DestinationsNavigator) {
    val vm = hiltViewModel<SubscriptionsViewModel>()
    val state by remember { vm.state }.collectAsState()
    val showDialog = remember { mutableStateOf(false) }

    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val invalidUrl = stringResource(R.string.invalid_rss_url)
    val errorFetchParse = stringResource(R.string.error_fetch_parse)

    val onDismiss = { showDialog.value = false }
    val onPaste = { clipboard.getText()?.text.orEmpty() }
    val onConfirm = { input: String ->
        if (input.isEmpty()) {
            onDismiss()
        } else if (validateUrl(input)) {
            vm.addRssFeed(input) { toast(context, errorFetchParse) }
            onDismiss()
        } else {
            toast(context, invalidUrl)
        }
    }

    if (showDialog.value) AddFeedDialog(onConfirm, onPaste, onDismiss)

    if (state.loading) {
        LoadingBox()
    } else {
        Scaffold(
            floatingActionButton = { Fab { showDialog.value = true } }
        ) { contentPadding ->
            PodcastList(navigator, state.podcasts, contentPadding)
        }
    }
}

@Composable
fun Fab(onClick: () -> Unit) {
    SmallFloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.secondary
    ) {
        Icon(Icons.Filled.Add, "Tap to add an RSS feed.")
    }
}