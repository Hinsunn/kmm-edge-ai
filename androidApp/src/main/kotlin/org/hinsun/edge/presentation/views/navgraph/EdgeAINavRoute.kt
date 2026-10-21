package org.hinsun.edge.presentation.views.navgraph

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.hinsun.edge.NavRoute

@Serializable
sealed interface EdgeAINavRoute : NavKey {

    @Serializable
    data object Bottom : EdgeAINavRoute, NavKey

    @Serializable
    data object Message : EdgeAINavRoute, NavKey

    @Serializable
    data object KMMApp : EdgeAINavRoute, NavKey

}

val edgeAiConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(EdgeAINavRoute.Bottom::class, EdgeAINavRoute.Bottom.serializer())
            subclass(EdgeAINavRoute.Message::class, EdgeAINavRoute.Message.serializer())
            subclass(EdgeAINavRoute.KMMApp::class, EdgeAINavRoute.KMMApp.serializer())
        }
    }
}