package com.tagaev.weatherappcmp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform