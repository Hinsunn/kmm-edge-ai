package org.hinsun.edge.presentation.views.conversation

import androidx.lifecycle.ViewModel
import org.koin.core.annotation.KoinViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import org.hinsun.edge.database.EdgeAIDatabase
import org.hinsun.edge.database.entites.Conversation

//
// File        : ConversationViewModel.kt
// Author      : Tran Van Hoai
// Date        : 10/22/26
//

@KoinViewModel
class ConversationViewModel(database: EdgeAIDatabase) : ViewModel() {
    private val databaseDap = database.dao()
    val conversations: StateFlow<List<Conversation>> = databaseDap
        .conversations()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
}