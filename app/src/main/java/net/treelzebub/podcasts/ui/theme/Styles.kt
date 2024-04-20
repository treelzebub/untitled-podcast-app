package net.treelzebub.podcasts.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object TextStyles {

    val CardTitle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
    val CardSubtitle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    val CardDescription = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
    val CardDate = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
}