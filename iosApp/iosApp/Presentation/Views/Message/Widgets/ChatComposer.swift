//
// File        : ChatComposer.swift
// Author      : Tran Van Hoai
// Date        : 17/10/26
//

import Foundation
import SwiftUI

struct ChatComposer: View {
    let focused: FocusState<Bool>.Binding
    let text: Binding<String>

    var body: some View {
        VStack(spacing: 12) {
            TextField(
                "Enter your message",
                text: text,
                axis: .vertical
            )
            .lineLimit(1 ... 5)
            .focused(focused)
            .frame(maxWidth: .infinity, alignment: .leading)

            HStack(alignment: .center, spacing: 16) {
                Button {} label: {
                    HStack(spacing: 8) {
                        Image(systemName: "pentagon.lefthalf.filled")

                        Text("Standard")
                            .font(.system(size: 14))
                    }
                    .padding(.horizontal, 12)
                    .padding(.vertical, 7)
                    .overlay {
                        RoundedRectangle(cornerRadius: 6)
                            .strokeBorder(
                                .gray.opacity(0.3),
                                lineWidth: 1
                            )
                    }
                }
                .buttonStyle(.plain)

                Spacer(minLength: 0)

                Button {
                    print("Start Listen")
                } label: {
                    Image(systemName: "microphone.fill")
                        .frame(width: 24, height: 24)
                        .contentShape(Rectangle())
                }
                .buttonStyle(.plain)
                .accessibilityLabel("Start listening")

                Button {
                    print("Send")
                } label: {
                    Image(systemName: "paperplane.fill")
                        .frame(width: 24, height: 24)
                        .contentShape(Rectangle())
                }
                .buttonStyle(.plain)
                .disabled(text.wrappedValue.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
                .accessibilityLabel("Send message")
            }
            .font(.system(size: 18))
            .foregroundStyle(.secondary)
        }
        .padding()
        .background(Color(uiColor: .systemBackground))
        .overlay {
            RoundedRectangle(cornerRadius: 8)
                .strokeBorder(
                    .gray.opacity(0.3),
                    lineWidth: 1
                )
        }
        .padding(.horizontal)
        .padding(.vertical, 8)
        .background(Color(uiColor: .systemBackground))
    }
}
