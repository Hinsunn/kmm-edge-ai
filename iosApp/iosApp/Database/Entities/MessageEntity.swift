//
// File        : MessageEntity.swift
// Author      : Tran Van Hoai
// Date        : 17/10/26
//

import Foundation
import SwiftData

enum MessageRole: String, Codable {
    case system
    case human
    case ai
}

@Model class MessageEntity {
    @Attribute(.unique) var id: UUID
    var message: String
    var messageRole: MessageRole

    var session: SessionEntity?

    var usageCredits: Int
    var usageTokens: Int

    var createdAt: Int64
    var updatedAt: Int64

    init(message: String, messageRole: MessageRole) {
        self.id = .init()
        self.message = message
        self.messageRole = messageRole

        self.usageCredits = 0
        self.usageTokens = 0

        self.createdAt = Self.now
        self.updatedAt = Self.now
    }

    static var now: Int64 {
        Int64(Date().timeIntervalSince1970 * 1000)
    }
}
