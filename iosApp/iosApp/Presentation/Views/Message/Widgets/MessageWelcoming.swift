//
// File        : WelcomeView.swift
// Author      : Tran Van Hoai
// Date        : 17/10/26
//

import Foundation
import SwiftUI

struct MessageWelcoming: View {
    let suggestions: [String]
    let isRefreshing: Bool
    let onTapSuggestion: (String) -> Void
    let onRefresh: () -> Void

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                Spacer().frame(height: 60)
                Text("Welcome to KMM Edge AI 🐳")
                    .font(.title3)
                    .frame(maxWidth: .infinity, alignment: .leading)

                Spacer().frame(height: 12)

                ForEach(suggestions, id: \.self) { suggestion in
                    Button { onTapSuggestion(suggestion) } label: {
                        HStack(alignment: .top, spacing: 12) {
                            Image(systemName: "arrow.up.arrow.down")
                                .font(.system(size: 14))
                                .padding(.top, 4)

                            Text(suggestion)
                                .font(.body)
                                .multilineTextAlignment(.leading)

                            Spacer(minLength: 0)
                        }
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .contentShape(Rectangle())
                    }
                    .buttonStyle(.plain)
                }

                Button { self.onRefresh() } label: {
                    HStack(spacing: 12) {
                        Image(systemName: "arrow.trianglehead.2.clockwise.rotate.90")
                            .rotationEffect(.degrees(self.isRefreshing ? 360 : 0))
                            .animation(
                                self.isRefreshing
                                    ? .linear(duration: 0.7).repeatForever(autoreverses: false)
                                    : .default,
                                value: self.isRefreshing
                            )

                        Text("Refresh")
                            .font(.body)
                    }
                }
                .buttonStyle(.plain)
                .foregroundStyle(.secondary)
                .padding(.top, 8)
                .disabled(self.isRefreshing)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.horizontal, 20)
        }
        .scrollIndicators(.hidden)
        .scrollDismissesKeyboard(.interactively)
    }
}
