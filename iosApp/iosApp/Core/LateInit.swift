//
// File        : LateInit.swift
// Author      : Tran Van Hoai
// Date        : 11/10/26
//

import Foundation

@propertyWrapper
struct LateInit<Value> {
    private var storage: Value?

    var wrappedValue: Value {
        get {
            guard let value = storage else {
                fatalError("Property accessed before initialization")
            }

            return value
        }
        set {
            storage = newValue
        }
    }

    init() {
        self.storage = nil
    }
}
