package com.tagaev.data.models.forecast_domain

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable
@Serializable
data class MainForecast(
    var city: String,
    var coordinates: Coordinates,
    var currentForecast: CurrentForecast,

    var sunrise: LocalTime,
    var sunset: LocalTime,

    var listOfDaysForecast: List<CurrentForecast>
)
@Serializable
data class Coordinates(
    var lat: Float,
    var lon: Float,
)
@Serializable
data class CurrentForecast(
    var dateTime: LocalDateTime,
    var currentTemp: Float,
    var currentWindSpeed: Float,
    var currentWindDegree: Float,
    var currentHumidity: Int,
    var currentClouds: Float,
    var currentPressure: Int,
)