//
// File        : EnergyOrbV4.swift
// Author      : Tran Van Hoai
// Date        : 21/10/26
//

import CoreImage
import DotLottie
import Metal
import SwiftUI
import UIKit

/// Orb v4: plays `energy_orb.lottie` (dotLottie, white smoke on transparent) on a loop.
///
/// Image assets inside the file must be marked embedded (`"e": 1`). With `"e": 0` the
/// native renderer looks for external image files and draws a placeholder icon instead,
/// even though web players render the same file fine.
///
/// Parsing the file (8MB JSON + 90 image frames) takes ~0.3s on a Mac simulator and
/// longer on device, so it is done once, off the main thread, via `preload()`, and the
/// same instance is reused every time the orb appears.
struct EnergyOrbV4: View {
    @MainActor private static var shared: DotLottieAnimation?
    @MainActor private static var loading: Task<DotLottieAnimation, Never>?

    @State private var animation = EnergyOrbV4.shared

    /// Call early (e.g. when the screen showing the assistant appears) so the first
    /// tap opens the orb instantly. Safe to call repeatedly.
    @MainActor
    static func preload() async {
        _ = await load()
    }

    @MainActor
    private static func load() async -> DotLottieAnimation {
        if let shared {
            return shared
        }
        if let loading {
            return await loading.value
        }
        // Render size of the orb in OrbOverlay (200pt) in pixels, so the first draw
        // doesn't have to reallocate the player's buffer.
        let px = Int(200 * UIScreen.main.scale)
        let task = Task.detached(priority: .userInitiated) {
            // A fresh object with no observers yet, so building it off-main is safe.
            let animation = DotLottieAnimation(
                fileName: "energy_orb",
                config: AnimationConfig(autoplay: true, loop: true)
            )
            // Warm the first frame and Core Image (DotLottieView creates a CIContext
            // per view), which otherwise stall the main thread on the first open.
            animation.resize(width: px, height: px)
            _ = animation.tick(milliseconds: 0)
            if let device = MTLCreateSystemDefaultDevice() {
                let context = CIContext(mtlDevice: device, options: [.cacheIntermediates: false, .allowLowPower: true])
                _ = context.createCGImage(CIImage(color: .clear), from: CGRect(x: 0, y: 0, width: 1, height: 1))
            }

            return animation
        }

        loading = task
        let animation = await task.value
        shared = animation
        loading = nil
        return animation
    }

    var body: some View {
        ZStack {
            if let animation {
                animation.view()
                    .allowsHitTesting(false)
            }
        }
        .aspectRatio(1, contentMode: .fit)
        .task {
            // Tapped before preload finished: wait for it instead of blocking the UI.
            if animation == nil {
                animation = await Self.load()
            }
        }
    }
}
