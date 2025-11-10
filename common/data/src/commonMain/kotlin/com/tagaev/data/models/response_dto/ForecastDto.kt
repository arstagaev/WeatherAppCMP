package com.tagaev.data.models.response_dto


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ForecastDto(
    @SerialName("current")
    val current: Current,
    @SerialName("forecast")
    val forecast: ForecastDayList,
    @SerialName("location")
    val location: Location
)