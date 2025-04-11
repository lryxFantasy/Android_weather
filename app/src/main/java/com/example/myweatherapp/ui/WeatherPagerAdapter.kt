package com.example.myweatherapp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myweatherapp.R
import com.example.myweatherapp.model.Forecasts
import java.text.SimpleDateFormat
import java.util.*

class WeatherPagerAdapter(private val weatherList: List<Forecasts>) : RecyclerView.Adapter<WeatherPagerAdapter.WeatherViewHolder>() {

    // 创建 ViewHolder
    inner class WeatherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cityNameTextView: TextView = itemView.findViewById(R.id.cityNameTextView)
        private val weatherImageView: ImageView = itemView.findViewById(R.id.weatherImageView)
        private val weatherDescriptionTextView: TextView = itemView.findViewById(R.id.weatherDescriptionTextView)
        private val temperatureRangeTextView: TextView = itemView.findViewById(R.id.temperatureRangeTextView)
        private val windTextView: TextView = itemView.findViewById(R.id.windTextView)
        private val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        private val futureWeatherTextView: TextView = itemView.findViewById(R.id.futureWeatherTextView) // 未来天气

        fun bind(forecast: Forecasts) {
            // 设置城市名称
            cityNameTextView.text = forecast.city

            // 显示日期
            dateTextView.text = forecast.casts[0].date

            // 显示天气描述
            weatherDescriptionTextView.text = forecast.casts[0].dayweather

            // 显示温度范围（最低温度 ~ 最高温度）
            temperatureRangeTextView.text = "${forecast.casts[0].nighttemp}°C —— ${forecast.casts[0].daytemp}°C"

            // 显示风级信息
            windTextView.text = "${forecast.casts[0].daywind}风   ${forecast.casts[0].daypower}级\n\n"

            // 显示未来五天的天气信息（合并日期、天气、温度、风力），并换行显示每一天
            val simpleDateFormat = SimpleDateFormat("MM-dd", Locale.getDefault()) // 设置日期格式为 月-日
            val futureWeather = forecast.casts.drop(1).take(3).joinToString("\n") {  // 只取未来三天
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.date)
                val formattedDate = simpleDateFormat.format(date)

                // 使用 String.format 对齐
                String.format(
                    "%-8s%-6s%3s°C—%3s°C    %-6s%s",
                    formattedDate,       // 日期
                    it.dayweather,       // 天气
                    it.nighttemp,        // 夜间温度
                    it.daytemp,          // 白天温度
                    "${it.daypower}级", // 风力
                    "${it.daywind}风"           // 风向
                )
            }


            futureWeatherTextView.text = futureWeather

            // 根据天气类型设置天气图标
            when {
                forecast.casts[0].dayweather.contains("晴") -> {
                    weatherImageView.setImageResource(R.drawable.sun)
                }
                forecast.casts[0].dayweather.contains("云") -> {
                    weatherImageView.setImageResource(R.drawable.cloud)
                }
                forecast.casts[0].dayweather.contains("雨") -> {
                    weatherImageView.setImageResource(R.drawable.rain)
                }
                else -> {
                    weatherImageView.setImageResource(R.drawable.cloud)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_weather, parent, false)
        return WeatherViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeatherViewHolder, position: Int) {
        holder.bind(weatherList[position])
    }

    override fun getItemCount(): Int {
        return weatherList.size
    }
}
