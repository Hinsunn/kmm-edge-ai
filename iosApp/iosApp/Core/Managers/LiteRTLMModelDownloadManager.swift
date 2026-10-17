//
// File        : LiteRTLMModelDownloadManager.swift
// Author      : Tran Van Hoai
// Date        : 17/10/26
//

import Foundation
import SwiftData

@MainActor
class LiteRTLMModelDownloadManager: NSObject {
    /// The shared singleton instance of the LiteRTLMModelDownloadManager.
    static let shared = LiteRTLMModelDownloadManager()

    /// The maximum number of concurrent downloads allowed. This value is set to 5, meaning that at most 5 downloads can occur simultaneously.
    private let maxConcurrentDownloads = 5

    /// The background completion handler to be called when all background tasks are finished.
    var backgroundCompletionHandler: (() -> Void)?

    private var context: ModelContext {
        AppDatabase.container.mainContext
    }

    private let modelsDirectory: URL
    private var tasks: [UUID: URLSessionDownloadTask] = [:]
    private var lastProgressSave = Date.distantPast
    private lazy var session: URLSession = {
        let config = URLSessionConfiguration.background(withIdentifier: "org.hinsun.edge.download")
        config.sessionSendsLaunchEvents = true
        config.isDiscretionary = false
        config.httpMaximumConnectionsPerHost = maxConcurrentDownloads
        return URLSession(configuration: config, delegate: self, delegateQueue: .main)
    }()

    override init() {
        let support = FileManager.default.urls(for: .applicationSupportDirectory, in: .userDomainMask)[0]
        modelsDirectory = support.appendingPathComponent("Models", isDirectory: true)
        super.init()

        try? FileManager.default.createDirectory(at: modelsDirectory, withIntermediateDirectories: true)
        var values = URLResourceValues()
        values.isExcludedFromBackup = true
        var dir = modelsDirectory
        try? dir.setResourceValues(values)

        seed()
        reattach()
        fetchSize()
    }

    // MARK: - Public Methods

    func download(_ model: LiteRTLMEntity) {
        guard model.downloadStatus == .idle || model.downloadStatus == .failed else { return }
        set(model, .enqueued)
        pump()
    }

    func pause(_ model: LiteRTLMEntity) {
        switch model.downloadStatus {
        case .enqueued:
            set(model, .paused)
            save()
        case .downloading:
            set(model, .paused)
            if let task = tasks.removeValue(forKey: model.id) {
                Task {
                    let data = await task.cancelByProducingResumeData()
                    guard model.downloadStatus == .paused, let data else { return }
                    model.resumeData = data
                    self.save()
                }
            }
            pump()
        default:
            break
        }
    }

    func resume(_ model: LiteRTLMEntity) {
        guard model.downloadStatus == .paused || model.downloadStatus == .failed else { return }
        set(model, .enqueued)
        pump()
    }

    func remove(_ model: LiteRTLMEntity) {
        tasks.removeValue(forKey: model.id)?.cancel()
        try? FileManager.default.removeItem(at: fileURL(for: model))
        model.resumeData = nil
        model.errorMessage = nil
        model.downloadProgress = 0.0
        model.downloadedBytes = 0
        set(model, .idle)
        pump()
    }

    // MARK: - Private Methods

    private func fetchSize() {
        Task {
            for model in allModels() where model.totalBytes == 0 {
                guard var request = request(for: model) else { continue }

                nsLog("Fetching size for model \(model.name) from \(model.downloadURL)")
                request.httpMethod = "HEAD"
                // Follows Hugging Face's redirect to the CDN, whose response has the real length.
                guard let (_, response) = try? await URLSession.shared.data(for: request),
                      (response as? HTTPURLResponse)?.statusCode == 200,
                      response.expectedContentLength > 0
                else { continue }
                model.totalBytes = response.expectedContentLength
            }
            save()
        }
    }

    private func reattach() {
        Task {
            for case let task as URLSessionDownloadTask in await session.allTasks {
                guard let model = model(of: task), model.downloadStatus == .downloading else {
                    task.cancel()
                    continue
                }

                tasks[model.id] = task
            }

            for model in allModels() where model.downloadStatus == .downloading && tasks[model.id] == nil {
                set(model, .enqueued)
            }
            pump()
        }
    }

    private func seed() {
        nsLog("Seeding LiteRTLMEntity models into database")
        let existing = Dictionary(allModels().map { ($0.downloadURL, $0) }, uniquingKeysWith: { a, _ in a })
        for model in LiteRTLMEntity.availableModels {
            if let row = existing[model.downloadURL] {
                // Catalog copy lives in code; refresh it so edits reach rows seeded earlier
                row.name = model.name
                row.summary = model.summary
            } else {
                context.insert(model)
            }
        }

        for model in allModels()
            where model.downloadStatus == .completed && !FileManager.default.fileExists(atPath: fileURL(for: model).path)
        {
            model.downloadProgress = 0
            model.downloadedBytes = 0
            set(model, .idle)
        }

        save()
    }

    private func pump() {
        let models = allModels()
        var active = models.filter { $0.downloadStatus == .downloading }.count
        for model in models where model.downloadStatus == .enqueued && active < maxConcurrentDownloads {
            start(model)
            active += 1
        }
        save()
    }

