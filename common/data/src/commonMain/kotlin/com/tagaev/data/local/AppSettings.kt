package com.tagaev.data.local

import com.russhwolf.settings.Settings
import com.russhwolf.settings.serialization.decodeValueOrNull
import com.russhwolf.settings.serialization.encodeValue
import com.russhwolf.settings.serialization.removeValue
import com.tagaev.data.models.response_dto.ForecastDto
import com.tagaev.data.models.forecast_domain.Coordinates
import com.russhwolf.settings.set
import com.russhwolf.settings.get
import com.russhwolf.settings.ExperimentalSettingsApi
import com.tagaev.data.utils.nowEpochMs

object PrefKeys {
    // Weather cache & validators
    const val WEATHER_CACHE = "weather.cache.v1"           // new key (keeps old EVENTS_CACHE as fallback)
    const val CACHE_ETAG = "weather.cache.etag"
    const val CACHE_LAST_MOD = "weather.cache.lastModified"
    const val CACHE_LAST_UPDATED_MS = "weather.cache.lastUpdatedMs"
    const val CACHE_LAST_CHECKED_MS = "weather.cache.lastCheckedMs"

    // Coordinates
    const val COORD_LAT = "coord.lat"
    const val COORD_LON = "coord.lon"

}

// -------------------- Simple UI state holders (kept) --------------------

class AppSettings(
    private val settings: Settings,
) {
    // -------------------- Coordinates --------------------
    fun saveCoordinates(c: Coordinates) {
        settings.putFloat(PrefKeys.COORD_LAT, c.lat)
        settings.putFloat(PrefKeys.COORD_LON, c.lon)
    }

    fun loadCoordinates(): Coordinates? {
        val lat = settings.getFloatOrNull(PrefKeys.COORD_LAT) ?: return null
        val lon = settings.getFloatOrNull(PrefKeys.COORD_LON) ?: return null
        return Coordinates(lat, lon)
    }

    // -------------------- Weather cache blob --------------------
    @Deprecated("Use saveLastForecastDto(dto, savedAtMs) instead")
    @OptIn(ExperimentalSettingsApi::class)
    fun saveWeatherForecast(forecast: ForecastDto) {
        settings.encodeValue(
            serializer = ForecastDto.serializer(),
            key = PrefKeys.WEATHER_CACHE,
            value = forecast
        )
        settings.putLong(PrefKeys.CACHE_LAST_UPDATED_MS, nowEpochMs())
    }

    @OptIn(ExperimentalSettingsApi::class)
    fun saveLastForecastDto(dto: ForecastDto, savedAtMs: Long = nowEpochMs()) {
        // Alias to saveWeatherForecast with explicit timestamp
        settings.encodeValue(
            serializer = ForecastDto.serializer(),
            key = PrefKeys.WEATHER_CACHE,
            value = dto
        )
        settings.putLong(PrefKeys.CACHE_LAST_UPDATED_MS, savedAtMs)
    }

    @Deprecated("Use loadLastForecastDto() instead")
    @OptIn(ExperimentalSettingsApi::class)
    fun loadWeatherForecastCache(): ForecastDto? {
        // Prefer new key; fallback to legacy key if present
        return settings.decodeValueOrNull(serializer = ForecastDto.serializer(), key = PrefKeys.WEATHER_CACHE)
    }

    @OptIn(ExperimentalSettingsApi::class)
    fun loadLastForecastDto(): ForecastDto? {
        // Alias to loadWeatherForecastCache
        return settings.decodeValueOrNull(
            serializer = ForecastDto.serializer(),
            key = PrefKeys.WEATHER_CACHE
        )
    }

    @OptIn(ExperimentalSettingsApi::class)
    fun clearWeatherForecastCache() {
        settings.removeValue(ForecastDto.serializer(), PrefKeys.WEATHER_CACHE)
        settings.remove(PrefKeys.CACHE_ETAG)
        settings.remove(PrefKeys.CACHE_LAST_MOD)
        settings.remove(PrefKeys.CACHE_LAST_UPDATED_MS)
        settings.remove(PrefKeys.CACHE_LAST_CHECKED_MS)
    }

    // -------------------- HTTP cache validators & timestamps --------------------
    fun saveEtag(etag: String?) {
        if (etag == null) settings.remove(PrefKeys.CACHE_ETAG) else settings.putString(PrefKeys.CACHE_ETAG, etag)
    }

    fun saveLastModified(value: String?) {
        if (value == null) settings.remove(PrefKeys.CACHE_LAST_MOD) else settings.putString(PrefKeys.CACHE_LAST_MOD, value)
    }

    fun loadEtag(): String? = settings.getStringOrNull(PrefKeys.CACHE_ETAG)
    fun loadLastModified(): String? = settings.getStringOrNull(PrefKeys.CACHE_LAST_MOD)

    fun markCheckedNow() {
        settings.putLong(PrefKeys.CACHE_LAST_CHECKED_MS, nowEpochMs())
    }

    fun lastUpdatedMs(): Long? = settings.getLongOrNull(PrefKeys.CACHE_LAST_UPDATED_MS)

    // Alias used by components to read when the last forecast snapshot was persisted
    fun loadLastForecastSavedAt(): Long? = lastUpdatedMs()

    fun lastCheckedMs(): Long? = settings.getLongOrNull(PrefKeys.CACHE_LAST_CHECKED_MS)

    // Ages (ms) relative to `nowEpochMs()`; useful for TTL checks
    fun lastUpdatedAgeMs(now: Long = nowEpochMs()): Long? =
        lastUpdatedMs()?.let { now - it }

    fun lastCheckedAgeMs(now: Long = nowEpochMs()): Long? =
        lastCheckedMs()?.let { now - it }

    /** Returns true if there is no cache or the cache is older than [ttlMs]. */
    fun isCacheStale(ttlMs: Long, now: Long = nowEpochMs()): Boolean =
        lastUpdatedAgeMs(now)?.let { it > ttlMs } ?: true

    // -------------------- Generic helpers --------------------
    fun getBool(key: String, defaultValue: Boolean) = settings.getBoolean(key = key, defaultValue = defaultValue)
    fun setBool(key: String, newValue: Boolean) = settings.putBoolean(key = key, value = newValue)

    fun getStringOrNull(key: String) = settings.getStringOrNull(key = key)
    fun getString(key: String, defaultValue: String) = settings.getString(key = key, defaultValue = defaultValue)
    fun setString(key: String, newValue: String) = settings.putString(key = key, value = newValue)

    fun getInt(key: String, defaultValue: Int) = settings.getInt(key, defaultValue)
    fun setInt(key: String, value: Int) = settings.putInt(key, value)

    fun getLongOrNull(key: String): Long? = settings.getLongOrNull(key)
    fun setLong(key: String, value: Long) = settings.putLong(key, value)

    fun getFloatOrNull(key: String): Float? = settings.getFloatOrNull(key)
    fun setFloat(key: String, value: Float) = settings.putFloat(key, value)
}

// -------------------- local extensions --------------------
//private fun Settings.getStringOrNull(key: String): String? = if (hasKey(key)) getString(key) else null
//private fun Settings.getLongOrNull(key: String): Long? = if (hasKey(key)) getLong(key) else null
//private fun Settings.getFloatOrNull(key: String): Float? = if (hasKey(key)) getFloat(key) else null