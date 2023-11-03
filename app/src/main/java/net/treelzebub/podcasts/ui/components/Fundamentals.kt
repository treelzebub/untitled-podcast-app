package net.treelzebub.podcasts.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ItemCard(
    modifier: Modifier = Modifier,
    fn: @Composable ColumnScope.() -> Unit
) {
    Card(
        Modifier
            .padding(start = 12.dp, top = 0.dp, end = 12.dp, bottom = 6.dp)
            .height(IntrinsicSize.Min)
            .then(modifier)
    ) { fn() }
}