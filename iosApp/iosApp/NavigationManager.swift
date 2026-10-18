//
// File        : NavGraph.swift
// Author      : Tran Van Hoai
// Date        : 18/10/26
//

import Foundation
import SwiftUI

enum NavRoute: Hashable {
    case bottom
    case message(sessionId: UUID?)
}

@Observable final class NavigationManager {
    var path: [NavRoute] = .init()

    func navigate(to route: NavRoute) {
        path.append(route)
    }

    func pop() {
        guard !path.isEmpty else { return }
        path.removeLast()
    }

    func popToRoot() {
        path.removeAll()
    }
}
