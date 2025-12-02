package com.example.movieapp.ui

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.movieapp.R
import com.example.movieapp.adapters.FullscreenImageAdapter
import com.example.movieapp.adapters.ThumbnailGalleryAdapter

class FullscreenGalleryActivity : AppCompatActivity() {

    private lateinit var viewPagerImages: ViewPager2
    private lateinit var recyclerViewThumbnails: RecyclerView
    private lateinit var tvImageCounter: TextView
    private lateinit var btnClose: Button

    private var imagePaths: ArrayList<String> = arrayListOf()
    private var currentPosition: Int = 0
    private lateinit var thumbnailAdapter: ThumbnailGalleryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen_gallery)

        viewPagerImages = findViewById(R.id.viewPagerImages)
        recyclerViewThumbnails = findViewById(R.id.recyclerViewThumbnails)
        tvImageCounter = findViewById(R.id.tvImageCounter)
        btnClose = findViewById(R.id.btnCloseGallery)

        // Pobieranie danych z Intent
        imagePaths = intent.getStringArrayListExtra("image_paths") ?: arrayListOf()
        currentPosition = intent.getIntExtra("position", 0)

        // Konfiguracja ViewPager2 (główne zdjęcia - swipe)
        val fullscreenAdapter = FullscreenImageAdapter(imagePaths)
        viewPagerImages.adapter = fullscreenAdapter
        viewPagerImages.setCurrentItem(currentPosition, false)

        // Listener dla zmiany strony (gdy przesuwamy palcem)
        viewPagerImages.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentPosition = position
                updateCounter(position)
                recyclerViewThumbnails.smoothScrollToPosition(position)
            }
        })

        // Konfiguracja RecyclerView z miniaturkami
        recyclerViewThumbnails.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        thumbnailAdapter = ThumbnailGalleryAdapter(imagePaths) { position ->
            viewPagerImages.setCurrentItem(position, true)
        }
        recyclerViewThumbnails.adapter = thumbnailAdapter
        recyclerViewThumbnails.scrollToPosition(currentPosition)

        // Inicjalizacja licznika
        updateCounter(currentPosition)

        btnClose.setOnClickListener {
            finish()
        }
    }

    private fun updateCounter(position: Int) {
        tvImageCounter.text = "${position + 1} / ${imagePaths.size}"
    }
}