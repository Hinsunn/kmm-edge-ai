package org.hinsun.edge

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
    data object Messages : NavRoute, NavKey

    @Serializable
    data object Sessions : NavRoute, NavKey

    @Serializable
    data object LLMEngines : NavRoute, NavKey

    @Serializable
    data object SpeechEngines : NavRoute, NavKey

}


val config = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(NavRoute.Messages::class, NavRoute.Messages.serializer())
            subclass(NavRoute.Sessions::class, NavRoute.Sessions.serializer())
            subclass(NavRoute.LLMEngines::class, NavRoute.LLMEngines.serializer())
            subclass(NavRoute.SpeechEngines::class, NavRoute.SpeechEngines.serializer())
        }
    }
}