package org.hinsun.edge.core.platforms

import android.content.Context
import org.pytorch.executorch.Module
import org.pytorch.executorch.Tensor
import java.io.File

//
// File        : PlatformPath.android.kt
// Author      : Tran Van Hoai
// Date        : 10/23/26
//

actual class PlatformPath(private val context: Context) {
    actual val modelsDir: String
        get() = File(context.filesDir, "Models").apply { mkdirs() }.absolutePath
}
