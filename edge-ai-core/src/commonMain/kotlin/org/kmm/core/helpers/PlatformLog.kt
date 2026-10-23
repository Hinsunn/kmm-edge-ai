package org.kmm.core.helpers

//
// File        : PlatformLog.kt
// Author      : Tran Van Hoai
// Date        : 10/23/26
//

expect object PlatformLog {
    fun e(tag: String, message: String, throwable: Throwable? = null)
    fun d(tag: String, message: String)
    fun i(tag: String, message: String)
}