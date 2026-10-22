package org.hinsun.edge.presentation.views.swipe

import androidx.compose.animation.fadeIn
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import org.hinsun.edge.presentation.ui.NotFoundView
import org.hinsun.edge.presentation.views.configuration.ConfigurationView
import org.hinsun.edge.presentation.views.conversation.ConversationView
import org.hinsun.edge.presentation.views.download.DownloadView

//
// File        : SwipeView.kt
// Author      : Tran Van Hoai
// Date        : 10/22/26
//

@Composable
fun SwipeView() {
    val navBackStack = rememberNavBackStack(
        configuration = SwipeRoute.configuration,
        SwipeRoute.Conversation
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                SwipeRoute.routes.forEach { key ->
                    NavigationBarItem(
                        selected = navBackStack.lastOrNull() == key,
                        onClick = {
                            if (navBackStack.lastOrNull() != key) {
                                navBackStack.clear()
                                navBackStack.add(key)
                            }
                        },
                        icon = { Icon(key.icon, contentDescription = key.label) },
                        label = { Text(key.label) }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavDisplay(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            backStack = navBackStack,
            onBack = { navBackStack.removeLastOrNull() },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
            popTransitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
            predictivePopTransitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
            entryProvider = { key ->
                when (key) {
                    is SwipeRoute.Conversation -> NavEntry(key) { ConversationView() }
                    is SwipeRoute.Download -> NavEntry(key) { DownloadView() }
                    is SwipeRoute.Configuration -> NavEntry(key) { ConfigurationView() }
                    else -> NavEntry(key) { NotFoundView() }
                }
            }
        )
    }
}