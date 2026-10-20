//
// File        : GlobalEventDriven.swift
// Author      : Tran Van Hoai
// Date        : 20/10/26
//

import Foundation
import SwiftData
import SwiftUI

enum AuthGlobalEvent: BusEvent {
    case requiredAuthentication
    case sessionExpired
}

enum DatabaseGlobalEvent: BusEvent {
    case sessionCleanup
    case messageCleanup

    /// The number of times the event should be replayed to new subscribers.
    static var replay: Int {
        64
    }
}

@MainActor
@Observable
final class GlobalEventDriven {
    @ObservationIgnored private let modelContext: ModelContext
    @ObservationIgnored private let subscriptions = Subscriptions()

    init(modelContext: ModelContext, bus: EventBus = .shared) {
        self.modelContext = modelContext

        bus.subscribeOnMain(AuthGlobalEvent.self) { [weak self] in self?.handle($0) }
        bus.subscribeOnMain(DatabaseGlobalEvent.self) { [weak self] in self?.handle($0) }
    }

    private func handle(_ event: AuthGlobalEvent) {
        switch event {
        case .requiredAuthentication:
            print("Required authentication")
        case .sessionExpired:
            print("Session expired")
        }
    }

    private func handle(_ event: DatabaseGlobalEvent) {
        switch event {
        case .sessionCleanup:
            print("Session cleanup")
        case .messageCleanup:
            print("Message cleanup")
        }
    }
}
