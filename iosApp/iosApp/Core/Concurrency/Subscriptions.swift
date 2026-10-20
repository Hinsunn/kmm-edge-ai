//
// File        : Subscriptions.swift
// Author      : Tran Van Hoai
// Date        : 20/10/26
//

import Foundation

final class Subscriptions {
    private var tasks: [Task<Void, Never>] = []

    func add(_ task: Task<Void, Never>) {
        tasks.append(task)
    }

    func cancelAll() {
        tasks.forEach { $0.cancel() }
        tasks.removeAll()
    }
}

extension Task where Success == Void, Failure == Never {
    func store(in subscriptions: Subscriptions) {
        subscriptions.add(self)
    }
}
