//
// File        : EnergyOrb.swift
// Author      : Tran Van Hoai
// Date        : 18/10/26
//

import Foundation
import simd
import SwiftUI

struct EnergyOrb: View {
    var hue: Double = 0.60
    var speed: Double = 1.0
    var ribbons = 5
    var strands = 48
    var segments = 48

    var body: some View {
        TimelineView(.animation) { tl in
            let t = tl.date.timeIntervalSinceReferenceDate * speed
            ZStack {
                orb(t: t).blur(radius: 10)
                orb(t: t)
            }
        }
        .aspectRatio(1, contentMode: .fit)
    }

    private func orb(t: Double) -> some View {
        Canvas { ctx, size in
            ctx.blendMode = .plusLighter

            let c = CGPoint(x: size.width / 2, y: size.height / 2)
            let R = min(size.width, size.height) * 0.42
            let yaw = t * 0.25
            let tilt = 0.35 + 0.15 * sin(t * 0.3)

            func spherical(_ lat: Double, _ lon: Double) -> SIMD3<Double> {
                SIMD3(cos(lat) * sin(lon), sin(lat), cos(lat) * cos(lon))
            }
            func rotate(_ p: SIMD3<Double>) -> SIMD3<Double> {
                let x1 = p.x * cos(yaw) + p.z * sin(yaw)
                let z1 = -p.x * sin(yaw) + p.z * cos(yaw)
                let y2 = p.y * cos(tilt) - z1 * sin(tilt)
                let z2 = p.y * sin(tilt) + z1 * cos(tilt)
                return SIMD3(x1, y2, z2)
            }

            // Viền + ánh sáng nền bên trong quả cầu
            let disk = Path(ellipseIn: CGRect(x: c.x - R, y: c.y - R, width: 2 * R, height: 2 * R))
            ctx.fill(disk, with: .radialGradient(
                Gradient(colors: [
                    Color(hue: hue, saturation: 0.9, brightness: 0.6).opacity(0.0),
                    Color(hue: hue, saturation: 0.9, brightness: 0.7).opacity(0.22)
                ]),
                center: c, startRadius: 0, endRadius: R
            ))
            ctx.stroke(disk,
                       with: .color(Color(hue: hue, saturation: 0.7, brightness: 1).opacity(0.35)),
                       lineWidth: 1.2)

            for r in 0..<ribbons {
                let rf = Double(r)
                let base = rf / Double(ribbons) * 2 * .pi

                // Một "đường xương sống" của dải lụa. side = 0 là mép A, side = 1 là mép B
                func spine(_ s: Double, _ side: Double) -> SIMD3<Double> {
                    let lat = (s - 0.5) * .pi * 0.92
                        + 0.35 * sin(t * 0.6 + rf * 1.7 + s * 4 + side * 1.5)
                    let width = 0.55 + 0.45 * sin(t * 0.5 + rf + s * 3.0)
                    let lon = base + s * 2.4 + side * width + 0.4 * sin(t * 0.4 + rf * 2)
                    return spherical(lat, lon)
                }

                for k in 0..<strands {
                    let f = Double(k) / Double(strands - 1)
                    var front = Path(), back = Path()
                    var prev: (pt: CGPoint, front: Bool)?

                    for i in 0 ... segments {
                        let s = Double(i) / Double(segments)
                        let a = spine(s, 0), b = spine(s, 1)
                        var p = a + (b - a) * f
                        let len = length(p)
                        if len < 1e-3 {
                            prev = nil; continue
                        }
                        p /= len // ép lên mặt cầu

                        let q = rotate(p)
                        let pt = CGPoint(x: c.x + q.x * R, y: c.y - q.y * R)
                        let isFront = q.z >= 0

                        if let pr = prev {
                            if isFront {
                                front.move(to: pr.pt); front.addLine(to: pt)
                            } else {
                                back.move(to: pr.pt); back.addLine(to: pt)
                            }
                        }
                        prev = (pt, isFront)
                    }

                    // Mép dải sáng và trắng hơn, giữa dải trong và đậm hơn
                    let edge = pow(abs(f - 0.5) * 2, 2.5)
                    let color = Color(hue: hue - 0.03 * edge,
                                      saturation: 0.85 - 0.55 * edge,
                                      brightness: 1)
                    ctx.stroke(back, with: .color(color.opacity(0.03 + 0.08 * edge)), lineWidth: 0.6)
                    ctx.stroke(front, with: .color(color.opacity(0.06 + 0.22 * edge)), lineWidth: 0.7)
                }
            }
        }
    }
}
