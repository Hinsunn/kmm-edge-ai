import SwiftUI

struct ContentView: View {
    @State private var navigationManager = NavigationManager()

    var body: some View {
        BottomView().environment(navigationManager)
    }
}
