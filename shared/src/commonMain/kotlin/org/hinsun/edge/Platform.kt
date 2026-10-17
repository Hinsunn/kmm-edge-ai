package org.hinsun.edge

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform