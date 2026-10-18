//
// File        : RobotMascot.swift
// Author      : Tran Van Hoai
// Date        : 18/10/26
//

import Foundation
import SwiftUI

struct RobotMascot: View {
    var size: CGFloat = 120
    var floatAmount: CGFloat = 1

    @State private var eyeScale: CGFloat = 1

    private let eyeColor = Color(red: 0.80, green: 0.97, blue: 0.86)
    private let glowColor = Color(red: 0.45, green: 0.72, blue: 1.0)
    private let outlineColor = Color(red: 0.80, green: 0.82, blue: 0.90)

    private var headGradient: LinearGradient {
        LinearGradient(
            colors: [
                Color.white,
                Color(red: 0.97, green: 0.98, blue: 1.0),
                Color(red: 0.93, green: 0.95, blue: 0.98)
            ],
            startPoint: .topLeading,
            endPoint: .bottomTrailing
        )
    }

    private var rimGradient: AngularGradient {
        AngularGradient(
            colors: [
                Color(red: 0.35, green: 0.78, blue: 1.00),
                Color(red: 0.62, green: 0.50, blue: 1.00),
                Color(red: 1.00, green: 0.45, blue: 0.78),
                Color(red: 1.00, green: 0.68, blue: 0.30),
                Color(red: 0.95, green: 0.90, blue: 0.35),
                Color(red: 0.40, green: 0.90, blue: 0.60),
                Color(red: 0.35, green: 0.78, blue: 1.00)
            ],
            center: .center
        )
    }

    var body: some View {
        TimelineView(.animation) { tl in
            let t = tl.date.timeIntervalSinceReferenceDate
            let s = CGFloat(sin(t * 1.3))
            let dy = s * size * 0.035 * floatAmount
            let dx = CGFloat(sin(t * 0.8 + 1.2)) * size * 0.02 * floatAmount
            let rot = Double(sin(t * 0.9 + 0.5)) * 1.5 * Double(floatAmount)

            ZStack {
                Ellipse()
                    .fill(glowColor.opacity(0.14 + 0.04 * Double(s)))
                    .frame(width: size * 0.46, height: size * 0.07)
                    .scaleEffect(1 + 0.10 * s * floatAmount)
                    .blur(radius: size * 0.025)
                    .offset(y: size * 0.46)

                robotBody
                    .rotationEffect(.degrees(rot))
                    .offset(x: dx, y: dy)
            }
            .frame(width: size, height: size)
        }
        .frame(width: size, height: size)
        .task { await blinkLoop() }
    }

    private var robotBody: some View {
        let w = size

        return ZStack {
            ForEach([CGFloat(-1), CGFloat(1)], id: \.self) { dir in
                RoundedRectangle(cornerRadius: w * 0.05, style: .continuous)
                    .fill(headGradient)
                    .overlay(
                        RoundedRectangle(cornerRadius: w * 0.05, style: .continuous)
                            .strokeBorder(outlineColor.opacity(0.8), lineWidth: w * 0.01)
                    )
                    .frame(width: w * 0.10, height: w * 0.22)
                    .offset(x: dir * w * 0.44, y: w * 0.03)
            }

            RoundedRectangle(cornerRadius: w * 0.06, style: .continuous)
                .fill(headGradient)
                .overlay(
                    RoundedRectangle(cornerRadius: w * 0.06, style: .continuous)
                        .strokeBorder(outlineColor.opacity(0.8), lineWidth: w * 0.01)
                )
                .frame(width: w * 0.15, height: w * 0.14)
                .offset(y: -w * 0.37)

            RoundedRectangle(cornerRadius: w * 0.32, style: .continuous)
                .fill(headGradient)
                .overlay(
                    RoundedRectangle(cornerRadius: w * 0.32, style: .continuous)
                        .strokeBorder(outlineColor.opacity(0.8), lineWidth: w * 0.012)
                )
                .frame(width: w * 0.84, height: w * 0.70)

            ZStack {
                RoundedRectangle(cornerRadius: w * 0.26, style: .continuous)
                    .fill(
                        LinearGradient(
                            colors: [Color(white: 0.17), Color(white: 0.05)],
                            startPoint: .top, endPoint: .bottom
                        )
                    )

                RoundedRectangle(cornerRadius: w * 0.26, style: .continuous)
                    .strokeBorder(rimGradient, lineWidth: w * 0.03)
                    .blur(radius: w * 0.02)
                    .opacity(0.7)

                RoundedRectangle(cornerRadius: w * 0.26, style: .continuous)
                    .strokeBorder(rimGradient, lineWidth: w * 0.02)
            }
            .frame(width: w * 0.70, height: w * 0.50)
            .offset(y: w * 0.02)

            HStack(spacing: w * 0.14) {
                eye(w)
                eye(w)
            }
            .offset(y: w * 0.02)
        }
        .shadow(color: glowColor.opacity(0.18), radius: w * 0.10, y: w * 0.05)
        .shadow(color: .black.opacity(0.07), radius: w * 0.03, y: w * 0.025)
    }

    private func eye(_ w: CGFloat) -> some View {
        Capsule()
            .fill(eyeColor)
            .frame(width: w * 0.085, height: w * 0.17)
            .scaleEffect(y: eyeScale, anchor: .center)
            .shadow(color: eyeColor.opacity(0.7), radius: w * 0.02)
    }

    @MainActor
    private func blinkLoop() async {
        while !Task.isCancelled {
            try? await Task.sleep(for: .seconds(Double.random(in: 1.8 ... 5.0)))

            let times = Int.random(in: 0..<10) < 3 ? 2 : 1 // 30% chớp đôi
            for i in 0..<times {
                withAnimation(.easeIn(duration: 0.07)) { eyeScale = 0.08 }
                try? await Task.sleep(for: .milliseconds(90))
                withAnimation(.easeOut(duration: 0.10)) { eyeScale = 1 }
                if i < times - 1 {
                    try? await Task.sleep(for: .milliseconds(140))
                }
            }
        }
        eyeScale = 1
    }
}
