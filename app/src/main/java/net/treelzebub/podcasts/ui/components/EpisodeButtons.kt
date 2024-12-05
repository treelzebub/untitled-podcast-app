package net.treelzebub.podcasts.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.treelzebub.podcasts.R


@Composable
fun EpisodeButtons() {
    FullWidthSingleButtonBar(
        modifier = Modifier.padding(),
        res = R.drawable.notif_play,
        contentDescription = "",
        onClick = {}
    )
}

@Composable
fun FullWidthSingleButtonBar(
    modifier: Modifier = Modifier,
    background: Color = Color.White,
    foreground: Color = Color.Black,
    @DrawableRes res: Int,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .background(background)
    ) {
        Spacer(
            modifier = Modifier
                .height(1.dp)
                .fillMaxWidth()
                .align(Alignment.Center)
                .background(foreground)
        )

        OutlinedButton(
            modifier = Modifier
                .size(48.dp)
                .background(background)
                .align(Alignment.Center)
                .then(modifier),
            shape = CircleShape,
            border = BorderStroke(1.dp, foreground),
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = foreground),
            onClick = onClick
        ) {
            Icon(
                modifier = Modifier,
                painter = painterResource(res),
                tint = foreground,
                contentDescription = contentDescription
            )
        }
    }
}

@Preview
@Composable
fun EpisodeButtonsPreview() {
    EpisodeButtons()
}
