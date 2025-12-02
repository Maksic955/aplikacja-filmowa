package com.example.movieapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.movieapp.R
import com.example.movieapp.api.RetrofitClient
import com.example.movieapp.database.FavoriteMovie

class FavoriteMovieAdapter(
    private var movies: List<FavoriteMovie>,
    private val onMovieClick: (FavoriteMovie) -> Unit,
    private val onRemoveClick: (FavoriteMovie) -> Unit
) : RecyclerView.Adapter<FavoriteMovieAdapter.FavoriteMovieViewHolder>() {

    inner class FavoriteMovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val posterImage: ImageView = itemView.findViewById(R.id.ivMoviePoster)
        val titleText: TextView = itemView.findViewById(R.id.tvMovieTitle)
        val ratingText: TextView = itemView.findViewById(R.id.tvMovieRating)
        val btnRemove: Button = itemView.findViewById(R.id.btnRemoveFavorite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteMovieViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite_movie, parent, false)
        return FavoriteMovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteMovieViewHolder, position: Int) {
        val movie = movies[position]
        holder.titleText.text = movie.title
        holder.ratingText.text = "⭐ ${String.format("%.1f", movie.rating)}"

        val posterUrl = RetrofitClient.IMAGE_BASE_URL + movie.posterPath
        Glide.with(holder.itemView.context)
            .load(posterUrl)
            .placeholder(R.drawable.ic_launcher_background)
            .into(holder.posterImage)

        holder.itemView.setOnClickListener {
            onMovieClick(movie)
        }

        holder.btnRemove.setOnClickListener {
            onRemoveClick(movie)
        }
    }

    override fun getItemCount(): Int = movies.size

    fun updateMovies(newMovies: List<FavoriteMovie>) {
        movies = newMovies
        notifyDataSetChanged()
    }
}