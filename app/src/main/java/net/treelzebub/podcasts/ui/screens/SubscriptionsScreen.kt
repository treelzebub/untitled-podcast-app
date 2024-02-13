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
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import net.treelzebub.podcasts.ui.components.AddFeedDialog
import net.treelzebub.podcasts.ui.components.PodcastList
import net.treelzebub.podcasts.ui.components.toast
import net.treelzebub.podcasts.ui.vm.SubscriptionsViewModel

private fun validateUrl(url: String): Boolean = Patterns.WEB_URL.matcher(url).matches()

@RootNavGraph(start = true)
@Destination
@Composable
fun SubscriptionsScreen(navigator: DestinationsNavigator, ) {
    val context = LocalContext.current
    val vm = hiltViewModel<SubscriptionsViewModel>()
    val podcasts by remember { vm.podcasts }.collectAsState(initial = emptyList())
    val showDialog = remember { mutableStateOf(false) }

    val clipboard = LocalClipboardManager.current

    val onDismiss = { showDialog.value = false }
    val onPaste = { clipboard.getText()?.text.orEmpty() }
    val onConfirm = { input: String ->
        if (validateUrl(input)) {
            vm.addRssFeed(input)
            onDismiss()
        } else {
            toast(context, "Invalid URL.")
        }
    }

    if (showDialog.value) AddFeedDialog(onConfirm, onPaste, onDismiss)

    Scaffold(
        floatingActionButton = { Fab { showDialog.value = true } }
    ) { contentPadding ->
        PodcastList(navigator, podcasts, contentPadding)
    }
}

@Composable
fun Fab(onClick: () -> Unit) {
    SmallFloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.secondary
    ) {
        Icon(Icons.Filled.Add, "Small floating action button.")
    }

}