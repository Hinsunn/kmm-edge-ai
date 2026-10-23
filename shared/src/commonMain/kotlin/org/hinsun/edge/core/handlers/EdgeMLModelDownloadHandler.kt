package org.hinsun.edge.core.handlers

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.hinsun.edge.database.EdgeAIDatabase
import org.hinsun.edge.database.EdgeAIDatabaseDao
import org.hinsun.edge.database.entites.EdgeML
import org.koin.core.annotation.Singleton

//
// File        : EdgeMLModelDownloadHandler.kt
// Author      : Tran Van Hoai
// Date        : 10/22/26
//

@Singleton
class EdgeMLModelDownloadHandler(database: EdgeAIDatabase) {
    private val databaseDao: EdgeAIDatabaseDao = database.dao()
    private val scope = CoroutineScope(SupervisorJob())

    private val _edgeMLs = MutableStateFlow<List<EdgeML>>(emptyList())
    val edgeMLs: StateFlow<List<EdgeML>> = _edgeMLs
        .asStateFlow()
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        initialize()
    }

    fun initialize() = scope.launch {
        databaseDao.edgeMls().collect { edgeMLs ->
            _edgeMLs.value = edgeMLs
        }
    }
}