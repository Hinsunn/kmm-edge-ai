package org.hinsun.edge.database.entites

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.uuid.Uuid

enum class LiteRTLMDownloadStatus {
    IDLE,
    ENQUEUED,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    FAILED
}

@Entity(tableName = "litert_lm")
data class LiteRTLMEntity(
    @PrimaryKey val id: String = Uuid.random().toString(),
    var name: String,
    var summary: String,
    @ColumnInfo(name = "model_path") var modelPath: String,

    @ColumnInfo(name = "download_url") var downloadURL: String,
    @ColumnInfo(name = "download_status") var downloadStatus: LiteRTLMDownloadStatus = LiteRTLMDownloadStatus.IDLE,
    @ColumnInfo(name = "download_progress") var downloadProgress: Double = 0.0,
    @ColumnInfo(name = "downloaded_bytes") var downloadedBytes: Long = 0,
    @ColumnInfo(name = "total_bytes") var totalBytes: Long = 0,

    @ColumnInfo(name = "error_message") var errorMessage: String? = null,
    @ColumnInfo(name = "created_at") var createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") var updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        // Same catalog as iosApp LiteRTLMEntity.availableModels
        val availableModels = listOf(
            LiteRTLMEntity(
                name = "Gemma 4 E2B IT",
                summary = "A compact instruction-tuned model, built for clear and well-formatted answers. It is strong at summarizing, rewriting and answering everyday questions while staying small enough for fast replies. A balanced choice between quality and speed.",
                modelPath = "gemma-4-E2B-it.litertlm",
                downloadURL = "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it.litertlm"
            ),
            LiteRTLMEntity(
                name = "Qwen3 0.6B",
                summary = "The smallest and fastest model here, so it answers almost instantly and runs well even on older phones. It supports both a quick-reply mode and a step-by-step thinking mode, and handles many languages. A good default for everyday chat and short tasks.",
                modelPath = "Qwen3-0.6B_dynamic_wi4b32_afp32.litertlm",
                downloadURL = "https://huggingface.co/litert-community/Qwen3-0.6B/resolve/main/Qwen3-0.6B_dynamic_wi4b32_afp32.litertlm"
            ),
            LiteRTLMEntity(
                name = "Gemma 3 1B IT",
                summary = "Google's compact instruction-tuned model, built for clear and well-formatted answers. It is strong at summarizing, rewriting and answering everyday questions while staying small enough for fast replies. A balanced choice between quality and speed.",
                modelPath = "Gemma3-1B-IT_multi-prefill-seq_q4_ekv4096.litertlm",
                downloadURL = "https://huggingface.co/litert-community/Gemma3-1B-IT/resolve/main/Gemma3-1B-IT_multi-prefill-seq_q4_ekv4096.litertlm"
            ),
            LiteRTLMEntity(
                name = "Qwen2.5 1.5B Instruct",
                summary = "The largest text model in this list, stored in 8-bit for higher answer quality. It is better at reasoning, math, coding and following structured formats like JSON. Pick it when accuracy matters more than speed or storage.",
                modelPath = "Qwen2.5-1.5B-Instruct_multi-prefill-seq_q8_ekv4096.litertlm",
                downloadURL = "https://huggingface.co/litert-community/Qwen2.5-1.5B-Instruct/resolve/main/Qwen2.5-1.5B-Instruct_multi-prefill-seq_q8_ekv4096.litertlm"
            ),
            LiteRTLMEntity(
                name = "Zamba2 1.2B Instruct",
                summary = "Uses a hybrid Mamba and transformer design, which keeps memory use low and speed steady as conversations get longer. It gives solid chat quality for its size. A good fit for long sessions on devices with limited memory.",
                modelPath = "Zamba2-1.2B-instruct_int8.litertlm",
                downloadURL = "https://huggingface.co/litert-community/Zamba2-1.2B-instruct/resolve/main/Zamba2-1.2B-instruct_int8.litertlm"
            ),
            LiteRTLMEntity(
                name = "InternVL3.5 1B",
                summary = "A vision-language model: it understands images as well as text. It can describe photos, read text in screenshots and answer questions about charts or documents. Choose it when you want to ask about pictures, not just type.",
                modelPath = "InternVL3_5-1B.litertlm",
                downloadURL = "https://huggingface.co/litert-community/InternVL3_5-1B/resolve/main/model.litertlm"
            ),
        )
    }
}
