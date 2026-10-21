package org.hinsun.edge.presentation.views.download

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import org.hinsun.edge.core.managers.LiteRTLMModelDownloadManager
import org.hinsun.edge.database.entites.LiteRTLMEntity

class DownloadViewModel : ViewModel() {
    val models: StateFlow<List<LiteRTLMEntity>> = LiteRTLMModelDownloadManager.models
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
