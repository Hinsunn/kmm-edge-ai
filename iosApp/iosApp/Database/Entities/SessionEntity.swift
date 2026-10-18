//
// File        : SessionEntity.swift
// Author      : Tran Van Hoai
// Date        : 17/10/26
//

import Foundation
import SwiftData

@Model class SessionEntity {
    @Attribute(.unique) var id: UUID
    var title: String

    @Relationship(deleteRule: .cascade, inverse: \MessageEntity.session)
    var messages: [MessageEntity]

    var createdAt: Int64
    var updatedAt: Int64

    init(title: String) {
        self.id = .init()
        self.title = title

        self.messages = []

        self.createdAt = Self.now
        self.updatedAt = Self.now
    }

    static var now: Int64 {
        Int64(Date().timeIntervalSince1970 * 1000)
    }

    func addMessage(
        _ message: String,
        messageRole: MessageRole = .human,
        in context: ModelContext
    ) {
        let message = MessageEntity(
            message: message,
            messageRole: messageRole,
            session: self
        )

        context.insert(message)
        self.updatedAt = SessionEntity.now
    }
}
