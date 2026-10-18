//
// File        : EnergyOrbMetal.swift
// Author      : Tran Van Hoai
// Date        : 18/10/26
//

import SwiftUI

struct EnergyOrbMetal: View {
    var tint: Color = .init(hue: 0.60, saturation: 0.85, brightness: 1.0)
    var speed: Double = 5.0

    @Environment(\.colorScheme) private var scheme
    @State private var start = Date()

    var body: some View {
        let highlight: Float = scheme == .dark ? 0.6 : 1.0
        let tint = tint
        let speed = speed
        let start = start

        TimelineView(.animation(minimumInterval: 1.0 / 30.0)) { timeline in
            let time = Float(timeline.date.timeIntervalSince(start) * speed)
            Rectangle()
                .fill(.white)
                .visualEffect { content, proxy in
                    content.colorEffect(
                        ShaderLibrary.energyOrb(
                            .float2(proxy.size),
                            .float(time),
                            .color(tint),
                            .float(highlight)
                        )
                    )
                }
        }
        .aspectRatio(1, contentMode: .fit)
    }
}
