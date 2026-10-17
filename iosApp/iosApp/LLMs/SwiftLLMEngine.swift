//
// File        : SwiftLLMEngine.swift
// Author      : Tran Van Hoai
// Date        : 10/10/26
//

import Foundation
import Shared
import os

final class SwiftLLMEngine: LLMEngine {

    func sendStream(prompt: String, onChunk: @escaping (String) -> Void, onDone: @escaping (String?) -> Void) {
        log(message: "Sending stream with prompt: \(prompt)")
        onChunk("This is a chunk of response.")
        onDone(nil)
    }

    func start(modelPath: String, onResult: @escaping (String?) -> Void) {
        log(message: "Starting LLM engine with model path: \(modelPath)")
        onResult(nil)
    }

    func log(message: String) {
        NSLog("[iOS LLMEngine] \(message)")
    }

}
