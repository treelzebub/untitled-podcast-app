package net.treelzebub.podcasts.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.treelzebub.podcasts.R
import net.treelzebub.podcasts.ui.vm.EpisodeDetailsViewModel.Action.Download


@Composable
fun ItemCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .padding(start = 12.dp, top = 0.dp, end = 12.dp, bottom = 6.dp)
            .height(IntrinsicSize.Min)
            .then(modifier),
        shape = RoundedCornerShape(6.dp),
        onClick = onClick
    ) { content() }
}

@Composable
fun ButtonCircleBorderless(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    @DrawableRes res: Int,
    contentDescription: String,
    tint: Color = Color.Black,
    onClick: () -> Unit = {}
) {
    OutlinedButton(
        modifier = Modifier
            .size(size)
            .then(modifier),
        shape = CircleShape,
        border = BorderStroke(0.dp, Color.White),
        contentPadding = PaddingValues(0.dp),
        onClick = onClick

    ) {
        Icon(
            modifier = Modifier.size(size),
            painter = painterResource(id = res),
            tint = tint,
            contentDescription = contentDescription
        )
    }
}