package org.kmm.core.helpers

import platform.Foundation.NSLog
import kotlin.time.Clock

actual object PlatformLog {
    actual fun e(tag: String, message: String, throwable: Throwable?) {
        val now = Clock.System.now().nanosecondsOfSecond

        if (throwable != null) {
            NSLog("ERROR: [$tag|$now] $message. Throwable: $throwable CAUSE ${throwable.cause}")
        } else {
            NSLog("ERROR: [$tag|$now] $message")
        }
    }

    actual fun d(tag: String, message: String) {
        val now = Clock.System.now().nanosecondsOfSecond
        NSLog("DEBUG: [$tag|$now] $message")
    }

    actual fun i(tag: String, message: String) {
        val now = Clock.System.now().nanosecondsOfSecond
        NSLog("INFO: [$tag|$now] $message")
    }
}