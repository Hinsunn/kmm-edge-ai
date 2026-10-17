//
// File        : DownloadView.swift
// Author      : Tran Van Hoai
// Date        : 17/10/26
//

import Foundation
import SwiftData
import SwiftUI

struct DownloadView: View {
    @Query(sort: \LiteRTLMEntity.name) private var models: [LiteRTLMEntity]
    @Environment(\.modelContext) private var modelContext
    private let manager = LiteRTLMModelDownloadManager.shared

    var body: some View {
        NavigationStack {
            List(models) { model in
                VStack(alignment: .leading, spacing: 6) {
                    HStack {
                        VStack(alignment: .leading, spacing: 2) {
                            Text(model.name)
                                .font(.subheadline)
                                .lineLimit(1)

                            Spacer().frame(height: 2)
                            sizeText(model).font(.caption.monospacedDigit()).foregroundStyle(.secondary)
                        }
                        Spacer()

                        if model.downloadStatus != .idle && model.downloadStatus != .completed {
                            Text(model.downloadProgress * 100, format: .number.precision(.fractionLength(2)))
                                .font(.system(size: 14))
                                .foregroundStyle(.secondary)
                        }

                        HStack {
                            switch model.downloadStatus {
                            case .idle:
                                Button("Download", systemImage: "arrow.down.circle") {
                                    manager.download(model)
                                }
                                .font(.title3)
                            case .enqueued, .downloading:
                                Button("Pause", systemImage: "pause.circle") {
                                    manager.pause(model)
                                }
                                .font(.title3)
                            case .paused, .failed:
                                Button("Resume", systemImage: "play.circle") {
                                    manager.resume(model)
                                }
                                .font(.title3)
                            case .completed:
                                Button("Remove", systemImage: "trash", role: .destructive) {
                                    manager.remove(model)
                                }
                                .font(.system(size: 18, weight: .regular, design: .default))
                            }
                        }
                        .buttonStyle(.borderless)
                        .labelStyle(.iconOnly)
                    }

                    if model.downloadStatus != .idle && model.downloadStatus != .completed {
                        Spacer().frame(height: 2)
                        LinearProgressBar(progress: model.downloadProgress)
                        Spacer().frame(height: 2)
                    }

                    Text(model.summary)
                        .font(.system(size: 14))
                        .foregroundStyle(.secondary)

                    if let error = model.errorMessage {
                        Text(error).font(.caption).foregroundStyle(.red)
                    }
                }
            }
            .navigationTitle("LiteRTLM Models")
        }
        .task { initialize() }
    }

    private func initialize() {
        runGuard {
            let descriptor = FetchDescriptor<LiteRTLMEntity>(
                sortBy: [SortDescriptor(\.name, order: .forward)]
            )
            let models = try modelContext.fetch(descriptor)
            nsLog("Models: \(models.map(\.name))")
        }
    }

    private func sizeText(_ model: LiteRTLMEntity) -> Text {
        let total = model.totalBytes.formatted(.byteCount(style: .file))
        guard model.totalBytes > 0 else { return Text("0 MB") }
        switch model.downloadStatus {
        case .idle, .completed:
            return Text(total).font(.system(size: 14))
        default:
            return Text("\(model.downloadedBytes.formatted(.byteCount(style: .file)))  |  \(total)").font(.system(size: 14))
        }
    }

    private func runGuard(run: () throws -> Void) {
        do {
            try run()
        } catch {
            nsLog("Failed to run due to \(error)")
        }
    }

    private func nsLog(_ message: String) {
        NSLog("[DOWNLOAD]: \(message)")
    }
}
