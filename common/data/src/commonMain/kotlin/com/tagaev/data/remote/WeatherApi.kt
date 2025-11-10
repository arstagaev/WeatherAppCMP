package com.tagaev.data.remote

import com.tagaev.data.models.Resource
import com.tagaev.data.models.forecast_domain.Coordinates
import com.tagaev.data.models.forecast_domain.MainForecast
import com.tagaev.data.models.response_dto.ForecastDto
import com.tagaev.secrets.Secrets
import com.tagaev.utils.Log
import com.tagaev.utils.cleanJsonStart
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json

data class ApiConfig(
    val baseUrl: String = "https://api.weatherapi.com/v1/forecast.json",
    var token: String = Secrets.WEATHER_API_KEY
)

class WeatherApi(
    private val client: HttpClient = HttpClientFactory.create()
) {
    suspend fun getWeather(
        api: ApiConfig,
        coordinates: Coordinates = Coordinates(lat = 55.7569f, lon = 37.6151f)
    ): Resource<ForecastDto> {
        return try {
            val url = api.baseUrl
            val response = client.get(url) {
                url {
                    parameters.append("key", api.token)
//                    parameters.append("q", "${coordinates.lat},${coordinates.lon}") // 55.7569,37.6151
                    parameters.append("q", "55.7569,37.6151") // 55.7569,37.6151
                    parameters.append("days", "3")
                }
            }
            if (!response.status.isSuccess()) {
                // read body for diagnostics, but don’t crash if not text
                val errBody = runCatching { response.bodyAsText().take(2000) }.getOrNull()
                return Resource.Error(
                    exception = null,
                    causes = "HTTP ${response.status.value} ${response.status.description}" +
                            (if (errBody.isNullOrBlank()) "" else " | $errBody")
                )
            }

            val raw = response.bodyAsText().cleanJsonStart()
            Log.info("get weather raw:${raw.length}")
            val items = json.decodeFromString<ForecastDto>(raw)

            Resource.Success(items)
        } catch (e: RedirectResponseException) { // 3xx with body
            Resource.Error(e, "Redirect error: ${e.response.status}")
        } catch (e: ClientRequestException) {     // 4xx
            val body = runCatching { e.response.bodyAsText().take(2000) }.getOrNull()
            Resource.Error(e, "Client error ${e.response.status}: ${body ?: e.message}")
        } catch (e: ServerResponseException) {    // 5xx
            val body = runCatching { e.response.bodyAsText().take(2000) }.getOrNull()
            Resource.Error(e, "Server error ${e.response.status}: ${body ?: e.message}")
        } catch (e: Exception) {
            Resource.Error(e, "Unexpected error: ${e.message}")
        }
    }

    companion object {
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            explicitNulls = false
        }
    }
}