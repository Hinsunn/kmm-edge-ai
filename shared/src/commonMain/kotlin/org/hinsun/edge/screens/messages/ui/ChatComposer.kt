package org.hinsun.edge.screens.messages.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.rememberNavBackStack
import org.hinsun.edge.NavGraph
import org.hinsun.edge.NavRoute
import org.hinsun.edge.config
import org.hinsun.edge.theme.KMMEdgeAITheme

//
// File        : ChatComposer.kt
// Author      : Tran Van Hoai
// Date        : 11/10/26
//

@Composable
fun ChatComposer() {
    var value by remember { mutableStateOf("Can you explain LiteRT and TFLite Engine and give me the comparision ?") }

    Box(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color.Gray,
                shape = RoundedCornerShape(4.dp),
            ),
    ) {
        TextField(
            value = value,
            onValueChange = { value = it },
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {}
    }
}

@Preview
@Composable
fun ChatComposerPreview() {
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


