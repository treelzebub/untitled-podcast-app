package net.treelzebub.podcasts.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.LineBreak.Strategy.Companion.Balanced
import androidx.compose.ui.unit.sp

object TextStyles {

    val CardTitle = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
    )
    val CardSubtitle = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold
    )
    val CardDescription = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = Color.Gray,
        lineBreak = LineBreak.Paragraph.copy(strategy = Balanced),
        hyphens = Hyphens.Auto
    )
    val CardDate = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        color = Color.Black
    )
}