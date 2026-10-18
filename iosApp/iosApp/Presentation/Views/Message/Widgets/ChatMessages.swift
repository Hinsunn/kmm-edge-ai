//
// File        : ChatMessages.swift
// Author      : Tran Van Hoai
// Date        : 17/10/26
//

import Foundation
import SwiftData
import SwiftUI

struct ChatMessages: View {
    var messages: [MessageEntity]
    var isStreaming: Bool
    var streamingText: String

    var body: some View {
        ScrollView {
            LazyVStack(alignment: .leading, spacing: 12) {
                ForEach(messages) { message in
                    if message.messageRole == .human {
                        humanBubble(message)
                    } else {
                        aiBubble(message)
                    }
                }

                if isStreaming {
                    messageStreaming(streamingText)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding()
        }
        .scrollIndicators(.hidden)
        .scrollDismissesKeyboard(.interactively)
    }

    func humanBubble(_ message: MessageEntity) -> some View {
        VStack(alignment: .trailing, spacing: 6) {
            Text(.init(message.message))
                .padding(8)
                .background(
                    RoundedRectangle(cornerRadius: 8)
                        .fill(Color.accentColor.opacity(0.1))
                )
                .frame(maxWidth: UIScreen.main.bounds.width * 0.8, alignment: .trailing)

            Text(formatTimeFromEpochMilliseconds(message.createdAt))
                .font(.caption2)
                .foregroundStyle(.secondary)
        }
        .frame(maxWidth: .infinity, alignment: .trailing)
    }

    func messageStreaming(_ message: String) -> some View {
        HStack {
            VStack(alignment: .leading, spacing: 6) {
                Text(.init(message))
                    .padding(8)
                    .background(
                        RoundedRectangle(cornerRadius: 8)
                            .fill(Color.gray.opacity(0.1))
                    )
                    .frame(maxWidth: UIScreen.main.bounds.width * 0.8, alignment: .leading)
            }

            Spacer()
        }
    }

    func aiBubble(_ message: MessageEntity) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(.init(message.message))
                .padding(8)
                .background(
                    RoundedRectangle(cornerRadius: 8)
                        .fill(Color.gray.opacity(0.1))
                )
                .frame(maxWidth: UIScreen.main.bounds.width * 0.8, alignment: .leading)

            Text(formatTimeFromEpochMilliseconds(message.createdAt))
                .font(.caption2)
                .foregroundStyle(.secondary)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    func formatTimeFromEpochMilliseconds(_ epochMilliseconds: Int64) -> String {
        let date = Date(timeIntervalSince1970: TimeInterval(epochMilliseconds) / 1000)
        return date.formatted(date: .omitted, time: .complete)
    }
}
