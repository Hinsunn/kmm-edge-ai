//
// File        : Cryptography.swift
// Author      : Tran Van Hoai
// Date        : 11/10/26
//

import Foundation

protocol Cryptography {
    func encrypt(data: Data) -> Data
    func decrypt(data: Data) -> Data
}
