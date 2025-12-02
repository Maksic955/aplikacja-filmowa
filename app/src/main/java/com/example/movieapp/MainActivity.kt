package com.example.movieapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.movieapp.R
import com.example.movieapp.adapters.MovieAdapter
import com.example.movieapp.api.RetrofitClient
import com.example.movieapp.models.Movie
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var movieAdapter: MovieAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerViewMovies)
        recyclerView.layoutManager = LinearLayoutManager(this)

        movieAdapter = MovieAdapter(emptyList()) { movie ->
            // Kliknięcie w film - później dodamy szczegóły
            val intent = Intent(this, MovieDetailActivity::class.java)
            intent.putExtra("movie_id", movie.id)
            intent.putExtra("movie_title", movie.title)
            intent.putExtra("movie_overview", movie.overview)
            intent.putExtra("movie_poster", movie.posterPath)
            intent.putExtra("movie_rating", movie.voteAverage)
            startActivity(intent)
        }

        recyclerView.adapter = movieAdapter

        loadMovies()
    }

    private fun loadMovies() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getPopularMovies(RetrofitClient.API_KEY)
                movieAdapter.updateMovies(response.results)
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Błąd ładowania filmów: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}