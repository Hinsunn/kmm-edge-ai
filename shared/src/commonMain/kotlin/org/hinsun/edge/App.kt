package org.hinsun.edge

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.rememberNavBackStack
import org.hinsun.edge.theme.KMMEdgeAITheme

@Composable
@Preview
fun App() {
    val navBackStack = rememberNavBackStack(
        configuration = config,
        NavRoute.Messages,
    )

    KMMEdgeAITheme {
        NavGraph(
            modifier = Modifier,
            navBackStack = navBackStack,
        )
    }
}