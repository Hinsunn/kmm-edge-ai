//
// File        : LiteRTEngine.swift
// Author      : Tran Van Hoai
// Date        : 11/10/26
//

import Foundation
import LiteRTLM

class LiteRTEngine {
    @LateInit var engine: Engine

    func initialize(modelPath: String) async throws {
        let engineConfig = try EngineConfig(
            modelPath: modelPath,
            backend: .gpu,
            cacheDir: NSTemporaryDirectory(),
        )

        self.engine = Engine(engineConfig: engineConfig)
        try await engine.initialize()
    }

    func sendMessage(message: String) async throws -> String {
        let conservation = try await engine.createConversation()
        let response = try await conservation.sendMessage(Message(message))
        return response.toString
    }

}
