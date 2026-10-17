import Shared
import SwiftUI

@main
struct iOSApp: App {

    init() {
        LLMProvider.shared.engine = SwiftLLMEngine()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
