package net.treelzebub.podcasts.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.treelzebub.podcasts.R
import net.treelzebub.podcasts.ui.screens.DiscoverScreen
import net.treelzebub.podcasts.ui.screens.ProfileScreen
import net.treelzebub.podcasts.ui.screens.SettingsScreen
import net.treelzebub.podcasts.ui.screens.SubscriptionsScreen

private data class TabData(
    @StringRes val text: Int,
    @DrawableRes val image: Int
)

@Composable
fun TabsBar() {
    val tabs = listOf(
        TabData(R.string.tab_subscriptions, R.drawable.subscriptions),
        TabData(R.string.tab_discover, R.drawable.search),
        TabData(R.string.tab_profile, R.drawable.account_circle),
        TabData(R.string.tab_settings, R.drawable.settings)
    )
    val tabStyle = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight(700),
        color = Color.Black,
        textAlign = TextAlign.Center
    )
    var tabPosition by remember { mutableIntStateOf(0) }

    TabRow(
        modifier = Modifier.fillMaxWidth(),
        selectedTabIndex = tabPosition,
        indicator = { tabPositions ->
            Box(
                Modifier
                    .tabIndicatorOffset(tabPositions[tabPosition])
                    .height(4.dp)
                    .background(color = Color.Black, shape = RoundedCornerShape(4.dp)))
        },
        divider = {}
    ) {
        tabs.forEachIndexed { i, it ->
            Tab(
                modifier = Modifier
                    .wrapContentWidth(align = Alignment.CenterHorizontally)
                    .background(color = Color.White),
                selected = i == tabPosition,
                onClick = { tabPosition = i }
            ) {
                Column(Modifier.wrapContentWidth(align = Alignment.CenterHorizontally)) {
                    Image(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 2.dp),
                        painter = painterResource(id = it.image),
                        contentDescription = stringResource(it.text)
                    )
                    Text(
                        modifier = Modifier
                            .padding(bottom = 6.dp),
                        color = Color.Black,
                        style = tabStyle,
                        text = stringResource(it.text)
                    )
                }
            }
        }
    }
    when (tabPosition) {
        0 -> SubscriptionsScreen()
        1 -> DiscoverScreen({}, listOf(), {})
        2 -> ProfileScreen()
        3 -> SettingsScreen()
    }
}