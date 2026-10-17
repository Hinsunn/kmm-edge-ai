//
// File        : MessageView.swift
// Author      : Tran Van Hoai
// Date        : 17/10/26
//

import Foundation
import SwiftData
import SwiftUI

let suggestionQuestions = [
    "How do AI models learn from data and make decisions like humans?",
    "What would it take for humans to build a sustainable city on Mars?",
    "How is artificial intelligence changing the way students learn and teachers teach?",
    "How can smartphones run powerful AI models without an internet connection?",
    "What are the biggest unanswered questions about the universe and how scientists investigate them?",
    "How could quantum computing transform medicine, science, and modern technology?",
    "Which skills should students develop today to prepare for jobs in the next ten years?",
    "How can emerging technologies help us reduce pollution and fight climate change?",
    "How do self-driving cars perceive their surroundings and make safe driving decisions?",
    "What emerging inventions could transform our homes and everyday lives in the next decade?",
]

struct MessageView: View {
    @Query(sort: \SessionEntity.createdAt) private var sessions: [SessionEntity]
    @Environment(MainViewModel.self) private var mainViewModel

    @State private var message: String = ""
    @FocusState private var isTextFieldFocused: Bool

    @State private var isRefreshing: Bool = false
    @State private var suggestions: [String] = Array(suggestionQuestions.shuffled().prefix(3))

    private var hasMessage: Bool = false

    var body: some View {
        NavigationStack {
            ZStack {
                Group {
                    if self.hasMessage {
                        ChatMessages()
                    } else {
                        MessageWelcoming(
                            suggestions: suggestions,
                            isRefreshing: isRefreshing,
                            onTapSuggestion: { suggestion in
                                self.message = suggestion
                                self.isTextFieldFocused = true
                            },
                            onRefresh: { self.refreshSuggestion() }
                        )
                    }
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            }
            .safeAreaInset(edge: .bottom, spacing: 0) {
                ChatComposer(
                    focused: $isTextFieldFocused,
                    text: $message
                )
            }
            .background(Color(uiColor: .systemBackground))
            .contentShape(Rectangle())
            .onTapGesture {
                self.isTextFieldFocused = false
            }
            .navigationTitle("Messages")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button(action: {
                        print("Icon action tapped!")
                    }) {
                        Image(systemName: "app.badge.fill")
                    }
                    .foregroundColor(.gray)
                }
            }
            .task {
                // Listen to the active session changes and update the message accordingly
            }
        }
    }

    @MainActor
    private func refreshSuggestion() {
        guard !isRefreshing else { return }

        isRefreshing = true
        Task {
            do {
                try await Task.sleep(for: .milliseconds(1000))
                let remainingQuestions = suggestionQuestions.filter {
                    !suggestions.contains($0)
                }

                let pool = remainingQuestions.isEmpty
                    ? suggestionQuestions
                    : remainingQuestions

                withAnimation(.easeInOut(duration: 0.25)) {
                    suggestions = Array(pool.shuffled().prefix(3))
                }
                isRefreshing = false
            } catch {
                isRefreshing = false
            }
        }
    }
}
