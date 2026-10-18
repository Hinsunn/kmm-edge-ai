//
// File        : OrbOverlay.swift
// Author      : Tran Van Hoai
// Date        : 18/10/26
//

import Foundation
import SwiftUI

struct OrbOverlay: View {
    var duration: Double = 10
    var orbSize: CGFloat = 200
    var onFinish: () -> Void

    @State private var bgOpacity: Double = 0
    @State private var orbScale: CGFloat = 0.3
    @State private var orbOpacity: Double = 0
    @State private var isClosing = false

    var body: some View {
        ZStack {
            Color.black
                .opacity(bgOpacity)
                .ignoresSafeArea()

            EnergyOrbMetal()
                .frame(width: orbSize, height: orbSize)
                .scaleEffect(orbScale)
                .opacity(orbOpacity)
        }
        .contentShape(Rectangle())
        .onTapGesture { Task { await close() } }
        .task {
            // 1. Animation Opacity Background
            withAnimation(.easeOut(duration: 0.4)) {
                bgOpacity = 0.85
            }

            // 2. Scale and Opacity Orb
            withAnimation(.spring(response: 0.5, dampingFraction: 0.75).delay(0.15)) {
                orbScale = 1
                orbOpacity = 1
            }

            // 3. Close after duration
            try? await Task.sleep(for: .seconds(duration))
            await close()
        }
    }

    @MainActor
    private func close() async {
        guard !isClosing else { return }
        isClosing = true

        withAnimation(.easeIn(duration: 0.35)) {
            bgOpacity = 0
            orbScale = 0.6
            orbOpacity = 0
        }
        try? await Task.sleep(for: .milliseconds(350))
        onFinish()
    }
}
