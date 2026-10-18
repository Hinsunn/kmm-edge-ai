//
// File        : MessagesSkeleton.swift
// Author      : Tran Van Hoai
// Date        : 18/10/26
//

import Foundation
import SwiftUI

struct MessagesSkeleton: View {
    var body: some View {
        ScrollView {
            LazyVStack(spacing: 12) {
                chatSkeleton(alignment: .trailing, height: 60)
                chatSkeleton(alignment: .leading, height: 120)
                chatSkeleton(alignment: .trailing, height: 60)
                chatSkeleton(alignment: .leading, height: 100)
                chatSkeleton(alignment: .trailing, height: 60)
            }
            .frame(maxWidth: .infinity)
        }
        .padding()
        .scrollIndicators(.hidden)
        .scrollDismissesKeyboard(.interactively)
    }

    func chatSkeleton(
        alignment: HorizontalAlignment,
        height: CGFloat
    ) -> some View {
        return HStack {
            if alignment == .trailing {
                Spacer(minLength: 0)
            }

            VStack(alignment: alignment, spacing: 6) {
                Rectangle()
                    .fill(Color.gray.opacity(0.3))
                    .frame(width: UIScreen.main.bounds.width * 0.7, height: height)
                    .skeleton(isLoading: true)

                Rectangle()
                    .fill(Color.gray.opacity(0.3))
                    .frame(width: 100, height: 20)
                    .skeleton(isLoading: true)
            }

            if alignment == .leading {
                Spacer(minLength: 0)
            }
        }
        .frame(maxWidth: .infinity)
    }
}
