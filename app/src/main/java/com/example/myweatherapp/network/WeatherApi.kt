package com.example.myweatherapp.network

import com.example.myweatherapp.model.WeatherModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object WeatherApi {
    private const val BASE_URL = "https://restapi.amap.com/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val weatherApiService: WeatherApiService = retrofit.create(WeatherApiService::class.java)

    suspend fun getWeather(cityCode: Int): WeatherModel {
        return weatherApiService.getWeather(cityCode)
    }
}
