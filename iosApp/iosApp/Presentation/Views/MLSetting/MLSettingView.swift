//
// File        : SettingView.swift
// Author      : Tran Van Hoai
// Date        : 18/10/26
//

import Foundation
import SwiftData
import SwiftUI

enum InferenceBackend: String, Codable, CaseIterable {
    case npu
    case gpu
    case cpu
}

struct MLSettingView: View {
    /// Environments
    @Environment(LiteRTLMEngine.self) private var mlEngine

    /// Database
    @Query(sort: \LiteRTLMEntity.name) private var liteRTLMEntities: [LiteRTLMEntity]

    /// View States
    @State private var inferenceBackend: InferenceBackend = .gpu
    @State private var enabledAudio: Bool = false
    @State private var enabledVision: Bool = false
    @State private var enabledReasoning: Bool = false
    @State private var showModelSheet: Bool = false
    @State private var selectedModelId: UUID? = nil

    var body: some View {
        // Backend Settings: GPU, NPU, CPU
        List {
            Section {
                ForEach(InferenceBackend.allCases, id: \.self) { backend in
                    HStack {
                        Text(backend.rawValue.uppercased())
                        Spacer()

                        if inferenceBackend == backend {
                            Image(systemName: "checkmark")
                                .foregroundStyle(.blue)
                        }
                    }
                    .contentShape(Rectangle())
                    .onTapGesture {
                        inferenceBackend = backend
                    }
                    .listRowInsets(
                        EdgeInsets(top: 0, leading: 16, bottom: 0, trailing: 16)
                    )
                }
            } header: {
                sessionHeader("Inference Backend")
            }

            Section {
                HStack {
                    Text("LiteRTLM")
                    Spacer()
                    Text(
                        liteRTLMEntities.first {
                            $0.id == selectedModelId
                        }?.name ?? "Select Model"
                    )
                    .foregroundStyle(.secondary)
                    Image(systemName: "chevron.forward")
                }
                .contentShape(Rectangle())
                .onTapGesture { showModelSheet = true }
                .listRowInsets(
                    EdgeInsets(top: 0, leading: 16, bottom: 0, trailing: 16)
                )

                HStack {
                    Text("Audio")
                    Spacer()
                    Toggle("Audio", isOn: $enabledAudio)
                        .toggleStyle(.switch)
                        .tint(.accentColor)
                        .labelsHidden()
                }
                .listRowInsets(
                    EdgeInsets(top: 0, leading: 16, bottom: 0, trailing: 16)
                )

                HStack {
                    Text("Vision")
                    Spacer()
                    Toggle("Vision", isOn: $enabledVision)
                        .toggleStyle(.switch)
                        .tint(.accentColor)
                        .labelsHidden()
                }
                .listRowInsets(
                    EdgeInsets(top: 0, leading: 16, bottom: 0, trailing: 16)
                )

                HStack {
                    Text("Reasoning")
                    Spacer()
                    Toggle("Reasoning", isOn: $enabledReasoning)
                        .toggleStyle(.switch)
                        .tint(.accentColor)
                        .labelsHidden()
                }
                .listRowInsets(
                    EdgeInsets(top: 0, leading: 16, bottom: 0, trailing: 16)
                )

            } header: {
                sessionHeader("LiteRT Configuration")
            }
        }
        .listStyle(.insetGrouped)
        .navigationTitle("ML Settings")
        .sheet(isPresented: $showModelSheet) {
            ModelSelectionSheet(
                selectedModelId: $selectedModelId,
                onDismiss: { onDismiss() }
            )
            .presentationDetents([.medium, .large])
            .presentationDragIndicator(.visible)
        }
    }

    private func onDismiss() {
        Task {
            // retrieve the selected model entity from the database
            let selectedModel = liteRTLMEntities.first { $0.id == selectedModelId }
            if selectedModel == nil {
                nsLog(message: "Selected model not found in the database.")
                return
            }
            await mlEngine.initializeEngine(selectedModel!)
        }
    }

    private func sessionHeader(_ title: String) -> some View {
        Text(title)
            .font(.body)
            .textCase(nil)
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.leading, -16)
    }

    private func nsLog(message: String) {
        let now = Date().formatted(date: .numeric, time: .standard)
        NSLog("[MLSettingView|\(now)]: \(message)")
    }
}

struct ModelSelectionSheet: View {
    @Query(sort: \LiteRTLMEntity.name) private var models: [LiteRTLMEntity]
    @Environment(\.dismiss) private var dismiss

    @Binding var selectedModelId: UUID?
    var onDismiss: () -> Void

    var body: some View {
        NavigationStack {
            List {
                if models.isEmpty {
                    ContentUnavailableView(
                        "No Models",
                        systemImage: "cpu",
                        description: Text("Download a model to get started.")
                    )
                } else {
                    ForEach(models) { model in
                        Button {
                            selectedModelId = model.id
                        } label: {
                            HStack(spacing: 12) {
                                VStack(alignment: .leading, spacing: 4) {
                                    Text(model.name)
                                        .font(.body)
                                        .foregroundStyle(.primary)

                                    Text(model.summary)
                                        .font(.caption)
                                        .foregroundStyle(.secondary)
                                        .lineLimit(2)
                                }

                                Spacer()

                                if selectedModelId == model.id {
                                    Image(systemName: "checkmark")
                                        .foregroundStyle(.tint)
                                }
                            }
                            .contentShape(Rectangle())
                        }
                        .buttonStyle(.plain)
                    }
                }
            }
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Done") {
                        dismiss()
                        onDismiss()
                    }
                }
            }
        }
    }
}
