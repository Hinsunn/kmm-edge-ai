//
// File        : MainViewModel.swift
// Author      : Tran Van Hoai
// Date        : 18/10/26
//

import Foundation
import SwiftData
import SwiftUI

@Observable final class MainViewModel {
    private var activeSessionId: UUID?
    private var isLoadingSession: Bool = false

    func setActiveSessionId(_ activeSessionId: UUID?) {
        self.activeSessionId = activeSessionId
        print("Set Active Session ID: \(activeSessionId?.uuidString ?? "")")
    }

    func getActiveSessionId() -> UUID? {
        return self.activeSessionId
    }
}
