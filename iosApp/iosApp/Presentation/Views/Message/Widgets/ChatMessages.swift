//
// File        : ChatMessages.swift
// Author      : Tran Van Hoai
// Date        : 17/10/26
//

import Foundation
import SwiftData
import SwiftUI

struct ChatMessages: View {
    var body: some View {
        ScrollView {
            LazyVStack(alignment: .leading, spacing: 12) {}
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding()
        }
        .scrollDismissesKeyboard(.interactively)
    }
}
