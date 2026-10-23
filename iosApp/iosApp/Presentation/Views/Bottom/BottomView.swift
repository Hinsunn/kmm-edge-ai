//
// File        : BottomView.swift
// Author      : Tran Van Hoai
// Date        : 18/10/26
//

import Foundation
import Shared
import SwiftUI

struct BottomView: View {
    @State private var showOrb: Bool = false
    @Environment(NavigationManager.self) private var navigationManager

    var body: some View {
        @Bindable var nav = navigationManager

        TabView {
            NavigationStack(path: $nav.path) {
                AudioWaveformView()
            }
            .tabItem { Label("Waveform", systemImage: "airplay.audio") }

            NavigationStack(path: $nav.path) {
                SessionView()
                    .navigationDestination(for: NavRoute.self) { route in
                        destination(for: route)
                    }
            }
            .tabItem { Label("Sessions", systemImage: "bubble.left.fill") }

            NavigationStack(path: $nav.path) {
                MLSettingView()
            }
            .tabItem { Label("Settings", systemImage: "cube.fill") }

            NavigationStack(path: $nav.path) {
                DownloadView()
            }
            .tabItem { Label("Download", systemImage: "arrow.down.square.fill") }
        }
        .overlay {
            if !showOrb && !isMessageView {
                FloatingAssistant(size: 48) {
                    showOrb = true
                }
            }
        }
        .overlay {
            if showOrb && !isMessageView {
                OrbOverlay { showOrb = false }
            }
        }
        // Parse the orb animation in the background so tapping the mascot opens it instantly.
        .task { await EnergyOrbV4V2.preload() }
        .onEvent(DatabaseGlobalEvent.self) { event in
            switch event {
            case .messageCleanup:
                print("Message cleanup event received")
            case .sessionCleanup:
                print("Session cleanup event received")
            }
        }
    }

    @ViewBuilder
    private func destination(for route: NavRoute) -> some View {
        switch route {
        case .bottom:
            EmptyView()
        case .message(let sessionId):
            MessageView(sessionId: sessionId)
        }
    }

    private var isMessageView: Bool {
        navigationManager.path.contains { route in
            if case .message = route {
                return true
            }
            return false
        }
    }
}
