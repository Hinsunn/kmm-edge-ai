//
// File        : EnergyOrbMetalV2.swift
// Author      : Tran Van Hoai
// Date        : 21/10/26
//

import SwiftUI

/// Smoky white ring orb (v2). Premultiplied alpha output, so it composites over any background.
struct EnergyOrbMetalV2: View {
    var tint: Color = .white
    var speed: Double = 1.0

    @State private var start = Date()

    var body: some View {
        let tint = tint
        let speed = speed
        let start = start

        TimelineView(.animation(minimumInterval: 1.0 / 60.0)) { timeline in
            let time = Float(timeline.date.timeIntervalSince(start) * speed)
            Rectangle()
                .fill(.white)
                .visualEffect { content, proxy in
                    content.colorEffect(
                        ShaderLibrary.energyOrbV2(
                            .float2(proxy.size),
                            .float(time),
                            .color(tint)
                        )
                    )
                }
        }
        .aspectRatio(1, contentMode: .fit)
    }
}

#Preview {
    EnergyOrbMetalV2()
        .frame(width: 300, height: 300)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(.black)
}
