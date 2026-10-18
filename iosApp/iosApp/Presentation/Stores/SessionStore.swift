//
// File        : SessionStore.swift
// Author      : Tran Van Hoai
// Date        : 18/10/26
//

import Foundation
import SwiftData
import SwiftUI

@MainActor
@Observable
final class SessionStore {
    @ObservationIgnored private let context: ModelContext
    @ObservationIgnored private let loadTask: Task<Void, Never>?

    init(context: ModelContext) {
        self.context = context
        self.loadTask = nil
    }

    var activeSessionId: UUID? {
        didSet { Task {} }
    }

    var messages: [MessageEntity] = []

    private func loadMessages() async {
        guard let id = activeSessionId else { messages = []; return }
    }
}
