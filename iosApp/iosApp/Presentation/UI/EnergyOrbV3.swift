//
// File        : EnergyOrbV3.swift
// Author      : Tran Van Hoai
// Date        : 21/10/26
//

import AVFoundation
import SwiftUI

/// Orb v3: loops the pre-rendered smoke orb (`energy_orb.mov`, HEVC with alpha,
/// white smoke on transparent). HEVC is decoded by the hardware video decoder,
/// so playback costs almost no CPU, unlike decoding WebP frames in software.
struct EnergyOrbV3: View {
    var resource = "energy_orb"

    var body: some View {
        LoopingVideo(resource: resource)
            .aspectRatio(1, contentMode: .fit)
    }
}

private struct LoopingVideo: UIViewRepresentable {
    let resource: String

    func makeUIView(context: Context) -> PlayerView {
        let view = PlayerView()
        guard let url = Bundle.main.url(forResource: resource, withExtension: "mov") else { return view }
        let player = AVQueuePlayer()
        player.isMuted = true
        player.preventsDisplaySleepDuringVideoPlayback = false
        // Keep the looper alive as long as the view.
        view.looper = AVPlayerLooper(player: player, templateItem: AVPlayerItem(url: url))
        view.playerLayer.player = player
        player.play()
        return view
    }

    func updateUIView(_ uiView: PlayerView, context: Context) {}

    static func dismantleUIView(_ uiView: PlayerView, coordinator: ()) {
        uiView.playerLayer.player?.pause()
        uiView.looper = nil
    }
}

private final class PlayerView: UIView {
    var looper: AVPlayerLooper?

    override class var layerClass: AnyClass {
        AVPlayerLayer.self
    }

    var playerLayer: AVPlayerLayer {
        layer as! AVPlayerLayer
    }

    override init(frame: CGRect) {
        super.init(frame: frame)
        backgroundColor = .clear
        isOpaque = false
        playerLayer.videoGravity = .resizeAspect
        // Required for the alpha channel to show through instead of black.
        playerLayer.pixelBufferAttributes = [kCVPixelBufferPixelFormatTypeKey as String: kCVPixelFormatType_32BGRA]
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
