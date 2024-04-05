package net.treelzebub.podcasts.ui.components.bottomsheet

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.key
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import kotlin.math.min

@OptIn(ExperimentalFoundationApi::class)
@Stable
class MediaBottomSheetState(
    initialValue: MediaBottomSheetAnchor,
    val animationSpec: AnimationSpec<Float> = MediaBottomSheetDefaults.AnimationSpec,
    val confirmValueChange: (MediaBottomSheetAnchor) -> Boolean = { true }
) {

    companion object {

        // Default impl
        fun Saver(
            confirmValueChange: (MediaBottomSheetAnchor) -> Boolean = { true },
            animationSpec: AnimationSpec<Float> = MediaBottomSheetDefaults.AnimationSpec
        ): Saver<MediaBottomSheetState, MediaBottomSheetAnchor> =
            Saver(
                save = { it.currentValue },
                restore = {
                    MediaBottomSheetState(
                        initialValue = it,
                        animationSpec = animationSpec,
                        confirmValueChange = confirmValueChange
                    )
                }
            )
    }

    val draggableState = AnchoredDraggableState(
        initialValue = initialValue,
        animationSpec = animationSpec,
        positionalThreshold = MediaBottomSheetDefaults.PositionalThreshold,
        velocityThreshold = MediaBottomSheetDefaults.VelocityThreshold,
        confirmValueChange = confirmValueChange
    )

    val currentValue: MediaBottomSheetAnchor
        get() = draggableState.currentValue

    val targetValue: MediaBottomSheetAnchor
        get() = draggableState.targetValue

    val isVisible: Boolean
        get() = currentValue != MediaBottomSheetAnchor.Start

    val isEnd: Boolean
        get() = currentValue == MediaBottomSheetAnchor.End

    val isHalfExpanded: Boolean
        get() = currentValue == MediaBottomSheetAnchor.Half

    val isStart: Boolean
        get() = currentValue == MediaBottomSheetAnchor.Start

    val hasHalfExpandedState: Boolean
        get() = draggableState.anchors.hasAnchorFor(MediaBottomSheetAnchor.Half)

    suspend fun show() {
        val targetValue = when {
            hasHalfExpandedState -> MediaBottomSheetAnchor.Half
            else -> MediaBottomSheetAnchor.End
        }
        animateTo(targetValue)
    }

    suspend fun expand() {
        if (draggableState.anchors.hasAnchorFor(MediaBottomSheetAnchor.End)) {
            animateTo(MediaBottomSheetAnchor.End)
        }
    }

    suspend fun halfExpand() {
        if (draggableState.anchors.hasAnchorFor(MediaBottomSheetAnchor.Half)) {
            animateTo(MediaBottomSheetAnchor.Half)
        }
    }

    suspend fun hide() = animateTo(MediaBottomSheetAnchor.Start)

    fun requireOffset() = draggableState.requireOffset()

    fun updateAnchors(layoutHeight: Int, sheetHeight: Int) {
        val maxDragEndPoint = layoutHeight - 32 //.dp.toPixel
        val newAnchors = DraggableAnchors {
            MediaBottomSheetAnchor.entries.forEach { anchor ->
                val fractionalMaxDragEndPoint = maxDragEndPoint * anchor.fraction
                val dragEndPoint = layoutHeight - min(fractionalMaxDragEndPoint, sheetHeight.toFloat())
                anchor at dragEndPoint
            }
        }
        draggableState.updateAnchors(newAnchors)
    }

    suspend fun animateTo(
        targetValue: MediaBottomSheetAnchor,
        velocity: Float = draggableState.lastVelocity
    ) = draggableState.animateTo(targetValue, velocity)
}

@Composable
fun rememberMediaBottomSheetState(
    initialValue: MediaBottomSheetAnchor,
    animationSpec: AnimationSpec<Float> = MediaBottomSheetDefaults.AnimationSpec,
    confirmValueChange: (MediaBottomSheetAnchor) -> Boolean = { true },
): MediaBottomSheetState {
    return key(initialValue) {
        rememberSaveable(
            initialValue, animationSpec, confirmValueChange,
            saver = MediaBottomSheetState.Saver(
                confirmValueChange = confirmValueChange,
                animationSpec = animationSpec
            )
        ) {
            MediaBottomSheetState(
                initialValue = initialValue,
                animationSpec = animationSpec,
                confirmValueChange = confirmValueChange
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
class BottomSheetNestedScrollConnection(
    private val state: AnchoredDraggableState<MediaBottomSheetAnchor>,
    private val orientation: Orientation
) : NestedScrollConnection {
    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        val delta = available.offsetToFloat()
        return if (delta < 0 && source == NestedScrollSource.Drag) {
            state.dispatchRawDelta(delta).toOffset()
        } else Offset.Zero
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        val delta = available.offsetToFloat()
        return if (source == NestedScrollSource.Drag) {
            state.dispatchRawDelta(delta).toOffset()
        } else Offset.Zero
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        val toFling = available.velocityToFloat()
        val currentOffset = state.requireOffset()
        return if (toFling < 0 && currentOffset > state.anchors.minAnchor()) {
            state.settle(toFling)
            available
        } else Velocity.Zero
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        val toFling = available.velocityToFloat()
        state.settle(toFling)
        return available
    }

    private fun Offset.offsetToFloat(): Float = if (orientation == Orientation.Horizontal) x else y

    private fun Float.toOffset(): Offset = Offset(
        x = if (orientation == Orientation.Horizontal) this else 0f,
        y = if (orientation == Orientation.Vertical) this else 0f
    )

    private fun Velocity.velocityToFloat() = if (orientation == Orientation.Horizontal) x else y
}
