package net.treelzebub.podcasts.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.treelzebub.podcasts.ui.components.TabItem
import net.treelzebub.podcasts.ui.components.TabsBar

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun MainScreen() {
    val tabs = listOf(
        TabItem.Subscriptions,
        TabItem.Discover,
        TabItem.Profile,
        TabItem.Settings
    )
    val pagerState = rememberPagerState(pageCount = { tabs.size })

    Scaffold(
        Modifier.fillMaxSize(),
        bottomBar = { TabsBar(tabs, pagerState) }
    ) { contentPadding ->
        TabContent(tabs, pagerState, contentPadding)
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun TabContent(tabs: List<TabItem>, pagerState: PagerState, contentPadding: PaddingValues) {
    HorizontalPager(contentPadding = contentPadding, state = pagerState) { page ->
        tabs[page].screen()
    }
}