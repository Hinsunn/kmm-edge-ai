//
// File        : OrbOverlay.swift
// Author      : Tran Van Hoai
// Date        : 18/10/26
//

import Foundation
import SwiftUI

struct OrbOverlay: View {
    var duration: Double = 10
    var orbSize: CGFloat = 120
    var onFinish: () -> Void

    @State private var bgOpacity: Double = 0
    @State private var orbScale: CGFloat = 0.3
    @State private var orbOpacity: Double = 0
    @State private var breath: CGFloat = 1
    @State private var isClosing = false

    var body: some View {
        ZStack {
            Color.black
                .opacity(bgOpacity)
                .ignoresSafeArea()

            OrbRipples(startRadius: orbSize * 0.20)
                .ignoresSafeArea()
                .opacity(orbOpacity)
                .allowsHitTesting(false)

            EnergyOrbMetal()
                .frame(width: orbSize, height: orbSize)
                .scaleEffect(orbScale * breath)
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

            // 3. Breathing: ease 1.0 -> 0.9, then loop 0.9 <-> 1.1
            try? await Task.sleep(for: .milliseconds(650))
            withAnimation(.easeInOut(duration: 0.8)) {
                breath = 0.9
            } completion: {
                withAnimation(.easeInOut(duration: 1.6).repeatForever(autoreverses: true)) {
                    breath = 1.1
                }
            }

            // 4. Close after duration
            try? await Task.sleep(for: .seconds(duration - 0.65))
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

private struct OrbRipples: View {
    var startRadius: CGFloat
    var tint: Color = .init(hue: 0.60, saturation: 0.85, brightness: 1.0)
    var lifetime: Double = 2.8   // seconds a single ring lives
    var interval: Double = 0.8   // seconds between rings

    @State private var start = Date()

    var body: some View {
        TimelineView(.animation) { timeline in
            let t = timeline.date.timeIntervalSince(start)
            Canvas { ctx, size in
                let c = CGPoint(x: size.width / 2, y: size.height / 2)
                let maxRadius = hypot(size.width, size.height) * 0.5
                let rings = ringStates(at: t, maxRadius: maxRadius)

                // Soft halo for all rings in one blurred layer, then a crisp core on top.
                ctx.drawLayer { glow in
                    glow.addFilter(.blur(radius: 14))
                    glow.blendMode = .plusLighter
                    for r in rings {
                        glow.stroke(circle(c, r.radius), with: .color(tint.opacity(r.alpha * 0.9)), lineWidth: r.width * 4)
                    }
                }
                ctx.blendMode = .plusLighter
                for r in rings {
                    ctx.stroke(circle(c, r.radius), with: .color(tint.opacity(r.alpha)), lineWidth: r.width)
                    ctx.stroke(circle(c, r.radius), with: .color(.white.opacity(r.alpha * 0.5)), lineWidth: r.width * 0.35)
                }
            }
        }
    }

    private func ringStates(at t: Double, maxRadius: CGFloat) -> [(radius: CGFloat, alpha: Double, width: CGFloat)] {
        guard t > 0 else { return [] }
        let newest = Int(t / interval)
        return (0...newest).reversed().compactMap { i in
            let p = (t - Double(i) * interval) / lifetime
            guard p >= 0, p < 1 else { return nil }
            let eased = 1 - pow(1 - p, 3)                  // ease-out cubic: fast at the orb, slows far away
            let fadeIn = min(p / 0.08, 1)                  // emerge from the orb edge instead of popping
            let alpha = fadeIn * pow(1 - p, 1.8)
            let radius = startRadius + (maxRadius - startRadius) * eased
            let width = 5 - 3.8 * CGFloat(eased)           // thins out as it spreads
            return (radius, alpha, width)
        }
    }

    private func circle(_ c: CGPoint, _ r: CGFloat) -> Path {
        Path(ellipseIn: CGRect(x: c.x - r, y: c.y - r, width: r * 2, height: r * 2))
    }
}
