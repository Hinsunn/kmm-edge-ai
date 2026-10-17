package org.hinsun.edge.screens.messages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.rememberNavBackStack
import org.hinsun.edge.NavGraph
import org.hinsun.edge.NavRoute
import org.hinsun.edge.config
import org.hinsun.edge.screens.messages.ui.ChatComposer
import org.hinsun.edge.theme.KMMEdgeAITheme

//
// File        : MessagesView.kt
// Author      : Tran Van Hoai
// Date        : 10/10/26
//

@Composable
fun MessagesView() {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier.weight(1f),
            )

            ChatComposer()
        }
    }
}

@Preview
@Composable
fun MessagesViewPreview() {
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