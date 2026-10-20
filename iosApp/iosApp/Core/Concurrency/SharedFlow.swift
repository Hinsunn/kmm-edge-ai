//
// File        : SharedFlow.swift
// Author      : Tran Van Hoai
// Date        : 20/10/26
//

import Foundation

final class SharedFlow<T: Sendable>: @unchecked Sendable {
    private let lock = NSLock()
    private var subscribers: [UUID: AsyncStream<T>.Continuation] = [:]
    private var cache: [T] = []

    private let replay: Int
    private let bufferingPolicy: AsyncStream<T>.Continuation.BufferingPolicy

    init(
        replay: Int = 0,
        bufferingPolicy: AsyncStream<T>.Continuation.BufferingPolicy = .unbounded
    ) {
        self.replay = max(0, replay)
        self.bufferingPolicy = bufferingPolicy
    }

    func subscribe() -> AsyncStream<T> {
        let id = UUID()
        let (stream, continuation) = AsyncStream.makeStream(
            of: T.self,
            bufferingPolicy: bufferingPolicy
        )

        lock.withLock {
            for value in cache {
                continuation.yield(value)
            }
            subscribers[id] = continuation
        }

        continuation.onTermination = { [weak self] _ in
            self?.remove(id)
        }

        return stream
    }

    func emit(_ value: T) {
        lock.withLock {
            if replay > 0 {
                cache.append(value)
                if cache.count > replay {
                    cache.removeFirst(cache.count - replay)
                }
            }

            for continuation in subscribers.values {
                continuation.yield(value)
            }
        }
    }

    func finish() {
        let all = lock.withLock { () -> [AsyncStream<T>.Continuation] in
            let values = Array(subscribers.values)
            subscribers.removeAll()
            return values
        }

        all.forEach { $0.finish() }
    }

    private func remove(_ id: UUID) {
        lock.withLock { subscribers[id] = nil }
    }
}
