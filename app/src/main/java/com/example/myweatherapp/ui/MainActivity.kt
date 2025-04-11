package com.example.myweatherapp.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.myweatherapp.R
import com.example.myweatherapp.model.Forecasts
import com.example.myweatherapp.network.WeatherApi
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient // 定位服务

    private lateinit var rootLayout: LinearLayout // 根布局
    private lateinit var btnSwitchSkin: Button // 换肤按钮
    private lateinit var btnAddCity: Button // 添加城市按钮
    private lateinit var delAddCity: Button // 删除城市按钮
    private lateinit var aboutCity: Button // 关于按钮
    private lateinit var viewPager: ViewPager2 // 用来显示城市的 ViewPager2
    private lateinit var txtCurrentCity: TextView // 当前城市 TextView
    private val weatherList = mutableListOf<Forecasts>() // 存储天气数据的列表
    private val cities = mapOf(
        "厦门市" to 350200, "深圳市" to 440300, "北京市" to 110000, "上海市" to 310000,
        "石家庄市" to 130100, "太原市" to 140100, "沈阳市" to 210100, "长春市" to 220100,
        "哈尔滨市" to 230100, "南京市" to 320100, "杭州市" to 330100, "合肥市" to 340100,
        "福州市" to 350100, "南昌市" to 360100, "济南市" to 370100, "郑州市" to 410100,
        "武汉市" to 420100, "长沙市" to 430100, "广州市" to 440100, "南宁市" to 450100,
        "海口市" to 460100, "成都市" to 510100, "贵阳市" to 520100, "昆明市" to 530100,
        "拉萨市" to 540100, "西安市" to 610100, "兰州市" to 620100, "西宁市" to 630100,
        "银川市" to 640100, "乌鲁木齐市" to 650100
    )

    // 换肤背景资源
    private val themeBackgrounds = listOf(
        R.drawable.theme_1_background, // 第一个主题背景
        R.drawable.theme_3_background,  // 第二个主题背景
        R.drawable.theme_2_background  // 第二个主题背景

    )
    private var currentThemeIndex = 0 // 当前主题索引

    // 根据当前位置自动添加城市
    private fun addCityByLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            // 请求权限
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                100
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            // 如果无法获取定位信息或其他错误，默认设为厦门市
            val defaultCity = "厦门市"
            val defaultCityCode = cities[defaultCity]

            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude
                println("Latitude: $latitude")
                println("Longitude: $longitude")

                // 使用协程异步获取地址
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        val geocoder = Geocoder(this@MainActivity)
                        val addresses = geocoder.getFromLocation(latitude, longitude, 1)

                        withContext(Dispatchers.Main) {
                            if (addresses != null && addresses.isNotEmpty()) {
                                val cityName = addresses[0].locality // 获取城市名称
                                val cityCode = cities[cityName] // 根据城市名称获取城市代码

                                if (cityCode != null) {
                                    addCity(cityCode) // 自动添加城市
                                    Toast.makeText(this@MainActivity, "当前位置城市已添加：$cityName", Toast.LENGTH_SHORT).show()
                                } else {
                                    // 如果城市不支持，默认使用厦门市
                                    addCity(defaultCityCode ?: 0)
                                    Toast.makeText(this@MainActivity, "当前位置城市未被支持，已自动设置为厦门市", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                // 无法获取城市，默认使用厦门市
                                addCity(defaultCityCode ?: 0)
                                Toast.makeText(this@MainActivity, "无法获取当前位置，已自动设置为厦门市", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: IOException) {
                        // 网络异常或其他错误，默认设置为厦门市
                        withContext(Dispatchers.Main) {
                            addCity(defaultCityCode ?: 0)
                            Toast.makeText(this@MainActivity, "获取位置失败，请检查网络连接，已自动设置为厦门市", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                // 如果无法获取定位信息，默认设置为厦门市
                if (defaultCityCode != null) {
                    addCity(defaultCityCode) // 自动添加厦门市
                    Toast.makeText(this, "无法获取当前位置，请检查定位权限或设置，已自动设置为厦门市", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "厦门市未被支持", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }




    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            addCityByLocation() // 如果权限被授予，重新尝试获取定位
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // 初始化定位客户端
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // 获取当前位置并添加城市
        addCityByLocation()
        // 初始化布局元素
        rootLayout = findViewById(R.id.root_layout)
        btnSwitchSkin = findViewById(R.id.btn_switch_skin)//换肤按钮
        btnAddCity = findViewById(R.id.btn_add_city) // 添加城市按钮
        delAddCity = findViewById(R.id.btn_delete_city) // 删除城市按钮
        aboutCity = findViewById(R.id.btn_about)//关于按钮
        viewPager = findViewById(R.id.viewPager)
        txtCurrentCity = findViewById(R.id.txt_current_city) // 获取当前城市的 TextView

        // 设置换肤按钮点击事件
        btnSwitchSkin.setOnClickListener {
            changeTheme()
        }
        aboutCity.setOnClickListener {
            showCityCode()
        }
        // 删除按钮点击事件
        delAddCity.setOnClickListener {
            if (weatherList.isEmpty()) {
                // 当城市列表为空时，提示用户无城市可删除
                Toast.makeText(this, "当前没有城市可以删除，请先添加城市！", Toast.LENGTH_SHORT).show()
            } else {
                // 获取当前 ViewPager2 的位置
                val currentPosition = viewPager.currentItem
                val forecast = weatherList[currentPosition] // 获取当前天气对象
                removeCity(forecast.city) // 删除城市
            }
        }


        // 添加“添加城市”按钮点击事件
        btnAddCity.setOnClickListener {
            showSelectCityDialog()
        }


        // 获取天气数据
        //fetchWeatherData(listOf(350200, 110000)) // 厦门市、北京市的城市代码

        // 监听 ViewPager2 滑动事件，更新当前城市
        // 在 ViewPager2 注册的页面切换回调中，根据天气动态切换主题
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateCurrentCity(weatherList[position].city)


                // 根据天气类型动态切换主题
                val forecast = weatherList[position]
                when {
                    forecast.casts[0].dayweather.contains("晴") -> changeTheme(0) // 晴天主题
                    forecast.casts[0].dayweather.contains("云") -> changeTheme(2) // 多云主题
                    forecast.casts[0].dayweather.contains("雨") -> changeTheme(1) // 雨天主题
                    else -> changeTheme(2) // 默认主题
                }
            }
        })

    }

    private fun fetchWeatherData(cityCodes: List<Int>) {
        lifecycleScope.launch {
            try {
                // 获取天气数据
                val results = withContext(Dispatchers.IO) {
                    cityCodes.map { WeatherApi.getWeather(it) }
                }
                weatherList.clear()
                weatherList.addAll(results.flatMap { it.forecasts })

                // 更新当前城市为第一个城市
                updateCurrentCity(weatherList.first().city)

                // 设置 ViewPager2 的适配器
                viewPager.adapter = WeatherPagerAdapter(weatherList)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 更新当前城市方法
    fun updateCurrentCity(city: String) {
        txtCurrentCity.text = city
    }

    // 切换主题的方法
    fun changeTheme(themeIndex: Int? = null) {
        // 如果有输入的主题索引，则使用该索引的主题；否则执行默认切换逻辑
        currentThemeIndex = themeIndex ?: ((currentThemeIndex + 1) % themeBackgrounds.size)

        // 设置新的背景
        rootLayout.setBackgroundResource(themeBackgrounds[currentThemeIndex])
    }


    // 根据 cityCode 获取 cityName
    private fun getCityNameByCode(cityCode: Int): String? {
        return cities.entries.firstOrNull { it.value == cityCode }?.key
    }

    // 显示所有城市及城市代码
    private fun showCityCode() {
        if (weatherList.isEmpty()) {
            Toast.makeText(this, "当前没有任何城市！", Toast.LENGTH_SHORT).show()
            return
        }

        // 构建展示内容
        val cityInfo = weatherList.joinToString(separator = "\n") { forecast ->
            val cityCode = cities[forecast.city] ?: "未知代码"
            "${forecast.city}: $cityCode"
        }

        // 使用 AlertDialog 显示
        AlertDialog.Builder(this)
            .setTitle("已添加的城市及城市代码")
            .setMessage(cityInfo)
            .setPositiveButton("确定", null)
            .show()
    }

    // 显示选择城市选框
    private fun showSelectCityDialog() {

        val cityNames = cities.keys.toTypedArray() // 城市名称数组

        AlertDialog.Builder(this)
            .setTitle("选择一个城市添加")
            .setItems(cityNames) { _, which ->
                val selectedCity = cityNames[which]
                val cityCode = cities[selectedCity]
                if (cityCode != null) {
                    addCity(cityCode)
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    // 添加城市的方法
    private fun addCity(cityCode: Int) {
        // 检查是否已经添加过该城市
        if (weatherList.any { it.city == getCityNameByCode(cityCode) }) {
            Toast.makeText(this, "该城市已经添加过！", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    WeatherApi.getWeather(cityCode)
                }

                if (result.forecasts.isEmpty()) {
                    Toast.makeText(this@MainActivity, "无法获取该城市天气数据！", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                weatherList.addAll(result.forecasts)
                viewPager.adapter?.notifyDataSetChanged() // 通知适配器刷新数据
                Toast.makeText(this@MainActivity, "城市添加成功！", Toast.LENGTH_SHORT).show()
                viewPager.adapter = WeatherPagerAdapter(weatherList)

            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "无法获取该城市天气数据！", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 删除城市的方法
    @SuppressLint("NotifyDataSetChanged")
    private fun removeCity(cityName: String) {
        val updatedList = weatherList.filterNot { it.city == cityName }

        weatherList.clear()
        weatherList.addAll(updatedList)

        if (weatherList.isEmpty()) {
            viewPager.adapter = null // 清空适配器
            txtCurrentCity.text = "请添加城市"
            rootLayout.setBackgroundResource(R.drawable.theme_1_background) // 默认背景
            Toast.makeText(this, "所有城市已删除！", Toast.LENGTH_SHORT).show()
        } else {
            viewPager.adapter?.notifyDataSetChanged()
            viewPager.currentItem = 0 // 确保 ViewPager 停留在有效的页面
            updateCurrentCity(weatherList.first().city)

            // 更新主题
            val forecast = weatherList.first()
            when {
                forecast.casts[0].dayweather.contains("晴") -> changeTheme(0)
                forecast.casts[0].dayweather.contains("云") -> changeTheme(2)
                forecast.casts[0].dayweather.contains("雨") -> changeTheme(1)
                else -> changeTheme(2)
            }
        }
    }



}
