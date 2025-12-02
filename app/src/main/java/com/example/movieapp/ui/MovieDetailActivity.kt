package com.example.movieapp.ui

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
import com.example.movieapp.models.ImageData
import kotlinx.coroutines.launch

class MovieDetailActivity : AppCompatActivity() {

    private lateinit var ivPoster: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvRating: TextView
    private lateinit var tvOverview: TextView
    private lateinit var recyclerViewGallery: RecyclerView
    private lateinit var btnBack: Button

    private var currentImages: List<ImageData> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_detail)

        ivPoster = findViewById(R.id.ivMovieDetailPoster)
        tvTitle = findViewById(R.id.tvMovieDetailTitle)
        tvRating = findViewById(R.id.tvMovieDetailRating)
        tvOverview = findViewById(R.id.tvMovieDetailOverview)
        recyclerViewGallery = findViewById(R.id.recyclerViewGallery)
        btnBack = findViewById(R.id.btnBack)

        // Horizontal layout dla galerii
        recyclerViewGallery.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Pobieranie danych z Intent
        val movieId = intent.getIntExtra("movie_id", 0)
        val title = intent.getStringExtra("movie_title") ?: "Brak tytułu"
        val overview = intent.getStringExtra("movie_overview") ?: "Brak opisu"
        val posterPath = intent.getStringExtra("movie_poster")
        val rating = intent.getDoubleExtra("movie_rating", 0.0)

        // Wyświetlanie danych
        tvTitle.text = title
        tvRating.text = "⭐ ${String.format("%.1f", rating)}"
        tvOverview.text = overview

        val posterUrl = RetrofitClient.IMAGE_BASE_URL + posterPath
        Glide.with(this)
            .load(posterUrl)
            .placeholder(R.drawable.ic_launcher_background)
            .into(ivPoster)

        // Ładowanie galerii zdjęć
        loadMovieImages(movieId)

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun loadMovieImages(movieId: Int) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getMovieImages(movieId, RetrofitClient.API_KEY)
                val images = response.backdrops.take(10)
                currentImages = images

                if (images.isNotEmpty()) {
                    val adapter = ImageGalleryAdapter(images) { position ->
                        // Kliknięcie w zdjęcie - otwieramy pełnoekranową galerię
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