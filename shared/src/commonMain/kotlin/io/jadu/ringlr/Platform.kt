package io.jadu.ringlr

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform