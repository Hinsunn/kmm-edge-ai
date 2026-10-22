package org.hinsun.edge.di

import org.koin.core.logger.Level
import org.koin.dsl.KoinConfiguration
import org.koin.dsl.includes
import org.koin.plugin.module.dsl.startKoin

//
// File        : KoinInit.kt
// Author      : Tran Van Hoai
// Date        : 10/22/26
//

fun initializeKoinDI(configuration: KoinConfiguration? = null) {
    // Modules are discovered from @Configuration classes by the Koin compiler plugin
    startKoin<EdgeAIKoinApp> {
        includes(configuration)
        printLogger(Level.DEBUG)
    }
}
