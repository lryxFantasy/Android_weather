package com.example.myweatherapp.model

data class Casts(
    val date: String,
    val dayweather: String,
    val nightweather: String,
    val daytemp: Int,
    val nighttemp: Int,
    val daywind: String,
    val daypower: String,
    val daytemp_float: Float,
    val nighttemp_float: Float
)
