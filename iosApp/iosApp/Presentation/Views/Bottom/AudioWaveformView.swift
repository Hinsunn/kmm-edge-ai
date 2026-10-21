//
// File        : AudioWaveformView.swift
// Author      : Tran Van Hoai
// Date        : 18/10/26
//

import Foundation
import SwiftUI

struct AudioWaveformView: View {
    private var monitor = AudioWaveformMonitor.shared

    var body: some View {
        VStack(spacing: 20) {
            Spacer()

            WaveformBars(levels: monitor.downsampledMagnitudes.map { $0 / magnitudeLimit })
                .frame(height: 300)
                .padding()

            Spacer()

            Button(action: {
                if monitor.isMonitoring {
                    monitor.stopMonitoring()
                } else {
                    Task { await monitor.startMonitoring() }
                }
            }) {
                Label(monitor.isMonitoring ? "Stop" : "Start", systemImage: monitor.isMonitoring ? "stop.fill" : "waveform")
                    .font(.title2.bold())
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(monitor.isMonitoring ? Color.red : Color.blue)
                    .foregroundStyle(.white)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .padding(.horizontal)
            }
        }
        .padding(.bottom, 20)
        .navigationTitle("Live Audio Waveform")
        .navigationBarTitleDisplayMode(.inline)
    }
}

/// Bar chart that eases each bar toward its latest level every display frame.
/// The mic only delivers ~10 FFT updates per second, so animating per frame in a
/// Canvas (instead of re-laying out a Chart on each update) keeps the motion smooth.
private struct WaveformBars: View {
    /// Target heights, 0...1.
    var levels: [Float]
    var gradient = Gradient(colors: [.pink, .purple, .blue])

    @State private var state = BarState()

    var body: some View {
        TimelineView(.animation) { timeline in
            Canvas { ctx, size in
                let heights = state.step(toward: levels, at: timeline.date)
                guard !heights.isEmpty else { return }
                let slot = size.width / CGFloat(heights.count)
                let barWidth = slot * 0.6
                let shading = GraphicsContext.Shading.linearGradient(
                    gradient,
                    startPoint: CGPoint(x: 0, y: 0),
                    endPoint: CGPoint(x: 0, y: size.height)
                )
                for (i, h) in heights.enumerated() {
                    let barHeight = max(2, CGFloat(h) * size.height)
                    let rect = CGRect(
                        x: CGFloat(i) * slot + (slot - barWidth) / 2,
                        y: size.height - barHeight,
                        width: barWidth,
                        height: barHeight
                    )
                    ctx.fill(Path(roundedRect: rect, cornerRadius: 1), with: shading)
                }
            }
        }
    }
}

/// Per-frame smoothing state; a class so updating it while drawing doesn't re-render.
private final class BarState {
    private var heights: [Float] = []
    private var last: Date?

    func step(toward targets: [Float], at now: Date) -> [Float] {
        if heights.count != targets.count {
            heights = Array(repeating: 0, count: targets.count)
        }
        let dt = Float(min(now.timeIntervalSince(last ?? now), 0.1))
        last = now
        for i in heights.indices {
            let target = min(max(targets[i], 0), 1)
            // Rise fast, fall slower, like a VU meter. Frame-rate independent.
            let rate: Float = target > heights[i] ? 20 : 8
            heights[i] += (target - heights[i]) * (1 - exp(-rate * dt))
        }
        return heights
    }
}
