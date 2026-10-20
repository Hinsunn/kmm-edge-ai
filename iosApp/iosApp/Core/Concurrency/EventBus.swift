//
// File        : EventBus.swift
// Author      : Tran Van Hoai
// Date        : 20/10/26
//

import Foundation

protocol BusEvent: Sendable {
    static var replay: Int { get }
    static var bufferSize: Int { get }
}

extension BusEvent {
    static var replay: Int {
        0
    }

    static var bufferSize: Int {
        64
    }
}

final class EventBus: @unchecked Sendable {
    static let shared = EventBus()
    init() {}

    private let lock = NSLock()
    private var flows: [ObjectIdentifier: Any] = [:]

    private func flow<E: BusEvent>(of type: E.Type) -> SharedFlow<E> {
        lock.withLock {
            let key = ObjectIdentifier(type)
            if let existing = flows[key] as? SharedFlow<E> {
                return existing
            }

            let created = SharedFlow<E>(
                replay: E.replay,
                bufferingPolicy: .bufferingNewest(E.bufferSize)
            )

            flows[key] = created
            return created
        }
    }

    func emit<E: BusEvent>(_ event: E) {
        flow(of: E.self).emit(event)
    }

    func events<E: BusEvent>(of type: E.Type) -> AsyncStream<E> {
        flow(of: type).subscribe()
    }

    @discardableResult
    func subscribe<E: BusEvent>(
        _ type: E.Type,
        _ handler: @escaping @Sendable (E) async -> Void
    ) -> Task<Void, Never> {
        let stream = events(of: type)
        return Task {
            for await event in stream {
                await handler(event)
            }
        }
    }

    @discardableResult
    func subscribeOnMain<E: BusEvent>(
        _ type: E.Type,
        _ handler: @escaping @MainActor (E) -> Void
    ) -> Task<Void, Never> {
        let stream = events(of: type)
        return Task { @MainActor in
            for await event in stream {
                handler(event)
            }
        }
    }

    static func emit<E: BusEvent>(_ event: E) {
        shared.emit(event)
    }

    @discardableResult
    static func listen<E: BusEvent>(
        _ type: E.Type,
        _ handler: @escaping @Sendable (E) async -> Void
    ) -> Task<Void, Never> {
        shared.subscribe(type, handler)
    }

    @discardableResult
    static func listenOnMain<E: BusEvent>(
        _ type: E.Type,
        _ handler: @escaping @MainActor (E) -> Void
    ) -> Task<Void, Never> {
        shared.subscribeOnMain(type, handler)
    }
}
