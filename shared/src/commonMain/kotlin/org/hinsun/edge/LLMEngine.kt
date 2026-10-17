package org.hinsun.edge

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

interface LLMEngine {
    fun start(modelPath: String, onResult: (error: String?) -> Unit)
    fun sendStream(prompt: String, onChunk: (String) -> Unit, onDone: (error: String?) -> Unit)
}

object LLMProvider {
    lateinit var engine: LLMEngine
}

fun LLMEngine.chatFlow(prompt: String): Flow<String> = callbackFlow {
    sendStream(
        prompt,
        onChunk = { trySend(it) },
        onDone = { err -> if (err == null) close() else close(RuntimeException(err)) }
    )
    awaitClose {}
}