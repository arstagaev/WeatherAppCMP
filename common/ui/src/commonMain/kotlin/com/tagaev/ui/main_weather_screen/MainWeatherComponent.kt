package com.tagaev.ui.main_weather_screen

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.tagaev.core.repository.WeatherRepository
import com.tagaev.core.utils.RequestPolicy
import com.tagaev.data.local.AppSettings
import com.tagaev.data.mappers.toCurrentForecastList
import com.tagaev.data.models.Resource
import com.tagaev.data.models.forecast_domain.Coordinates
import com.tagaev.data.models.forecast_domain.MainForecast
import com.tagaev.data.models.response_dto.ForecastDto
import com.tagaev.data.utils.nowEpochMs
import com.tagaev.data.utils.toLocalTimeAmPm
import com.tagaev.ui.root.IRootComponent
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format
import kotlinx.datetime.format.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.ExperimentalTime


interface IMainWeatherComponent {
    val resource: StateFlow<Resource<MainForecast>>
    val forecast: SharedFlow<MainForecast>
    val alertMessage: StateFlow<String?>
    fun dismissAlert()

    fun onChangeCoordinates(lat: Float, lon: Float)
    fun onRefreshForecast()
}

class MainWeatherComponent(
    componentContext: ComponentContext,
    private val appSettings: AppSettings,
) : IMainWeatherComponent, ComponentContext by componentContext, KoinComponent {

    private val appScope: CoroutineScope by inject()
    private val repo: WeatherRepository by inject()
//    private val nav = StackNavigation<IRootComponent.Config>()

    private val _resource = MutableStateFlow<Resource<MainForecast>>(Resource.Loading)
    override val resource: StateFlow<Resource<MainForecast>> = _resource

    private val _forecast = MutableSharedFlow<MainForecast>(extraBufferCapacity = 1)
    override val forecast: SharedFlow<MainForecast> = _forecast

    private val _alertMessage = MutableStateFlow<String?>(null)
    override val alertMessage: StateFlow<String?> = _alertMessage
    override fun dismissAlert() { _alertMessage.value = null }

    private var lastManualMs: Long = 0L
    private var lastGoodForecast: MainForecast? = null

    init {
        // 1) Hydrate from persisted cache if present (instant UI)
        appScope.launch {
            appSettings.loadLastForecastDto()?.let { cachedDto ->
                val domain = cachedDto.toDomain()
                lastGoodForecast = domain
                _forecast.tryEmit(domain)
                _resource.value = Resource.Success(domain)
            }
        }
        // 2) Then try to refresh from network
        appScope.launch { onRefreshForecast() }
    }

    @OptIn(ExperimentalTime::class)
    override fun onRefreshForecast() {
        appScope.launch {
            // If we already show data, keep it and show a small refresh indicator in the screen (UI side)
            if (_resource.value !is Resource.Success) {
                _resource.value = Resource.Loading
            }

            // UI-side debounce to avoid spamming the API
            val now = nowEpochMs()
            if (_resource.value is Resource.Success && now - lastManualMs < RequestPolicy.MIN_REFRESH_INTERVAL_MS) {
                return@launch
            }
            lastManualMs = now

            val coords = appSettings.loadCoordinates() ?: Coordinates(55.7569f, 37.6151f)

            when (val res = repo.getWeather(coords)) {
                is Resource.Success -> {
                    println("Load data success ${res.data}")
                    val domain = res.data.toDomain()
                    lastGoodForecast = domain
                    _forecast.tryEmit(domain)
                    _resource.value = Resource.Success(domain)
                    appSettings.saveLastForecastDto(res.data, nowEpochMs())
                }
                is Resource.Error -> {
                    println("Load data error ${res.exception}")
                    // Prefer previously cached content (process-memory) if present
                    val cached = lastGoodForecast

                    // Prepare a helpful message (offline vs. other) and, if we have a cache, say we're showing it
                    val savedAtStr: String? = runCatching {
                        appSettings.loadLastForecastSavedAt()?.let { ms ->
                            val dt = Instant.fromEpochMilliseconds(ms).toLocalDateTime(TimeZone.currentSystemDefault())
                            // HH:mm local time
                            dt.time.format(LocalTime.Format { hour(); char(':'); minute() })
                        }
                    }.getOrNull()

                    val offline = res.exception.isOfflineLike()
                    _alertMessage.value = when {
                        cached != null && offline && savedAtStr != null ->
                            "No internet connection. Showing cached data from $savedAtStr."
                        cached != null && offline ->
                            "No internet connection. Showing cached data."
                        cached != null && savedAtStr != null ->
                            "Couldn't refresh weather. Showing cached data from $savedAtStr."
                        cached != null ->
                            "Couldn't refresh weather. Showing cached data."
                        offline ->
                            "No internet connection. Check your network and try again."
                        else ->
                            res.causes ?: res.exception?.message ?: "Couldn't refresh weather right now."
                    }
                    println("Load data error _alertMessage:${_alertMessage.value}")
                    if (cached != null) {
                        // Keep UI on the last good snapshot
                        _forecast.tryEmit(cached)
                        _resource.value = Resource.Success(cached)
                    } else {
                        // Keep existing content if any; otherwise surface the error
                        if (_resource.value !is Resource.Success) {
                            _resource.value = Resource.Error(res.exception, res.causes)
                        }
                    }
                }
                Resource.Loading -> {
                    _resource.value = Resource.Loading
                }
            }
        }
    }

    override fun onChangeCoordinates(lat: Float, lon: Float) {
        // Save into settings (adjust method name if yours differs)
        appSettings.saveCoordinates(Coordinates(lat, lon))
        // Optional: clear manual debounce so we refresh immediately
        lastManualMs = 0L
        onRefreshForecast()
    }
}

// --- Temp mapper: DTO -> Domain (keep here for now; move to a shared mappers module later) ---
@OptIn(ExperimentalTime::class)
private fun ForecastDto.toDomain(): MainForecast {
    val coord = Coordinates(
        lat = this.location.lat.toFloat(),
        lon = this.location.lon.toFloat()
    )
    val now = Instant.fromEpochMilliseconds(nowEpochMs()).toLocalDateTime(TimeZone.currentSystemDefault())
    return MainForecast(
        city = this.location.name,
        coordinates = coord,
        currentForecast = com.tagaev.data.models.forecast_domain.CurrentForecast(
            dateTime = now,
            currentTemp = this.current.tempC.toFloat(),
            currentWindSpeed = this.current.windKph.toFloat(),
            currentWindDegree = this.current.windDegree.toFloat(),
            currentHumidity = this.current.humidity,
            currentClouds = this.current.cloud.toFloat(),
            currentPressure = this.current.pressureIn.toInt()
        ),
        sunrise = this.forecast.forecastday[0].astro.sunrise.toLocalTimeAmPm(),
        sunset  = this.forecast.forecastday[0].astro.sunset.toLocalTimeAmPm(),
        listOfDaysForecast = this.forecast.forecastday.toCurrentForecastList()
    )
}

private fun Throwable?.isOfflineLike(): Boolean {
    val t = this ?: return false
    return t is ConnectTimeoutException ||
           t is SocketTimeoutException ||
           t is HttpRequestTimeoutException ||
           t is UnresolvedAddressException ||
           (t.cause?.isOfflineLike() ?: false)
}
