package net.treelzebub.podcasts.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.navigation.popBackStack
import com.ramcosta.composedestinations.navigation.popUpTo
import com.ramcosta.composedestinations.utils.isRouteOnBackStack
import net.treelzebub.podcasts.R
import net.treelzebub.podcasts.ui.screens.NavGraphs
import net.treelzebub.podcasts.ui.screens.destinations.DirectionDestination
import net.treelzebub.podcasts.ui.screens.destinations.DiscoverScreenDestination
import net.treelzebub.podcasts.ui.screens.destinations.ProfileScreenDestination
import net.treelzebub.podcasts.ui.screens.destinations.SettingsScreenDestination
import net.treelzebub.podcasts.ui.screens.destinations.SubscriptionsScreenDestination

sealed class TabItem(
    @StringRes val text: Int,
    @DrawableRes val image: Int,
    val direction: DirectionDestination
) {
    data object Subscriptions : TabItem(R.string.tab_subscriptions, R.drawable.subscriptions, SubscriptionsScreenDestination)
    data object Discover : TabItem(R.string.tab_discover, R.drawable.search, DiscoverScreenDestination)
    data object Profile : TabItem(R.string.tab_profile, R.drawable.account_circle, ProfileScreenDestination)
    data object Settings : TabItem(R.string.tab_settings, R.drawable.settings, SettingsScreenDestination)
}

@Composable
fun BottomBar(navController: NavHostController, tabs: List<TabItem>) {
    NavigationBar {
        tabs.forEach { destination ->
            val isCurrentDestOnBackStack = navController.isRouteOnBackStack(destination.direction)
            NavigationBarItem(
                selected = isCurrentDestOnBackStack,
                onClick = {
                    if (isCurrentDestOnBackStack) {
                        // When we click again on a bottom bar item and it was already selected
                        // we want to pop the back stack until the initial destination of this bottom bar item
                        navController.popBackStack(destination.direction, false)
                        return@NavigationBarItem
                    }

                    navController.navigate(destination.direction) {
                        // Pop up to the root of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo(NavGraphs.root) {
                            saveState = true
                        }

                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        painterResource(destination.image),
                        contentDescription = stringResource(destination.text)
                    )
                },
                label = { Text(stringResource(destination.text)) },
            )
        }
    }
}
