package org.hinsun.edge.presentation.views.swipe

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.SpaceDashboard
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

//
// File        : SwipeRoute.kt
// Author      : Tran Van Hoai
// Date        : 10/22/26
//

@Serializable
sealed interface SwipeRoute : NavKey {
    val icon: ImageVector
    val label: String

    @Serializable
    data object Conversation : SwipeRoute, NavKey {
        override val icon: ImageVector = Icons.Filled.Assessment
        override val label: String = "Conversation"
    }

    @Serializable
    data object Download : SwipeRoute, NavKey {
        override val icon: ImageVector = Icons.Filled.Download
        override val label: String = "Download"
    }

    @Serializable
    data object Configuration : SwipeRoute, NavKey {
        override val icon: ImageVector = Icons.Filled.SpaceDashboard
        override val label: String = "Configuration"
    }

    companion object {
        val routes: List<SwipeRoute> = listOf(Conversation, Download, Configuration)
        val configuration = SavedStateConfiguration {
            serializersModule = SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(Conversation::class, Conversation.serializer())
                    subclass(Download::class, Download.serializer())
                    subclass(Configuration::class, Configuration.serializer())
                }
            }
        }
    }
}