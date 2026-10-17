//
// File        : SessionView.swift
// Author      : Tran Van Hoai
// Date        : 17/10/26
//

import Foundation
import SwiftData
import SwiftUI

struct SessionView: View {
    @Environment(\.modelContext) private var context
    @Query(sort: \SessionEntity.createdAt) private var sessions: [SessionEntity]

    @Environment(MainViewModel.self) private var mainViewModel

    var body: some View {
        NavigationStack {
            VStack(alignment: .leading) {
                List(sessions) { session in
                    HStack {
                        Text(session.title)
                        Spacer()
                        Button {
                            withAnimation {
                                deleteSession(session)
                            }
                        } label: {
                            Image(systemName: "trash")
                                .foregroundColor(.red)
                        }
                        .buttonStyle(.plain)
                    }
                    .onTapGesture { mainViewModel.setActiveSessionId(session.id) }
                }
            }
            .navigationTitle("Sessions")
        }
        .task { initialize() }
    }

    private func deleteSession(_ session: SessionEntity) {
        runGuard {
            context.delete(session)
            try context.save()
        }
    }

    private func initialize() {
        guard sessions.isEmpty else { return }

        runGuard {
            let randomSessionTitles: [String] = [
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

            for title in randomSessionTitles {
                let session: SessionEntity = .init(title: title)
                context.insert(session)
            }

            try context.save()
        }
    }

    private func runGuard(_ runTask: () throws -> Void) {
        do {
            try runTask()
        } catch {
            NSLog("[SESSION]: Failed to run task: \(error)")
        }
    }
}
