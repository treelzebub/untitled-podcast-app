package net.treelzebub.podcasts.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.plusAssign
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine
import com.ramcosta.composedestinations.spec.Route
import net.treelzebub.podcasts.ui.components.BottomBar
import net.treelzebub.podcasts.ui.components.TabItem
import net.treelzebub.podcasts.ui.screens.NavGraphs
import net.treelzebub.podcasts.ui.screens.appCurrentDestinationAsState
import net.treelzebub.podcasts.ui.screens.destinations.Destination
import net.treelzebub.podcasts.ui.screens.destinations.SubscriptionsScreenDestination
import net.treelzebub.podcasts.ui.screens.startAppDestination

@Composable
@OptIn(ExperimentalMaterialNavigationApi::class, ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
fun PodcastsApp() {
    val engine = rememberAnimatedNavHostEngine()
    val navController = engine.rememberNavController()
    val startRoute = SubscriptionsScreenDestination
    val tabs = listOf(
        TabItem.Subscriptions,
        TabItem.Discover,
        TabItem.Profile,
        TabItem.Settings
    )
//    val pagerState = rememberPagerState(pageCount = { tabs.size })

    AppScaffold(
        startRoute = startRoute,
        navController = navController,
        topBar = { i, j -> PodcastsAppBar() },
        bottomBar = { BottomBar(navController, tabs) }
    ) {
        DestinationsNavHost(
            engine = engine,
            navController = navController,
            navGraph = NavGraphs.root,
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            startRoute = startRoute
        )
    }
}

@OptIn(ExperimentalMaterialNavigationApi::class)
@Composable
fun AppScaffold(
    startRoute: Route,
    navController: NavHostController,
    topBar: @Composable (Destination, NavBackStackEntry?) -> Unit,
    bottomBar: @Composable (Destination) -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    val destination = navController.appCurrentDestinationAsState().value
        ?: startRoute.startAppDestination
    val navBackStackEntry = navController.currentBackStackEntry

    val bottomSheetNavigator = rememberBottomSheetNavigator()
    navController.navigatorProvider += bottomSheetNavigator

    // 👇 ModalBottomSheetLayout is only needed if some destination is bottom sheet styled
    Scaffold(
        topBar = { topBar(destination, navBackStackEntry) },
        bottomBar = { bottomBar(destination) },
        content = content
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PodcastsAppBar() {
    Surface(shadowElevation = 4.dp) {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFbe3de8), titleContentColor = Color.White),
            title = { Text("Podcasts App") }
        )
    }
}