    private func request(for model: LiteRTLMEntity) -> URLRequest? {
        guard let url = URL(string: model.downloadURL) else { return nil }
        var request = URLRequest(url: url)
        // Only send the token when it was actually resolved at build time.
        // An unresolved "$(HF_TOKEN)" placeholder would break downloads of public repos with 401.
        if let token = Bundle.main.object(forInfoDictionaryKey: "HFToken") as? String,
           token.hasPrefix("hf_")
        {
            request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }
        return request
    }

    private func start(_ model: LiteRTLMEntity) {
        guard let request = request(for: model) else {
            model.errorMessage = "Invalid download URL"
            set(model, .failed)
            return
        }

        let task =
            model.resumeData.map {
                session.downloadTask(withResumeData: $0)
            } ?? session.downloadTask(with: request)
        task.taskDescription = model.id.uuidString
        model.resumeData = nil
        model.errorMessage = nil
        set(model, .downloading)
        tasks[model.id] = task
        task.resume()
        nsLog("Started download for model \(model.name) with task \(task)")
    }

    private func set(_ model: LiteRTLMEntity, _ status: DownloadStatus) {
        model.downloadStatus = status
        model.updatedAt = LiteRTLMEntity.now
    }

    private func save() {
        do {
            try context.save()
        } catch {
            nsLog("Failed to save context: \(error)")
        }
    }

    private func fileURL(for model: LiteRTLMEntity) -> URL {
        modelsDirectory.appendingPathComponent(model.modelPath)
    }

    private func allModels() -> [LiteRTLMEntity] {
        (try? context.fetch(FetchDescriptor<LiteRTLMEntity>())) ?? []
    }

    fileprivate func model(of task: URLSessionTask) -> LiteRTLMEntity? {
        guard let id = task.taskDescription.flatMap(UUID.init(uuidString:)) else { return nil }
        let descriptor = FetchDescriptor<LiteRTLMEntity>(predicate: #Predicate { $0.id == id })
        return try? context.fetch(descriptor).first
    }

    // MARK: - Logging

    private func nsLog(_ message: String) {
        #if DEBUG
        NSLog("[LiteRTLMModelDownloadManager] \(message)")
        #endif
    }
}

extension LiteRTLMModelDownloadManager: URLSessionDownloadDelegate {
    nonisolated func urlSession(
        _: URLSession,
        downloadTask: URLSessionDownloadTask,
        didWriteData _: Int64,
        totalBytesWritten: Int64,
        totalBytesExpectedToWrite: Int64
    ) {
        MainActor.assumeIsolated {
            guard totalBytesExpectedToWrite > 0, let model = model(of: downloadTask) else { return }
            model.downloadProgress = Double(totalBytesWritten) / Double(totalBytesExpectedToWrite)
            model.downloadedBytes = totalBytesWritten
            model.totalBytes = totalBytesExpectedToWrite
            if Date().timeIntervalSince(lastProgressSave) > 2 {
                lastProgressSave = Date()
                save()
            }
        }
    }

    nonisolated func urlSession(
        _: URLSession,
        downloadTask: URLSessionDownloadTask,
        didFinishDownloadingTo location: URL
    ) {
        MainActor.assumeIsolated {
            guard let model = model(of: downloadTask) else { return }
            if let http = downloadTask.response as? HTTPURLResponse, !(200 ..< 300).contains(http.statusCode) {
                model.errorMessage = "HTTP \(http.statusCode)"
                set(model, .failed)
                save()
                return
            }
            let destination = fileURL(for: model)
            do {
                try? FileManager.default.removeItem(at: destination)
                try FileManager.default.moveItem(at: location, to: destination)
                model.downloadProgress = 1
                model.downloadedBytes = model.totalBytes
                set(model, .completed)
                nsLog("Download completed for model \(model.name) at \(destination.path)")
            } catch {
                model.errorMessage = error.localizedDescription
                set(model, .failed)
            }
            save()
        }
    }

    nonisolated func urlSession(
        _: URLSession,
        task: URLSessionTask,
        didCompleteWithError error: Error?
    ) {
        let resumeData = (error as NSError?)?.userInfo[NSURLSessionDownloadTaskResumeData] as? Data
        let message = error?.localizedDescription
        MainActor.assumeIsolated {
            guard let model = model(of: task) else { return }

            if tasks[model.id] !== task {
                // A task we no longer track: paused by the user, or killed in a previous launch and
                // reported now. Keep its resume data; if we already restarted from zero, switch to it.
                guard let resumeData, model.downloadStatus != .completed, model.downloadStatus != .idle else { return }
                model.resumeData = resumeData
                if model.downloadStatus == .downloading {
                    tasks[model.id]?.cancel()
                    start(model)
                }
                save()
                return
            }

            tasks[model.id] = nil
            if message != nil, model.downloadStatus == .downloading {
                model.resumeData = resumeData
                model.errorMessage = message
                set(model, .failed)
            }
            pump()
        }
    }

    nonisolated func urlSessionDidFinishEvents(forBackgroundURLSession _: URLSession) {
        MainActor.assumeIsolated {
            backgroundCompletionHandler?()
            backgroundCompletionHandler = nil
        }
    }
}
