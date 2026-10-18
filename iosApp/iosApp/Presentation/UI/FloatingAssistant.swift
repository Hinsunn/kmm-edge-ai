//
// File        : FloatingAssistant.swift
// Author      : Tran Van Hoai
// Date        : 18/10/26
//

import Foundation
import SwiftUI

struct FloatingAssistant: View {
    var size: CGFloat = 64
    var margin: CGFloat = 6
    var topReserved: CGFloat = 8
    var bottomReserved: CGFloat = 90
    var onTap: () -> Void = {}

    private enum Side: String { case left, right }

    @AppStorage("floatingAssistant.side") private var side: Side = .right
    @AppStorage("floatingAssistant.yRatio") private var yRatio: Double = 0.65

    @State private var drag: CGSize = .zero
    @State private var isDragging = false

    var body: some View {
        GeometryReader { geo in
            let minX = margin + size / 2
            let maxX = geo.size.width - margin - size / 2
            let minY = geo.safeAreaInsets.top + topReserved + size / 2
            let maxY = max(minY, geo.size.height - geo.safeAreaInsets.bottom - bottomReserved - size / 2)

            let ratio = CGFloat(min(max(yRatio, 0), 1))

            let baseX = side == .left ? minX : maxX
            let baseY = minY + ratio * (maxY - minY)

            let liveX = min(max(baseX + drag.width, minX), maxX)
            let liveY = min(max(baseY + drag.height, minY), maxY)

            RobotMascot(size: size)
                .scaleEffect(isDragging ? 1.12 : 1)
                .contentShape(Rectangle())
                .onTapGesture(perform: onTap)
                .gesture(
                    DragGesture(minimumDistance: 4, coordinateSpace: .global)
                        .onChanged { value in
                            if !isDragging {
                                withAnimation(.spring(duration: 0.2)) { isDragging = true }
                            }
                            drag = value.translation
                        }
                        .onEnded { value in
                            let projectedX = baseX + value.predictedEndTranslation.width
                            let endY = min(max(baseY + value.translation.height, minY), maxY)

                            withAnimation(.spring(response: 0.45, dampingFraction: 0.72)) {
                                side = projectedX < geo.size.width / 2 ? .left : .right
                                yRatio = maxY > minY ? Double((endY - minY) / (maxY - minY)) : 0
                                drag = .zero
                                isDragging = false
                            }
                        }
                )
                .position(x: liveX, y: liveY)
        }
        .ignoresSafeArea(.keyboard)
    }
}
