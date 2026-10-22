//
// File        : ConversationView.kt
// Author      : Tran Van Hoai
// Date        : 10/22/26
//

package org.hinsun.edge.presentation.views.conversation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.hinsun.edge.database.entites.Conversation
import org.hinsun.edge.theme.KMMEdgeAITheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ConversationView(
    conversationViewModel: ConversationViewModel = koinViewModel<ConversationViewModel>()
) {
    val conversations by conversationViewModel.conversations.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Conversations") },
                actions = {
                    Icon(
                        imageVector = Icons.Filled.AddBox,
                        contentDescription = "Add",
                    )
                }
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            conversations.forEach { conversation ->
                Row {
                    Text(text = conversation.title)
                }
            }
        }
    }
}


@Preview
@Composable
private fun ConversationPreview() {}