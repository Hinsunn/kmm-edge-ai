//
// File        : LiteRT.swift
// Author      : Tran Van Hoai
// Date        : 17/10/26
//

import Foundation
import SwiftData

enum DownloadStatus: String, Codable {
    case idle
    case enqueued
    case downloading
    case paused
    case completed
    case failed
}

@Model class LiteRTLMEntity {
    @Attribute(.unique) var id: UUID
    @Attribute(.unique) var name: String
    var summary: String
    var modelPath: String

    var downloadURL: String
    var downloadStatus: DownloadStatus
    var downloadProgress: Double
    var downloadedBytes: Int64
    var totalBytes: Int64

    @Attribute(.externalStorage) var resumeData: Data?
    var errorMessage: String?

    // Epoch in Milliseconds
    var createdAt: Int64
    var updatedAt: Int64

    init(name: String, summary: String, modelPath: String, downloadURL: String) {
        self.id = .init()
        self.name = name
        self.summary = summary
        self.modelPath = modelPath
        self.downloadURL = downloadURL
        self.downloadStatus = .idle
        self.downloadProgress = 0.0
        self.downloadedBytes = 0
        self.totalBytes = 0
        self.createdAt = Self.now
        self.updatedAt = Self.now
    }

    static var now: Int64 {
        Int64(Date().timeIntervalSince1970 * 1000)
    }
}

extension LiteRTLMEntity {
    static var availableModels: [LiteRTLMEntity] = [
        .init(
            name: "Qwen3 0.6B",
            summary: "The smallest and fastest model here, so it answers almost instantly and runs well even on older iPhones. It supports both a quick-reply mode and a step-by-step thinking mode, and handles many languages. A good default for everyday chat and short tasks.",
            modelPath: "Qwen3-0.6B_dynamic_wi4b32_afp32.litertlm",
            downloadURL: "https://huggingface.co/litert-community/Qwen3-0.6B/resolve/main/Qwen3-0.6B_dynamic_wi4b32_afp32.litertlm"
        ),
        .init(
            name: "Gemma 3 1B IT",
            summary: "Google's compact instruction-tuned model, built for clear and well-formatted answers. It is strong at summarizing, rewriting and answering everyday questions while staying small enough for fast replies. A balanced choice between quality and speed.",
            modelPath: "Gemma3-1B-IT_multi-prefill-seq_q4_ekv4096.litertlm",
            downloadURL: "https://huggingface.co/litert-community/Gemma3-1B-IT/resolve/main/Gemma3-1B-IT_multi-prefill-seq_q4_ekv4096.litertlm"
        ),
        .init(
            name: "Qwen2.5 1.5B Instruct",
            summary: "The largest text model in this list, stored in 8-bit for higher answer quality. It is better at reasoning, math, coding and following structured formats like JSON. Pick it when accuracy matters more than speed or storage.",
            modelPath: "Qwen2.5-1.5B-Instruct_multi-prefill-seq_q8_ekv4096.litertlm",
            downloadURL: "https://huggingface.co/litert-community/Qwen2.5-1.5B-Instruct/resolve/main/Qwen2.5-1.5B-Instruct_multi-prefill-seq_q8_ekv4096.litertlm"
        ),
        .init(
            name: "Zamba2 1.2B Instruct",
            summary: "Uses a hybrid Mamba and transformer design, which keeps memory use low and speed steady as conversations get longer. It gives solid chat quality for its size. A good fit for long sessions on devices with limited memory.",
            modelPath: "Zamba2-1.2B-instruct_int8.litertlm",
            downloadURL: "https://huggingface.co/litert-community/Zamba2-1.2B-instruct/resolve/main/Zamba2-1.2B-instruct_int8.litertlm"
        ),
        .init(
            name: "InternVL3.5 1B",
            summary: "A vision-language model: it understands images as well as text. It can describe photos, read text in screenshots and answer questions about charts or documents. Choose it when you want to ask about pictures, not just type.",
            modelPath: "InternVL3_5-1B.litertlm",
            downloadURL: "https://huggingface.co/litert-community/InternVL3_5-1B/resolve/main/model.litertlm"
        ),
    ]
}
