//
// File        : AppDatabase.swift
// Author      : Tran Van Hoai
// Date        : 17/10/26
//

import Foundation
import SwiftData

class AppDatabase {
    static let container: ModelContainer = {
        do {
            return try ModelContainer(for: LiteRTLMEntity.self, MessageEntity.self, SessionEntity.self)
        } catch {
            fatalError("[APP_DATABASE] ModelContainer failed to initialize: \(error)")
        }
    }()
}
