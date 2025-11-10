package com.tagaev.core.utils


object RequestPolicy {
    const val MIN_REFRESH_INTERVAL_MS: Long = 2_000L   // manual cooldown (UI)
    const val AUTO_REFRESH_TTL_MS: Long = 10 * 60_000L  // repository freshness window
}