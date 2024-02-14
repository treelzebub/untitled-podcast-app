package net.treelzebub.podcasts.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import net.treelzebub.podcasts.R

@Composable
fun AddFeedDialog(
    onConfirm: (String) -> Unit,
    onPaste: () -> String,
    onDismissRequest: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val focusRequester = FocusRequester()

    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(unbounded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White, RoundedCornerShape(6.dp))
                    .wrapContentHeight()
                    .padding(8.dp)
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RectangleShape)
                        .padding(8.dp)
                        .focusRequester(focusRequester),
                    textStyle = TextStyle(fontSize = 14.sp),
                    singleLine = true,
                    label = { Text("RSS Link") },
                    placeholder = {
                        Text(
                            fontSize = 14.sp,
                            color = Color.LightGray,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                            text = "https://www.example.org/podcast.rss"
                        )
                    },
                    maxLines = 1,
                    trailingIcon = {
                        Icon(
                            modifier = Modifier.clickable { inputText = onPaste() },
                            painter = painterResource(id = R.drawable.content_paste),
                            contentDescription = "Paste icon. Tap to paste URL you have copied."
                        )
                    },
                    value = inputText,
                    onValueChange = { inputText = it }
                )

                LaunchedEffect(Unit) { focusRequester.requestFocus() }

                Row(
                    modifier = Modifier
                        .wrapContentSize()
                        .align(Alignment.End)
                ) {
                    Button(
                        modifier = Modifier.padding(end = 6.dp),
                        onClick = { onDismissRequest() }
                    ) {
                        Text("Cancel")
                    }
                    Button(onClick = { onConfirm(inputText) }) {
                        Text("OK")
                    }
                }
            }
        }
    }
}
