package org.hinsun.edge

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.rememberNavBackStack
import org.hinsun.edge.presentation.views.navgraph.NavGraph
import org.hinsun.edge.presentation.views.navgraph.NavRoute
import org.hinsun.edge.presentation.views.navgraph.config
import org.hinsun.edge.theme.KMMEdgeAITheme
import org.kmm.cpp.CppCore
import org.koin.compose.KoinApplication
import org.koin.plugin.module.dsl.koinConfiguration

@Composable
fun App() {
    val navBackStack = rememberNavBackStack(
        configuration = config,
        NavRoute.Swipe,
    )

    val cppNativeEngine = remember { CppCore() }
    LaunchedEffect(Unit) {
        cppNativeEngine.runNativeEngine()
    }

    KMMEdgeAITheme {
        NavGraph(
            modifier = Modifier,
            navBackStack = navBackStack,
        )
    }
}