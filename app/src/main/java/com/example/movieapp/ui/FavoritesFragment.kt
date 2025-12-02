package com.example.movieapp.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.movieapp.R
import com.example.movieapp.adapters.FavoriteMovieAdapter
import com.example.movieapp.database.DatabaseHelper
import com.example.movieapp.database.FavoriteMovie

class FavoritesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmptyState: TextView
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var favoriteAdapter: FavoriteMovieAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerViewFavorites)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)

        dbHelper = DatabaseHelper(requireContext())

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        favoriteAdapter = FavoriteMovieAdapter(emptyList(),
            onMovieClick = { movie ->
                val intent = Intent(requireContext(), MovieDetailActivity::class.java)
                intent.putExtra("movie_id", movie.id)
                intent.putExtra("movie_title", movie.title)
                intent.putExtra("movie_overview", "")
                intent.putExtra("movie_poster", movie.posterPath)
                intent.putExtra("movie_rating", movie.rating)
                startActivity(intent)
            },
            onRemoveClick = { movie ->
                removeFromFavorites(movie)
            }
        )

        recyclerView.adapter = favoriteAdapter

        loadFavorites()
    }

    override fun onResume() {
        super.onResume()
        loadFavorites()
    }

    private fun loadFavorites() {
        val username = getLoggedInUsername()
        val favorites = dbHelper.getFavorites(username)

        if (favorites.isEmpty()) {
            tvEmptyState.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            tvEmptyState.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            favoriteAdapter.updateMovies(favorites)
        }
    }

    private fun removeFromFavorites(movie: FavoriteMovie) {
        val username = getLoggedInUsername()
        dbHelper.removeFavorite(username, movie.id)
        loadFavorites()
    }

    private fun getLoggedInUsername(): String {
        val sharedPrefs = requireActivity().getSharedPreferences("MovieGalleryPrefs", Context.MODE_PRIVATE)
        return sharedPrefs.getString("logged_in_user", "") ?: ""
    }
}