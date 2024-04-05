package net.treelzebub.podcasts.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import net.treelzebub.podcasts.ui.components.bottomsheet.MediaBottomSheet

@Composable
fun DebugScreen() {
    var showBottomSheet by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showBottomSheet = true }
                ) { Icon(Icons.Filled.Home, contentDescription = "") }
            }
        ) { contentPadding ->
            // Screen content
            contentPadding
            if (showBottomSheet) {
                MediaBottomSheet { showBottomSheet = false }
            }
        }
    }
}
