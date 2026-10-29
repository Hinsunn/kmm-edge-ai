import Shared
import SwiftUI

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Self.Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Self.Context) {}
}

struct ContentView: View {
    @State private var navigationManager = NavigationManager()

    var body: some View {
        BottomView().environment(navigationManager)

        // ComposeView()
        //     .ignoresSafeArea()
    }
}
