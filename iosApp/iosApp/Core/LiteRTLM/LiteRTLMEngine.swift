//
// File        : LiteRTLMEngine.swift
// Author      : Tran Van Hoai
// Date        : 18/10/26
//

import Foundation
import LiteRTLM
import SwiftUI

@MainActor
@Observable
final class LiteRTLMEngine {
    let modelsDirectory: URL

    private(set) var isInitialized = false
    private(set) var isInitializing = false
    private(set) var activeModel: LiteRTLMEntity?
    private(set) var errorMessage: String?

    var isReady: Bool {
        return isInitialized && !isInitializing && errorMessage == nil
    }

    @ObservationIgnored private var engine: Engine?
    @ObservationIgnored private var conversation: Conversation?

    init() {
        let support = FileManager.default.urls(for: .applicationSupportDirectory, in: .userDomainMask)[0]
        modelsDirectory = support.appendingPathComponent("Models", isDirectory: true)
    }

    func initializeEngine(_ model: LiteRTLMEntity) async {
        guard !isInitializing else {
            nsLog("Engine is already initializing")
            return
        }

        guard !(isInitialized && activeModel?.id == model.id) else {
            nsLog("Engine is already initialized with the same model")
            return
        }

        isInitializing = true
        errorMessage = nil

        defer {
            isInitializing = false
        }

        do {
            let modelPathURL = modelPath(for: model.modelPath)
            // let path = modelPathURL.path(percentEncoded: false)
            let path = modelPathURL.path(percentEncoded: false)
            guard FileManager.default.fileExists(atPath: path) else {
                throw NSError(
                    domain: "LiteRTLMEngine",
                    code: 404,
                    userInfo: [NSLocalizedDescriptionKey: "Model file not found at \(path)"]
                )
            }

            let engineConfig = try EngineConfig(
                modelPath: path,
                backend: .cpu(threadCount: 4),
                maxNumTokens: 1024,
                cacheDir: NSTemporaryDirectory()
            )

            let newEngine = Engine(engineConfig: engineConfig)
            try await newEngine.initialize()

            engine = newEngine
            activeModel = model
            isInitialized = true

            nsLog("Engine initialized successfully")
        } catch {
            errorMessage = error.localizedDescription
            nsLog("Failed to initialize engine with error: \(error)")
        }
    }

    func streamMessage(_ message: String) -> AsyncThrowingStream<String, Error> {
        AsyncThrowingStream { continuation in
            let task = Task { @MainActor in
                do {
                    guard isReady, let engine else {
                        throw NSError(
                            domain: "LiteRTLMEngine",
                            code: 400,
                            userInfo: [NSLocalizedDescriptionKey: "Engine is not ready"]
                        )
                    }

                    if conversation == nil {
                        conversation = try await engine.createConversation()
                    }

                    for try await chunk in conversation!.sendMessageStream(Message(message)) {
                        let response = chunk.toString
                        nsLog("Received chunk: \(response)")
                        continuation.yield(response)
                    }
                    continuation.finish()
                } catch {
                    continuation.finish(throwing: error)
                }
            }

            continuation.onTermination = { _ in task.cancel() }
        }
    }

    private func modelPath(for modelPath: String) -> URL {
        return modelsDirectory.appendingPathComponent(modelPath)
    }

    private func runGuard(_ task: @escaping () async throws -> Void) {
        Task {
            do {
                try await task()
            } catch {
                nsLog("Failed to run task with error: \(error)")
            }
        }
    }

    private func nsLog(_ message: String) {
        let now = Date().formatted(date: .omitted, time: .standard)
        NSLog("[LITERTLM_ENGINE|\(now)]: %@", message)
    }
}
