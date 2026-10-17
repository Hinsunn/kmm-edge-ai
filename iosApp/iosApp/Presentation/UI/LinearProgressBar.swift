//
// File        : LinearProgressBar.swift
// Author      : Tran Van Hoai
// Date        : 17/10/26
//

import Foundation
import SwiftUI

struct LinearProgressBar: View {
    let progress: Double

    let height: CGFloat = 8.0
    let cornerRadius: CGFloat = 2.0
    let activeColor: Color = .accentColor
    let backgroundColor: Color = .init(.systemGray5)

    private var progressFraction: Double {
        // Normalize the progress value to be between 0.0 and 1.0
        max(0.0, min(1.0, progress))
    }

    var body: some View {
        GeometryReader { geometry in
            ZStack(alignment: .leading) {
                RoundedRectangle(cornerRadius: cornerRadius)
                    .fill(backgroundColor)
                    .frame(height: height)

                RoundedRectangle(cornerRadius: cornerRadius)
                    .fill(activeColor)
                    .frame(width: geometry.size.width * progressFraction, height: height)
                    .animation(.linear(duration: 0.2), value: progress)
            }
        }
        .frame(height: height)
    }
}
