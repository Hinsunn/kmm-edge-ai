package org.hinsun.edge

import android.util.Log

class AndroidLLMEngine : LLMEngine {
    override fun start(modelPath: String, onResult: (error: String?) -> Unit) {
        Log.d("AndroidLLMEngine", "Starting LLM engine with model path: $modelPath")
        onResult(null)
    }

    override fun sendStream(
        prompt: String,
        onChunk: (String) -> Unit,
        onDone: (error: String?) -> Unit
    ) {
        Log.d("AndroidLLMEngine", "Sending stream with prompt: $prompt")
        onChunk(prompt)
        onDone(null)
    }
}