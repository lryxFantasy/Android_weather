package com.example.myweatherapp.model

data class WeatherModel(
    val status: Int,
    val count: Int,
    val infocode: Int,
    val forecasts: List<Forecasts> = emptyList()
)
