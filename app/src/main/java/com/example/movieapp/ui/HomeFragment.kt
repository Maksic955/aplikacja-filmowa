package com.example.movieapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.movieapp.R
import com.example.movieapp.adapters.MovieAdapter
import com.example.movieapp.api.RetrofitClient
import com.example.movieapp.models.Movie
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var movieAdapter: MovieAdapter
    private var allMovies: List<Movie> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchView = view.findViewById(R.id.searchView)
        recyclerView = view.findViewById(R.id.recyclerViewMovies)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        movieAdapter = MovieAdapter(emptyList()) { movie ->
            val intent = Intent(requireContext(), MovieDetailActivity::class.java)
            intent.putExtra("movie_id", movie.id)
            intent.putExtra("movie_title", movie.title)
            intent.putExtra("movie_overview", movie.overview)
            intent.putExtra("movie_poster", movie.posterPath)
            intent.putExtra("movie_rating", movie.voteAverage)
            startActivity(intent)
        }

        recyclerView.adapter = movieAdapter

        // Konfiguracja wyszukiwarki
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchMovies(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    movieAdapter.updateMovies(allMovies)
                }
                return true
            }
        })

        loadMovies()
    }

    private fun loadMovies() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getPopularMovies(RetrofitClient.API_KEY)
                allMovies = response.results
                movieAdapter.updateMovies(allMovies)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Błąd ładowania filmów: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun searchMovies(query: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.searchMovies(RetrofitClient.API_KEY, query)
                movieAdapter.updateMovies(response.results)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Błąd wyszukiwania: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}