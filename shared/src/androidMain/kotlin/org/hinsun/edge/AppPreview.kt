package org.hinsun.edge

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import org.hinsun.edge.di.EdgeAIKoinApp
import org.koin.android.ext.koin.androidContext
import org.koin.compose.KoinApplicationPreview
import org.koin.plugin.module.dsl.withConfiguration

//
// File        : AppPreview.kt
// Author      : Tran Van Hoai
// Date        : 10/22/26
//

@Preview
@Composable
private fun AppPreview() {
    val context = LocalContext.current.applicationContext
    KoinApplicationPreview(application = {
        androidContext(context)
        withConfiguration<EdgeAIKoinApp>()
    }) {
        App()
    }
}
