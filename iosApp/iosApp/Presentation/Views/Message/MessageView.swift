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
    var sessionId: UUID?
    @State var currentSession: SessionEntity?

    @Environment(LiteRTLMEngine.self) private var mlEngine
    @Environment(NavigationManager.self) private var navigationManager
    @Environment(\.modelContext) private var modelContext
    @Environment(\.dismiss) private var dismiss

    @FocusState private var isTextFieldFocused: Bool

    @State private var text: String = ""
    @State private var isRefreshing: Bool = false
    @State private var suggestions: [String] = Array(suggestionQuestions.shuffled().prefix(3))

    @State private var isLoadingMessage: Bool = false
    @State private var messages: [MessageEntity] = []

    @State private var streamingText: String = ""
    @State private var isStreaming = false
    @State private var streamTask: Task<Void, Never>?

    private var hasMessage: Bool {
        !messages.isEmpty
    }

    var body: some View {
        ZStack {
            Group {
                if isLoadingMessage {
                    MessagesSkeleton()
                } else {
                    if self.hasMessage {
                        ChatMessages(
                            messages: messages,
                            isStreaming: isStreaming,
                            streamingText: streamingText
                        )
                    } else {
                        MessageWelcoming(
                            suggestions: suggestions,
                            isRefreshing: isRefreshing,
                            onTapSuggestion: { suggestion in
                                self.text = suggestion
                                // uncomment to focus when suggestion tapped
                                // self.isTextFieldFocused = true
                            },
                            onRefresh: { self.refreshSuggestion() }
                        )
                    }
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        }
        .safeAreaInset(edge: .bottom, spacing: 0) {
            ChatComposer(
                focused: $isTextFieldFocused,
                text: $text,
                isSending: isStreaming
            ) {
                if !isStreaming {
                    sendMessage(message: text)
                } else {
                    cancelStreaming()
                }
            }
        }
        .background(Color(uiColor: .systemBackground))
        .contentShape(Rectangle())
        .onTapGesture {
            self.isTextFieldFocused = false
        }
        .navigationTitle("Messages")
        .navigationBarTitleDisplayMode(.inline)
        .navigationBarBackButtonHidden(true)
        .toolbar {
            ToolbarItem(placement: .topBarLeading) {
                Button {
                    dismiss()
                } label: {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 16, weight: .medium))
                }
                .tint(.primary)
            }
        }
        .toolbar(.hidden, for: .tabBar)
        .task { initialize() }
        .onDisappear { streamTask?.cancel() }
    }

    private func sendMessage(message: String) {
        let trimmed = message.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty, !isStreaming else { return }

        guard mlEngine.isReady else {
            nsLog(message: "ML Engine is not ready. Please wait for initialization.")
            return
        }

        // Insert human message into modelContext
        var isInsertedHumanMessage = false
        do {
            // This case only run when the user send message for the first time and currentSession is nil
            if currentSession == nil {
                let newSession = SessionEntity(title: message)
                modelContext.insert(newSession)
                try modelContext.save()
                currentSession = newSession
            }

            let humanMessage = MessageEntity(
                message: message,
                messageRole: .human,
                session: currentSession!
            )
            modelContext.insert(humanMessage)
            try modelContext.save()

            // Append to messages array to update UI
            messages.append(humanMessage)
            isInsertedHumanMessage = true
        } catch {
            nsLog(message: "Failed to save human message due to error: \(error)")
        }

        guard isInsertedHumanMessage else {
            nsLog(message: "Human message was not inserted into modelContext. Aborting sendMessage.")
            return
        }

        text = ""
        streamingText = ""
        isStreaming = true

        streamTask = Task { @MainActor in
            do {
                for try await chunk in mlEngine.streamMessage(trimmed) {
                    streamingText += chunk
                }
            } catch is CancellationError {
                nsLog(message: "Streaming was cancelled.")
            } catch {
                nsLog(message: "Stream failed: \(error)")
            }

            let aiMessage = MessageEntity(
                message: streamingText.trimmingCharacters(in: .whitespacesAndNewlines),
                messageRole: .ai,
                session: currentSession!
            )

            do {
                modelContext.insert(aiMessage)
                try modelContext.save()
                messages.append(aiMessage)
            } catch {
                nsLog(message: "Failed to save AI message due to error: \(error)")
            }

            isStreaming = false
            streamingText = ""
        }
    }

    private func cancelStreaming() {
        // cancel the streaming task if it exists and save the current streaming text as an AI message
        streamTask?.cancel()
        if !streamingText.isEmpty {
            let aiMessage = MessageEntity(
                message: streamingText.trimmingCharacters(in: .whitespacesAndNewlines),
                messageRole: .ai,
                session: currentSession!
            )

            do {
                modelContext.insert(aiMessage)
                try modelContext.save()
                messages.append(aiMessage)
            } catch {
                nsLog(message: "Failed to save AI message due to error: \(error)")
            }
        }

        isStreaming = false
        streamingText = ""
    }

    @MainActor
    private func initialize() {
        // Guard not loading and session id is not nil
        guard !isLoadingMessage, let sessionId = sessionId else { return }

        isLoadingMessage = true
        Task { @MainActor in
            do {
                isLoadingMessage = false
                let predicate = #Predicate<SessionEntity> { session in
                    session.id == sessionId
                }
                var descriptor = FetchDescriptor<SessionEntity>(predicate: predicate)
                descriptor.fetchLimit = 1

                let matchingSessions = try modelContext.fetch(descriptor)
                if matchingSessions.isEmpty {
                    nsLog(message: "No session found with ID: \(sessionId). Creating a new session.")
                    return
                }

                let session = matchingSessions[0]

                self.currentSession = session
                self.messages = session.messages.sorted { $0.createdAt < $1.createdAt }

                isLoadingMessage = false
                nsLog(message: "Loaded \(self.messages.count) messages for session ID: \(sessionId).")
            } catch {
                isLoadingMessage = false
                nsLog(message: "Failed to load messages for session ID: \(sessionId) due to error: \(error).")
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

    private func nsLog(message: String) {
        let now = Date().formatted(date: .omitted, time: .standard)
        NSLog("[LITERTLM_ENGINE | \(now)]: %@", message)
    }
}
