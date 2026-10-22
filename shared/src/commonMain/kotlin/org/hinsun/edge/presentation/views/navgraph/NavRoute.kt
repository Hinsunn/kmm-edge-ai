package org.hinsun.edge.presentation.views.navgraph

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

//
// File        : NavRoute.kt
// Author      : Tran Van Hoai
// Date        : 10/10/26
//

@Serializable
sealed interface NavRoute : NavKey {

    @Serializable
    data object Message : NavRoute, NavKey

    @Serializable
    data object Swipe : NavRoute, NavKey

}


val config = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(NavRoute.Message::class, NavRoute.Message.serializer())
            subclass(NavRoute.Swipe::class, NavRoute.Swipe.serializer())
        }
    }
}