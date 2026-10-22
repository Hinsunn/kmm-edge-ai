package org.hinsun.edge.presentation.views.navgraph

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import org.hinsun.edge.App
import org.hinsun.edge.presentation.views.bottom.BottomView
import org.hinsun.edge.presentation.views.message.MessageView
import org.hinsun.edge.presentation.ui.NotFoundView

@Composable
fun EdgeAINavGraph(
    modifier: Modifier = Modifier,
    navBackStack: NavBackStack<NavKey>,
) {
    NavDisplay(
        modifier = modifier,
        backStack = navBackStack,
        onBack = { navBackStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        transitionSpec = {
            // Slide in from right when navigating forward
            slideInHorizontally(initialOffsetX = { it }) togetherWith
                    slideOutHorizontally(targetOffsetX = { -it })
        },
        popTransitionSpec = {
            // Slide in from left when navigating back
            slideInHorizontally(initialOffsetX = { -it }) togetherWith
                    slideOutHorizontally(targetOffsetX = { it })
        },
        predictivePopTransitionSpec = {
            // Slide in from left when navigating back
            slideInHorizontally(initialOffsetX = { -it }) togetherWith
                    slideOutHorizontally(targetOffsetX = { it })
        },
        entryProvider = { key ->
            when (key) {
                is EdgeAINavRoute.Bottom -> NavEntry(key) { BottomView() }
                is EdgeAINavRoute.Message -> NavEntry(key) { MessageView() }
                is EdgeAINavRoute.KMMApp -> NavEntry(key) { App() }
                else -> NavEntry(key) { NotFoundView() }
            }
        }
    )
}
