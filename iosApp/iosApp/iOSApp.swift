import Shared
import SwiftUI

final class AppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        _ = LiteRTLMModelDownloadManager.shared
        return true
    }

    func application(
        _ application: UIApplication,
        handleEventsForBackgroundURLSession identifier: String,
        completionHandler: @escaping () -> Void
    ) {
        LiteRTLMModelDownloadManager.shared.backgroundCompletionHandler = completionHandler
    }
}

@main
struct iOSApp: App {
    @State private var mlEngine: LiteRTLMEngine
    @State private var globalEventDriven: GlobalEventDriven

    init() {
        LLMProvider.shared.engine = SwiftLLMEngine()
        _mlEngine = State(initialValue: LiteRTLMEngine())
        _globalEventDriven = State(initialValue: GlobalEventDriven(modelContext: AppDatabase.container.mainContext))
    }

    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate
    var body: some Scene {
        WindowGroup {
            ContentView()
                .task { DeviceSystem.trace() }
        }
        .environment(globalEventDriven)
        .environment(mlEngine)
        .modelContainer(AppDatabase.container)
    }
}
