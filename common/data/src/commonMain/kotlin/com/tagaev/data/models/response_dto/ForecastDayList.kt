package com.tagaev.data.models.response_dto


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ForecastDayList(
    @SerialName("forecastday")
    val forecastday: List<Forecastday>
)