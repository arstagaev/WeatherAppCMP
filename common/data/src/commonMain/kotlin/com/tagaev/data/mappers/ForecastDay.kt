package com.tagaev.data.mappers

import com.tagaev.data.models.forecast_domain.CurrentForecast
import com.tagaev.data.models.response_dto.ForecastDayList
import com.tagaev.data.models.response_dto.Forecastday
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toLocalDateTime
import kotlin.math.roundToInt
import kotlin.time.ExperimentalTime

private const val KPH_TO_MS = 1f / 3.6f

// Main entry points
fun ForecastDayList.toCurrentForecastList(): List<CurrentForecast> =
    forecastday.toCurrentForecastList()

fun List<Forecastday>.toCurrentForecastList(): List<CurrentForecast> =
    flatMap { it.toCurrentForecastList() }

@OptIn(ExperimentalTime::class)
fun Forecastday.toCurrentForecastList(): List<CurrentForecast> {
    // If hourly data exists, map each hour. If not, fall back to a single point from daily averages.
    val hourly = hour
    return if (hourly.isNotEmpty()) {
        hourly.map { h ->
            CurrentForecast(
                dateTime = Instant.fromEpochSeconds(h.timeEpoch.toLong())
                    .toLocalDateTime(TimeZone.currentSystemDefault()),
                currentTemp = h.tempC.toFloat(),
                currentWindSpeed = (h.windKph.toFloat() * KPH_TO_MS),
                currentWindDegree = h.windDegree.toFloat(),
                currentHumidity = h.humidity,
                currentClouds = h.cloud.toFloat(),
                currentPressure = h.pressureMb.roundToInt(), // mb ≈ hPa
            )
        }
    } else {
        // Fallback to daily “avg” snapshot (no exact time in ‘day’, pick local noon)
        val noonLocal = Instant.fromEpochSeconds(dateEpoch.toLong())
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date // keep the same day, set noon
            .atTime(hour = 12, minute = 0)

        listOf(
            CurrentForecast(
                dateTime = noonLocal,
                currentTemp = day.avgtempC.toFloat(),
                currentWindSpeed = (day.maxwindKph.toFloat() * KPH_TO_MS), // best effort
                currentWindDegree = 0f,         // unknown at daily level
                currentHumidity = day.avghumidity,
                currentClouds = day.dailyChanceOfRain.toFloat(),             // unknown at daily level
                currentPressure = 0             // unknown at daily level
            )
        )
    }
}