package org.hinsun.edge.core.platforms

//
// File        : PlatformPath.kt
// Author      : Tran Van Hoai
// Date        : 10/23/26
//

// No constructor declared here, so each actual can take its own params (e.g. Context on Android)
expect class PlatformPath {
    val modelsDir: String
}
