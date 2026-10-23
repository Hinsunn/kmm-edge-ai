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
    @State private var isClosing = false
    private var monitor = AudioWaveformMonitor.shared

    var body: some View {
        ZStack {
            Color.black
                .opacity(bgOpacity)
                .ignoresSafeArea()

            OrbRipples(startRadius: orbSize * 0.20)
                .ignoresSafeArea()
                .opacity(orbOpacity)
                .allowsHitTesting(false)

            RadialBars(
                levels: monitor.downsampledMagnitudes.map(barLevel),
                innerRadius: orbSize * 0.5,
                maxLength: orbSize * 0.6
            )
            .frame(width: orbSize * 3, height: orbSize * 3)
            .scaleEffect(orbScale)
            .opacity(orbOpacity)
            .allowsHitTesting(false)

            EnergyOrbV4V2()
                .frame(width: orbSize, height: orbSize)
                .scaleEffect(orbScale)
                .opacity(orbOpacity)
        }
        .contentShape(Rectangle())
        .onTapGesture { Task { await close() } }
        .task {
            // Separate task: startMonitoring loops over the mic stream until cancelled
            guard !monitor.isMonitoring else { return }
            await monitor.startMonitoring()
        }
        .onDisappear { monitor.stopMonitoring() }
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

private let barNoiseFloor: Float = 1.5
private let barGain: Float = 3

private func barLevel(_ magnitude: Float) -> Float {
    let x = max(magnitude - barNoiseFloor, 0) / magnitudeLimit * barGain
    return min(x, 1).squareRoot()
}

private struct RadialBars: View {
    var levels: [Float]
    var innerRadius: CGFloat
    var maxLength: CGFloat
    var tint: Color = .init(red: 0.80, green: 0.82, blue: 0.86) // silver

    @State private var state = BarState()

    var body: some View {
        TimelineView(.animation) { timeline in
            // Mirror the spectrum so the ring closes without a seam between high and low bins
            let heights = state.step(toward: levels + levels.reversed(), at: timeline.date)
            Canvas { ctx, size in
                guard !heights.isEmpty else { return }
                let c = CGPoint(x: size.width / 2, y: size.height / 2)
                let step = 2 * .pi / CGFloat(heights.count)
                let barWidth = max(2, innerRadius * step * 0.45)

                var path = Path()
                for (i, h) in heights.enumerated() {
                    let angle = CGFloat(i) * step - .pi / 2
                    let length = 4 + CGFloat(h) * maxLength // 5pt resting height when silent
                    // Bar laid out along +x from the orb edge, then rotated into place around the center
                    let bar = CGRect(x: innerRadius, y: -barWidth / 2, width: length, height: barWidth)
                    let transform = CGAffineTransform(rotationAngle: angle)
                        .concatenating(CGAffineTransform(translationX: c.x, y: c.y))
                    path.addPath(Path(roundedRect: bar, cornerRadius: 0.5), transform: transform)
                }

                ctx.fill(path, with: .color(tint))
            }
        }
    }
}

private struct OrbRipples: View {
    var startRadius: CGFloat
    var tint: Color = .init(red: 0.80, green: 0.82, blue: 0.86) // silver
    var lifetime: Double = 2.8
    var interval: Double = 0.9

    @State private var start = Date()

    var body: some View {
        TimelineView(.animation) { timeline in
            let t = timeline.date.timeIntervalSince(start)
            Canvas { ctx, size in
                let c = CGPoint(x: size.width / 2, y: size.height / 2)
                let maxRadius = hypot(size.width, size.height) * 0.5
                let rings = ringStates(at: t, maxRadius: maxRadius)

                // Fake glow with wide, faint strokes instead of a full-screen blur
                // layer (an offscreen blur pass per frame is too heavy on older iPhones).
                ctx.blendMode = .plusLighter
                for r in rings {
                    let path = circle(c, r.radius)
                    ctx.stroke(path, with: .color(tint.opacity(r.alpha * 0.10)), lineWidth: r.width * 7)
                    ctx.stroke(path, with: .color(tint.opacity(r.alpha * 0.20)), lineWidth: r.width * 3.5)
                    ctx.stroke(path, with: .color(tint.opacity(r.alpha)), lineWidth: r.width)
                    ctx.stroke(path, with: .color(.white.opacity(r.alpha * 0.5)), lineWidth: r.width * 0.35)
                }
            }
        }
    }

    private func ringStates(at t: Double, maxRadius: CGFloat) -> [(radius: CGFloat, alpha: Double, width: CGFloat)] {
        guard t > 0 else { return [] }
        let newest = Int(t / interval)
        return (0 ... newest).reversed().compactMap { i in
            let p = (t - Double(i) * interval) / lifetime
            guard p >= 0, p < 1 else { return nil }
            let eased = 1 - pow(1 - p, 3) // ease-out cubic: fast at the orb, slows far away
            let fadeIn = min(p / 0.08, 1) // emerge from the orb edge instead of popping
            let alpha = fadeIn * pow(1 - p, 1.8)
            let radius = startRadius + (maxRadius - startRadius) * eased
            let width = 5 - 3.8 * CGFloat(eased) // thins out as it spreads
            return (radius, alpha, width)
        }
    }

    private func circle(_ c: CGPoint, _ r: CGFloat) -> Path {
        Path(ellipseIn: CGRect(x: c.x - r, y: c.y - r, width: r * 2, height: r * 2))
    }
}
