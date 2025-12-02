package com.example.movieapp.models

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("name")
    val cityName: String,

    @SerializedName("main")
    val main: MainWeather,

    @SerializedName("weather")
    val weather: List<WeatherDescription>,

    @SerializedName("wind")
    val wind: Wind,

    @SerializedName("coord")
    val coord: Coordinates
)

data class MainWeather(
    @SerializedName("temp")
    val temp: Double,

    @SerializedName("feels_like")
    val feelsLike: Double,

    @SerializedName("humidity")
    val humidity: Int,

    @SerializedName("pressure")
    val pressure: Int
)

data class WeatherDescription(
    @SerializedName("description")
    val description: String,

    @SerializedName("icon")
    val icon: String
)

data class Wind(
    @SerializedName("speed")
    val speed: Double
)

data class Coordinates(
    @SerializedName("lon")
    val lon: Double,

    @SerializedName("lat")
    val lat: Double
)