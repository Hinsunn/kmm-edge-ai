package org.hinsun.edge.core.platforms

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

//
// File        : PlatformPath.ios.kt
// Author      : Tran Van Hoai
// Date        : 10/23/26
//

actual class PlatformPath {
    @OptIn(ExperimentalForeignApi::class)
    actual val modelsDir: String
        get() {
            val fm = NSFileManager.defaultManager
            val base = fm.URLForDirectory(
                NSApplicationSupportDirectory,
                NSUserDomainMask,
                null,
                true,
                null
            )
            val path = requireNotNull(base?.path) + "/Models"
            fm.createDirectoryAtPath(path, true, null, null)
            return path
        }
}
