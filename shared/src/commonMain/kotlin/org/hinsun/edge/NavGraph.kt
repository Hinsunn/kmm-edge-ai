package org.hinsun.edge

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
import org.hinsun.edge.screens.llm_engines.LLMEnginesView
import org.hinsun.edge.screens.messages.MessagesView
import org.hinsun.edge.screens.sessions.SessionsView
import org.hinsun.edge.screens.speech_engines.SpeechEnginesView
import org.hinsun.edge.ui.NotFoundView

//
// File        : NavGraph.kt
// Author      : Tran Van Hoai
// Date        : 10/10/26
//

@Composable
fun NavGraph(
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
                is NavRoute.Messages -> NavEntry(key) { MessagesView() }
                is NavRoute.Sessions -> NavEntry(key) { SessionsView() }
                is NavRoute.LLMEngines -> NavEntry(key) { LLMEnginesView() }
                is NavRoute.SpeechEngines -> NavEntry(key) { SpeechEnginesView() }
                else -> NavEntry(key) { NotFoundView() }
            }
        }
    )
}