package com.example.movieapp.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.movieapp.R
import com.example.movieapp.adapters.ImageGalleryAdapter
import com.example.movieapp.api.RetrofitClient
import com.example.movieapp.database.DatabaseHelper
import com.example.movieapp.models.ImageData
import kotlinx.coroutines.launch

class MovieDetailActivity : AppCompatActivity() {

    private lateinit var ivPoster: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvRating: TextView
    private lateinit var tvOverview: TextView
    private lateinit var recyclerViewGallery: RecyclerView
    private lateinit var btnBack: Button
    private lateinit var btnToggleFavorite: Button

    private lateinit var dbHelper: DatabaseHelper
    private var currentImages: List<ImageData> = emptyList()

    private var movieId: Int = 0
    private var movieTitle: String = ""
    private var moviePosterPath: String? = null
    private var movieRating: Double = 0.0
    private var isFavorite: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_detail)

        ivPoster = findViewById(R.id.ivMovieDetailPoster)
        tvTitle = findViewById(R.id.tvMovieDetailTitle)
        tvRating = findViewById(R.id.tvMovieDetailRating)
        tvOverview = findViewById(R.id.tvMovieDetailOverview)
        recyclerViewGallery = findViewById(R.id.recyclerViewGallery)
        btnBack = findViewById(R.id.btnBack)
        btnToggleFavorite = findViewById(R.id.btnToggleFavorite)

        dbHelper = DatabaseHelper(this)

        // Horizontal layout dla galerii
        recyclerViewGallery.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Pobieranie danych z Intent
        movieId = intent.getIntExtra("movie_id", 0)
        movieTitle = intent.getStringExtra("movie_title") ?: "Brak tytułu"
        val overview = intent.getStringExtra("movie_overview") ?: "Brak opisu"
        moviePosterPath = intent.getStringExtra("movie_poster")
        movieRating = intent.getDoubleExtra("movie_rating", 0.0)

        tvTitle.text = movieTitle
        tvRating.text = "⭐ ${String.format("%.1f", movieRating)}"
        tvOverview.text = overview

        val posterUrl = RetrofitClient.IMAGE_BASE_URL + moviePosterPath
        Glide.with(this)
            .load(posterUrl)
            .placeholder(R.drawable.ic_launcher_background)
            .into(ivPoster)

        checkFavoriteStatus()

        loadMovieImages(movieId)

        btnToggleFavorite.setOnClickListener {
            toggleFavorite()
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun checkFavoriteStatus() {
        val username = getLoggedInUsername()
        isFavorite = dbHelper.isFavorite(username, movieId)
        updateFavoriteButton()
    }

    private fun updateFavoriteButton() {
        if (isFavorite) {
            btnToggleFavorite.text = "★ Usuń z ulubionych"
            btnToggleFavorite.backgroundTintList = getColorStateList(android.R.color.holo_orange_light)
        } else {
            btnToggleFavorite.text = "☆ Dodaj do ulubionych"
            btnToggleFavorite.backgroundTintList = null
        }
    }

    private fun toggleFavorite() {
        val username = getLoggedInUsername()

        if (isFavorite) {
            // Usuń z ulubionych
            if (dbHelper.removeFavorite(username, movieId)) {
                isFavorite = false
                updateFavoriteButton()
                Toast.makeText(this, "Usunięto z ulubionych", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Dodaj do ulubionych
            if (dbHelper.addFavorite(username, movieId, movieTitle, moviePosterPath, movieRating)) {
                isFavorite = true
                updateFavoriteButton()
                Toast.makeText(this, "Dodano do ulubionych", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getLoggedInUsername(): String {
        val sharedPrefs = getSharedPreferences("MovieGalleryPrefs", Context.MODE_PRIVATE)
        return sharedPrefs.getString("logged_in_user", "") ?: ""
    }

    private fun loadMovieImages(movieId: Int) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getMovieImages(movieId, RetrofitClient.API_KEY)
                val images = response.backdrops.take(10)
                currentImages = images

                if (images.isNotEmpty()) {
                    val adapter = ImageGalleryAdapter(images) { position ->
                        openFullscreenGallery(position)
                    }
                    recyclerViewGallery.adapter = adapter
                }
            } catch (e: Exception) {
                Toast.makeText(this@MovieDetailActivity, "Nie udało się załadować galerii", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openFullscreenGallery(position: Int) {
        val imagePaths = ArrayList(currentImages.map { it.filePath })
        val intent = Intent(this, FullscreenGalleryActivity::class.java)
        intent.putStringArrayListExtra("image_paths", imagePaths)
        intent.putExtra("position", position)
        startActivity(intent)
    }
}