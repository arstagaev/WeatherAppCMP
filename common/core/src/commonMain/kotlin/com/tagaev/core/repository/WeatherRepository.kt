package com.tagaev.core.repository

import com.tagaev.core.utils.RequestPolicy
import com.tagaev.data.models.Resource
import com.tagaev.data.models.forecast_domain.Coordinates
import com.tagaev.data.remote.ApiConfig
import com.tagaev.data.remote.WeatherApi
import com.tagaev.data.local.AppSettings
import com.tagaev.data.models.response_dto.ForecastDto
import com.tagaev.data.utils.nowEpochMs
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


class WeatherRepository(
    private val api: WeatherApi,
    private val cfg: ApiConfig,
) : KoinComponent {

    // Pull settings via DI without changing constructor signature at call sites
    private val settings: AppSettings by inject()

    /**
     * Load weather using cache+policy. We keep the same method name and signature.
     *
     * Policy:
     *  1) If we have cached data and the last check was just now (cooldown not elapsed), return cache.
     *  2) Otherwise try network; on failure fall back to cache if present.
     *  3) Always mark the check moment to enforce cooldown.
     */
    suspend fun getWeather(
        coordinates: Coordinates
    ): Resource<ForecastDto> {
        val now = nowEpochMs()
        val lastChecked = settings.lastCheckedMs() ?: 0L
        val cachedDto = settings.loadWeatherForecastCache()
        val hasCache = cachedDto != null

        // Anti-spam: if user triggers refresh too fast and we have cache, serve cache
        if (hasCache && (now - lastChecked) < RequestPolicy.MIN_REFRESH_INTERVAL_MS) {
            return Resource.Success(cachedDto)
        }

        // Try network (API already returns Resource)
        val network: Resource<ForecastDto> = api.getWeather(
            api = cfg,
            coordinates = coordinates
        )

        // Always record that we attempted a refresh
        settings.markCheckedNow()

        // Return API result as-is; component handles error/cached fallback UI
        return network
    }
}
