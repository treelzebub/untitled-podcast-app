package net.treelzebub.podcasts.ui.components.bottomsheet

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import net.treelzebub.podcasts.R
import net.treelzebub.podcasts.ui.models.EpisodeUi
import net.treelzebub.podcasts.util.PreviewMocks
import kotlin.math.roundToInt

// TODO: User can't hide sheet. Sheet hides when media is stopped/cleared.
enum class MediaBottomSheetAnchor(val fraction: Float) {
    Start(0.1f), Half(0.5f), End(1f);
}

object MediaBottomSheetDefaults {
    val AnimationSpec = SpringSpec<Float>()
    val PositionalThreshold = { distance: Float -> distance * 0.2f }
    val VelocityThreshold = { 125f }
}

@Composable
fun MediaBottomSheet(onDismiss: () -> Unit) {
    val bottomSheetState = rememberMediaBottomSheetState(initialValue = MediaBottomSheetAnchor.Start)
    val coroutineScope = rememberCoroutineScope()

    MediaBottomSheetScaffold(
        bottomSheetState = bottomSheetState,
        bottomSheetContent = { BottomSheetContent() },
        sheetBackgroundColor = Color.LightGray,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 24.dp)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "~ Bottom Sheet Playground ~",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    coroutineScope.launch {
                        if (bottomSheetState.isVisible) bottomSheetState.expand()
                        else bottomSheetState.halfExpand()
                    }
                }
            ) {
                Text(text = "Show")
            }
        }
    }
}

@Composable
fun BottomSheetContent() {
    Column(
        modifier = Modifier.padding(32.dp)
    ) {
        Text(
            text = "Bottom sheet",
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(32.dp))
        LazyColumn {
            items(100) {
                Text(
                    text = "Item $it",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaBottomSheetScaffold(
    bottomSheetState: MediaBottomSheetState,
    bottomSheetContent: @Composable BoxScope.() -> Unit,
    modifier: Modifier = Modifier,
    sheetShape: Shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    sheetBackgroundColor: Color = Color.White,
    content: @Composable ColumnScope.() -> Unit
) {
    var layoutHeight by remember { mutableIntStateOf(0) }
    var sheetHeight by remember { mutableIntStateOf(0) }
    val bottomSheetNestedScrollConnection = remember(bottomSheetState.draggableState) {
        BottomSheetNestedScrollConnection(
            state = bottomSheetState.draggableState,
            orientation = Orientation.Vertical
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged {
                layoutHeight = it.height
                if (layoutHeight > 0 && sheetHeight > 0) {
                    bottomSheetState.updateAnchors(layoutHeight, sheetHeight)
                }
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            content()
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.BottomCenter)
                .offset {
                    val yOffset = bottomSheetState
                        .requireOffset()
                        .roundToInt()
                    IntOffset(x = 0, y = yOffset)
                }
                .anchoredDraggable(
                    state = bottomSheetState.draggableState,
                    orientation = Orientation.Vertical
                )
                .nestedScroll(bottomSheetNestedScrollConnection)
                .background(sheetBackgroundColor, sheetShape)
                .padding(vertical = 16.dp),
        ) {
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .onSizeChanged {
                        sheetHeight = it.height
                        if (layoutHeight > 0 && sheetHeight > 0) {
                            bottomSheetState.updateAnchors(layoutHeight, sheetHeight)
                        }
                    },
                content = bottomSheetContent
            )
        }
    }
}


/**
 * ~ TODO ~
 */
@Composable
fun MediaBottomSheetStatelessContent(
    episode: EpisodeUi,
    xOffset: Int,
    @DrawableRes icon: Int,
    onTogglePlaybackState: () -> Unit,
    onTap: (Offset) -> Unit
) {
    Box(modifier = Modifier
        .offset { IntOffset(xOffset, 0) }
        .navigationBarsPadding()
        .height(64.dp)
        .fillMaxWidth()
        .pointerInput(Unit) { detectTapGestures(onTap = onTap) }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(episode.imageUrl),
                contentDescription = "image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(64.dp),
            )

            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(8.dp),
            ) {
                Text(
                    episode.channelTitle,
//                    style = Type,
//                    color = MaterialTheme.colors.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    episode.title,
//                    style = MaterialTheme.typography.body2,
//                    color = MaterialTheme.colors.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.graphicsLayer { alpha = 0.60f }
                )
            }

            Icon(
                painter = painterResource(icon),
                contentDescription = "play",
//                tint = MaterialTheme.colors.onBackground,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onTogglePlaybackState)
                    .padding(6.dp)
            )
        }
    }
}

@Preview(name = "Bottom Bar")
@Composable
fun PodcastBottomBarPreview() {
    MediaBottomSheetStatelessContent(
        episode = PreviewMocks.Episode,
        xOffset = 0,
        icon = R.drawable.account_circle,
        onTogglePlaybackState = { },
        onTap = { }
    )
}
