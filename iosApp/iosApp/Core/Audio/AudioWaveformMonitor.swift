//
// File        : AudioWaveformMonitor.swift
// Author      : Tran Van Hoai
// Date        : 21/10/26
//

import Accelerate
import AVFoundation
import Foundation

/// a. Amount of frequency bins to keep after performing the FFT
let sampleAmount: Int = 200
/// b. Reduce the number of plotted points
let downsampleFactor = 8
/// c. Handle high spikes distortion in the chart
let magnitudeLimit: Float = 100

@MainActor
@Observable
final class AudioWaveformMonitor {
    static let shared = AudioWaveformMonitor()
    private init() {}

    var isMonitoring = false
    private let audioEngine = AVAudioEngine()

    private let bufferSize = 8192
    private var fftSetup: OpaquePointer?

    var fftMagnitudes = [Float](repeating: 0, count: sampleAmount)
    var downsampledMagnitudes: [Float] {
        fftMagnitudes.lazy.enumerated().compactMap { idx, val in
            idx.isMultiple(of: downsampleFactor) ? val : nil
        }
    }

    func startMonitoring() async {
        // Check permissions for microphone access
        let granted = await AVAudioApplication.requestRecordPermission()
        guard granted else {
            print("Microphone access denied")
            return
        }

        // The input node has no valid format (0 Hz / 0 channels) until a recording
        // session is active, and installTap throws an ObjC exception on that format.
        // Configured off the main thread: these calls block and Apple warns about UI stalls.
        do {
            try await Task.detached(priority: .userInitiated) {
                let session = AVAudioSession.sharedInstance()
                try session.setCategory(.playAndRecord, mode: .measurement, options: [.defaultToSpeaker, .mixWithOthers])
                try session.setActive(true)
            }.value
        } catch {
            print("Error configuring audio session: \(error.localizedDescription)")
            return
        }

        let inputNode = audioEngine.inputNode
        let inputFormat = inputNode.outputFormat(forBus: 0)
        guard inputFormat.sampleRate > 0, inputFormat.channelCount > 0 else {
            // e.g. Simulator with no microphone available to the Mac.
            print("No audio input available (format: \(inputFormat))")
            return
        }
        fftSetup = vDSP_DFT_zop_CreateSetup(nil, UInt(bufferSize), .FORWARD)

        // Listen to microphone input
        let audioStream = AsyncStream<[Float]> { continuation in
            inputNode.installTap(onBus: 0, bufferSize: UInt32(bufferSize), format: inputFormat) { @Sendable buffer, _ in
                // 1. Access the first audio channel
                let channelData = buffer.floatChannelData?[0]
                let frameCount = Int(buffer.frameLength)

                // 2. Convert it into a Float array
                let floatData = Array(UnsafeBufferPointer(start: channelData, count: frameCount))

                // 3. Yield into the stream
                continuation.yield(floatData)
            }
        }

        do {
            // 1. Start the audioEngine
            try audioEngine.start()
            // 2. Update the property to monitor the state of the audioEngine
            isMonitoring = true
        } catch {
            print("Error starting audio engine: \(error.localizedDescription)")
            return
        }

        // 3. Retrieving the data from the audioStream
        for await buffers in audioStream {
            // 4. For each buffer, compute the FFT and store the results
            fftMagnitudes = await performFFT(on: buffers)
        }
    }

    func stopMonitoring() {
        // 1. Stop the audioEngine
        audioEngine.stop()

        // 2. Remove the tap from the microphone input
        audioEngine.inputNode.removeTap(onBus: 0)

        // 3. Reset the fftMagnitudes array to all zeros, to clear the visualization
        fftMagnitudes = [Float](repeating: 0, count: sampleAmount)

        // 4. Release the FFT setup free system memory
        if let setup = fftSetup {
            vDSP_DFT_DestroySetup(setup)
            fftSetup = nil
        }

        // 5. Update the audioEngine state property
        isMonitoring = false
    }

    func performFFT(on buffers: [Float]) async -> [Float] {
        // Check the configuration
        guard let setup = fftSetup else {
            return [Float](repeating: 0, count: sampleAmount)
        }

        // 1. Copy of the audio samples as float
        // The tap may deliver fewer (or more) frames than requested; the DFT reads exactly
        // bufferSize samples, so pad/trim to avoid reading past the array.
        var realIn = Array(buffers.prefix(bufferSize))
        realIn += [Float](repeating: 0, count: bufferSize - realIn.count)
        // 2. The imaginary part
        var imagIn = [Float](repeating: 0, count: bufferSize)

        // 3. The transformed values of the real data
        var realOut = [Float](repeating: 0, count: bufferSize)
        // The transformed values of the imaginary data
        var imagOut = [Float](repeating: 0, count: bufferSize)

        // Property storing computed magnitudes
        var magnitudes = [Float](repeating: 0, count: sampleAmount)
        // 1. Nested loops to safely access all data
        realIn.withUnsafeMutableBufferPointer { realInPtr in
            imagIn.withUnsafeMutableBufferPointer { imagInPtr in
                realOut.withUnsafeMutableBufferPointer { realOutPtr in
                    imagOut.withUnsafeMutableBufferPointer { imagOutPtr in
                        // 2. Execute the Discrete Fourier Transform (DFT)
                        vDSP_DFT_Execute(setup, realInPtr.baseAddress!, imagInPtr.baseAddress!, realOutPtr.baseAddress!, imagOutPtr.baseAddress!)

                        // 3. Hold the DFT output
                        var complex = DSPSplitComplex(realp: realOutPtr.baseAddress!, imagp: imagOutPtr.baseAddress!)
                        // 4. Compute and save the magnitude of each frequency component
                        vDSP_zvabs(&complex, 1, &magnitudes, 1, UInt(sampleAmount))
                    }
                }
            }
        }

        return magnitudes.map { min($0, magnitudeLimit) }
    }
}
