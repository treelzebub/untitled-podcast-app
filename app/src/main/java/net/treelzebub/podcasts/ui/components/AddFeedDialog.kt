package net.treelzebub.podcasts.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun AddFeedDialog() {
    AlertDialog(
        title = { Text(text = "Add RSS Feed") },
        onDismissRequest = { /*TODO*/ },
        confirmButton = { /*TODO*/ }
    )
}