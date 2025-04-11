package com.example.myweatherapp.model

data class Forecasts(
    val city: String,
    val adcode: Int,
    val casts: List<Casts>
)
