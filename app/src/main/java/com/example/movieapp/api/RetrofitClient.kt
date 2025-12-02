package com.example.movieapp.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://api.themoviedb.org/3/"
    const val API_KEY = "7e003fc265441da2178339519c705677"
    const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: TMDBApiService by lazy {
        retrofit.create(TMDBApiService::class.java)
    }
}