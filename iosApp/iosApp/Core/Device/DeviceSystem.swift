//
// File        : DeviceSystem.swift
// Author      : Tran Van Hoai
// Date        : 18/10/26
//

import Darwin
import Foundation
import Metal
import UIKit

class DeviceSystem {
    static func trace() {
        print("+" + String(repeating: "-", count: reportWidth - 4) + "+")
        printRow("IOS DEVICE SYSTEM REPORT")
        print("+" + String(repeating: "-", count: reportWidth - 4) + "+")

        printRow("Device Model          : \(deviceModel)")
        printRow("Device identifier     : \(machineIdentifier)")
        printRow("Device Name           : \(UIDevice.current.name)")
        printRow("iOS Version           : \(UIDevice.current.systemVersion)")
        printRow("CPU Architecture      : \(cpuArchitecture)")
        printRow("CPU Core Processors   : \(ProcessInfo.processInfo.processorCount)")
        printRow("Active CPU Cores      : \(ProcessInfo.processInfo.activeProcessorCount)")
        printRow("Physical Memory       : \(formattedBytes(ProcessInfo.processInfo.physicalMemory))")
        printRow("Available Memory      : \(availableMemory)")
        printRow("Process Memory        : \(processMemory)")

        gpu()

        printRow("Thermal State         : \(thermalState)")
        print("+" + String(repeating: "-", count: reportWidth - 4) + "+")
    }

    static var machineIdentifier: String {
        var systemInfo = utsname()
        uname(&systemInfo)

        return withUnsafePointer(to: &systemInfo.machine) { pointer in
            pointer.withMemoryRebound(to: CChar.self, capacity: 1) {
                String(cString: $0)
            }
        }
    }

    static var deviceModel: String {
        #if targetEnvironment(simulator)
        return "iOS Simulator (\(machineIdentifier))"
        #else
        return machineIdentifier
        #endif
    }

    static var cpuArchitecture: String {
        #if arch(arm64)
        return "ARM64"
        #elseif arch(x86_64)
        return "x86_64"
        #else
        return "Unknown"
        #endif
    }

    static var availableMemory: String {
        // Approximate free memory reported by the VM system.
        let pageSize = UInt64(vm_kernel_page_size)
        var stats = vm_statistics64()
        var count = mach_msg_type_number_t(
            MemoryLayout<vm_statistics64_data_t>.size
                / MemoryLayout<integer_t>.size
        )
        let result = withUnsafeMutablePointer(to: &stats) {
            $0.withMemoryRebound(
                to: integer_t.self,
                capacity: Int(count)
            ) {
                host_statistics64(
                    mach_host_self(),
                    HOST_VM_INFO64,
                    $0,
                    &count
                )
            }
        }

        guard result == KERN_SUCCESS else { return "Unavailable" }
        let freePages = UInt64(stats.free_count)
        let inactivePages = UInt64(stats.inactive_count)
        let speculativePages = UInt64(stats.speculative_count)

        let bytes = (freePages + inactivePages + speculativePages) * pageSize
        return formattedBytes(bytes) + " (approximately)"
    }

    static var processMemory: String {
        var info = mach_task_basic_info()
        var count = mach_msg_type_number_t(
            MemoryLayout<mach_task_basic_info_data_t>.size / MemoryLayout<natural_t>.size
        )

        let result = withUnsafeMutablePointer(to: &info) {
            $0.withMemoryRebound(
                to: integer_t.self,
                capacity: Int(count)
            ) {
                task_info(
                    mach_task_self_,
                    task_flavor_t(MACH_TASK_BASIC_INFO),
                    $0,
                    &count
                )
            }
        }

        guard result == KERN_SUCCESS else { return "Unavailable" }
        return formattedBytes(UInt64(info.resident_size))
    }

    static func gpu() {
        guard let device = MTLCreateSystemDefaultDevice() else {
            printRow("GPU Device            : Unavailable")
            return
        }

        printRow("GPU Device            : \(device.name)")
        printRow("GPU Memory            : \(formattedBytes(device.recommendedMaxWorkingSetSize))")
        #if os(macOS)
        printRow("GPU Low Power         : \(device.isLowPower ? "Yes" : "No")")
        printRow("GPU Headless          : \(device.isHeadless ? "Yes" : "No")")
        printRow("GPU Removable         : \(device.isRemovable ? "Yes" : "No")")
        #endif

        if #available(iOS 16.0, *) {
            printRow("GPU WorkingSet        : \(formattedBytes(device.recommendedMaxWorkingSetSize))")
        }

        printRow("GPU Family Support    :")
        for family in [
            MTLGPUFamily.apple1,
            .apple2, .apple3, .apple4, .apple5,
            .apple6, .apple7, .apple8, .apple9
        ] {
            if device.supportsFamily(family) {
                printRow("    \(gpuFamilyName(family))")
            }
        }
        printRow("GPU Thread Count      : Not exposed by public iOS API")
    }

    static var thermalState: String {
        switch ProcessInfo.processInfo.thermalState {
        case .nominal: return "Nominal"
        case .fair: return "Fair"
        case .serious: return "Serious"
        case .critical: return "Critical"
        @unknown default: return "Unknown"
        }
    }

    static func formattedBytes(_ bytes: UInt64) -> String {
        ByteCountFormatter.string(
            fromByteCount: Int64(bytes),
            countStyle: .memory
        )
    }

    private static let reportWidth = 80
    private static func printRow(_ content: String) {
        let innerWidth = reportWidth - 4
        let text = " \(content)"
        let truncated = String(text.prefix(innerWidth))

        let padding = String(
            repeating: " ",
            count: max(0, innerWidth - truncated.count)
        )

        print("|\(truncated)\(padding)|")
    }

    private static func gpuFamilyName(_ family: MTLGPUFamily) -> String {
        switch family {
        case .apple1: return "Apple GPU Family 1"
        case .apple2: return "Apple GPU Family 2"
        case .apple3: return "Apple GPU Family 3"
        case .apple4: return "Apple GPU Family 4"
        case .apple5: return "Apple GPU Family 5"
        case .apple6: return "Apple GPU Family 6"
        case .apple7: return "Apple GPU Family 7"
        case .apple8: return "Apple GPU Family 8"
        case .apple9: return "Apple GPU Family 9"
        default: return "Unknown GPU Family (\(family.rawValue))"
        }
    }
}
