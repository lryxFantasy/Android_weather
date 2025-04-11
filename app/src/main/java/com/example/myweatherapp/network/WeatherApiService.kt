package com.example.myweatherapp.network

import com.example.myweatherapp.model.WeatherModel
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("v3/weather/weatherInfo")
    suspend fun getWeather(
        @Query("city") cityCode: Int,
        @Query("key") apiKey: String = "adfb53596f9d9b1f37295e65cf43a388",
        @Query("extensions") extensions: String = "all"
    ): WeatherModel
}
