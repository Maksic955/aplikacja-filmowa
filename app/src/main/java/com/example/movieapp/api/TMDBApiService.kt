package com.example.movieapp.api

import com.example.movieapp.models.MovieResponse
import com.example.movieapp.models.MovieImages
import com.example.movieapp.models.GenreResponse
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Path

interface TMDBApiService {

    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "pl-PL",
        @Query("page") page: Int = 1
    ): MovieResponse

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("language") language: String = "pl-PL",
        @Query("page") page: Int = 1
    ): MovieResponse

    @GET("movie/{movie_id}/images")
    suspend fun getMovieImages(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String
    ): MovieImages

    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "pl-PL",
        @Query("page") page: Int = 1
    ): MovieResponse

    @GET("movie/now_playing")
    suspend fun getNewestMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "pl-PL",
        @Query("page") page: Int = 1
    ): MovieResponse

    @GET("genre/movie/list")
    suspend fun getGenres(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "pl-PL"
    ): GenreResponse

    @GET("discover/movie")
    suspend fun discoverMovies(
        @Query("api_key") apiKey: String,
        @Query("language") languageParam: String = "pl-PL",
        @Query("page") page: Int = 1,
        @Query("with_genres") genres: String? = null,
        @Query("with_original_language") originalLanguage: String? = null,
        @Query("vote_average.gte") voteAverageMin: Double? = null,
        @Query("vote_average.lte") voteAverageMax: Double? = null,
        @Query("sort_by") sortBy: String = "popularity.desc"
    ): MovieResponse
}