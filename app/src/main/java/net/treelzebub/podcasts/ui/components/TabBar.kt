package net.treelzebub.podcasts.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.SnapSpec
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch
import net.treelzebub.podcasts.R
import net.treelzebub.podcasts.ui.screens.DiscoverScreen
import net.treelzebub.podcasts.ui.screens.ProfileScreen
import net.treelzebub.podcasts.ui.screens.SettingsScreen
import net.treelzebub.podcasts.ui.screens.SubscriptionsScreen

sealed class TabItem(
    @StringRes val text: Int,
    @DrawableRes val image: Int,
    val screen: @Composable () -> Unit
) {
    data object Subscriptions : TabItem(R.string.tab_subscriptions, R.drawable.subscriptions, { SubscriptionsScreen() })
    data object Discover : TabItem(R.string.tab_discover, R.drawable.search, { DiscoverScreen() })
    data object Profile : TabItem(R.string.tab_profile, R.drawable.account_circle, { ProfileScreen() })
    data object Settings : TabItem(R.string.tab_settings, R.drawable.settings, { SettingsScreen() })
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun TabsBar(tabs: List<TabItem>, pagerState: PagerState) {
    val scope = rememberCoroutineScope()
    val tabStyle = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight(700),
        color = Color.Black,
        textAlign = TextAlign.Center
    )

    TabRow(
        modifier = Modifier.fillMaxWidth(),
        selectedTabIndex = pagerState.currentPage,
        indicator = { tabPositions ->
            Box(
                Modifier
                    .tabIndicatorOffset(tabPositions[pagerState.currentPage])
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
                selected = pagerState.currentPage == i,
                onClick = {
                    scope.launch { pagerState.animateScrollToPage(i, 0f, SnapSpec(24)) }
                }
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
}