import Shared
import SwiftData
import SwiftUI

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Self.Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Self.Context) {}
}

struct ContentView: View {
    @Environment(\.modelContext) private var context
    @State private var mainViewModel: MainViewModel = .init()

    var body: some View {
        TabView {
            SessionView()
                .tabItem { Label("Sessions", systemImage: "app.badge.fill") }

            DownloadView()
                .tabItem { Label("Download", systemImage: "rectangle.stack") }

            MessageView()
                .tabItem { Label("Messages", systemImage: "bubble") }

            ComposeView()
                .ignoresSafeArea()
                .tabItem { Label("KMM App", systemImage: "bolt.square.fill") }
        }
        .environment(mainViewModel)
    }
}
