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
    @State private var store: SessionStore
    @State private var mlEngine: LiteRTLMEngine

    init() {
        LLMProvider.shared.engine = SwiftLLMEngine()
        _store = State(initialValue: SessionStore(context: AppDatabase.container.mainContext))
        _mlEngine = State(initialValue: LiteRTLMEngine())
    }

    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
        .environment(store)
        .environment(mlEngine)
        .modelContainer(AppDatabase.container)
    }
}
