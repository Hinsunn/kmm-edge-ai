package org.hinsun.edge

import android.app.Application
import org.hinsun.edge.di.initializeKoinDI
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.KoinConfiguration

//
// File        : KMMEdgeAIApp.kt
// Author      : Tran Van Hoai
// Date        : 10/22/26
//

class KMMEdgeAIApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeKoinDI(configuration = KoinConfiguration {
            androidContext(this@KMMEdgeAIApp)
        })
    }
}