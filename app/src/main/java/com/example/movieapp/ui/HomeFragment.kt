package com.example.movieapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
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
    private lateinit var btnPrevPage: Button
    private lateinit var btnNextPage: Button
    private lateinit var tvPageInfo: TextView
    private lateinit var btnSortAZ: Button
    private lateinit var btnSortZA: Button
    private lateinit var btnOpenFilter: Button

    private var currentPage = 1
    private var totalPages = 1
    private var isSearchMode = false
    private var currentSearchQuery = ""

    // Sortowanie alfabetyczne
    private enum class SortMode {
        NONE, A_TO_Z, Z_TO_A
    }

    private var currentSortMode = SortMode.NONE

    // Filtry
    private var selectedGenres = emptySet<Int>()
    private var selectedLanguage: String? = null
    private var selectedRatingMin: Double? = null
    private var selectedRatingMax: Double? = null

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
        btnPrevPage = view.findViewById(R.id.btnPrevPage)
        btnNextPage = view.findViewById(R.id.btnNextPage)
        tvPageInfo = view.findViewById(R.id.tvPageInfo)
        btnSortAZ = view.findViewById(R.id.btnSortAZ)
        btnSortZA = view.findViewById(R.id.btnSortZA)
        btnOpenFilter = view.findViewById(R.id.btnOpenFilter)

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
                query?.let {
                    isSearchMode = true
                    currentSearchQuery = it
                    currentPage = 1
                    searchMovies(it, currentPage)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    isSearchMode = false
                    currentPage = 1
                    loadMovies()
                }
                return true
            }
        })

        // Sortowanie A-Z
        btnSortAZ.setOnClickListener {
            currentSortMode = SortMode.A_TO_Z
            updateSortButtons()
            applySorting()
        }

        // Sortowanie Z-A
        btnSortZA.setOnClickListener {
            currentSortMode = SortMode.Z_TO_A
            updateSortButtons()
            applySorting()
        }

        // Otwórz filtr
        btnOpenFilter.setOnClickListener {
            openFilterBottomSheet()
        }

        // Przyciski paginacji
        btnPrevPage.setOnClickListener {
            if (currentPage > 1) {
                currentPage--
                loadMovies()
            }
        }

        btnNextPage.setOnClickListener {
            if (currentPage < totalPages) {
                currentPage++
                loadMovies()
            }
        }

        updateSortButtons()
        loadMovies()
    }

    private fun updateSortButtons() {
        btnSortAZ.alpha = if (currentSortMode == SortMode.A_TO_Z) 1.0f else 0.5f
        btnSortZA.alpha = if (currentSortMode == SortMode.Z_TO_A) 1.0f else 0.5f
    }

    private fun openFilterBottomSheet() {
        val filterSheet = FilterBottomSheetFragment()
        filterSheet.onFilterApplied = { genres, language, ratingMin, ratingMax ->
            selectedGenres = genres
            selectedLanguage = language
            selectedRatingMin = ratingMin
            selectedRatingMax = ratingMax

            currentPage = 1
            loadMovies()

            // Pokaż ikonę gdy filtry aktywne
            updateFilterButton()
        }
        filterSheet.show(parentFragmentManager, "FilterBottomSheet")
    }

    private fun updateFilterButton() {
        val hasActiveFilters = selectedGenres.isNotEmpty() ||
                selectedLanguage != null ||
                selectedRatingMin != null

        if (hasActiveFilters) {
            btnOpenFilter.text = "🔍 Filtry ✓"
            btnOpenFilter.alpha = 1.0f
        } else {
            btnOpenFilter.text = "🔍 Filtry"
            btnOpenFilter.alpha = 0.7f
        }
    }

    private fun loadMovies() {
        lifecycleScope.launch {
            try {
                val hasFilters = selectedGenres.isNotEmpty() ||
                        selectedLanguage != null ||
                        selectedRatingMin != null

                val response = if (isSearchMode) {
                    RetrofitClient.apiService.searchMovies(
                        RetrofitClient.API_KEY,
                        currentSearchQuery,
                        page = currentPage
                    )
                } else if (hasFilters) {
                    // Użyj discover endpoint z filtrami
                    val genresString = if (selectedGenres.isNotEmpty()) {
                        selectedGenres.joinToString(",")
                    } else null

                    RetrofitClient.apiService.discoverMovies(
                        apiKey = RetrofitClient.API_KEY,
                        page = currentPage,
                        genres = genresString,
                        originalLanguage = selectedLanguage,
                        voteAverageMin = selectedRatingMin,
                        voteAverageMax = selectedRatingMax
                    )
                } else {
                    // Domyślnie - popularne filmy
                    RetrofitClient.apiService.getPopularMovies(
                        RetrofitClient.API_KEY,
                        page = currentPage
                    )
                }

                totalPages = response.totalPages
                allMovies = response.results
                applySorting()
                updatePaginationUI()
                recyclerView.scrollToPosition(0)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Błąd ładowania filmów: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun searchMovies(query: String, page: Int) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.searchMovies(
                    RetrofitClient.API_KEY,
                    query,
                    page = page
                )
                totalPages = response.totalPages
                allMovies = response.results
                applySorting()
                updatePaginationUI()
                recyclerView.scrollToPosition(0)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Błąd wyszukiwania: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun applySorting() {
        val sortedMovies = when (currentSortMode) {
            SortMode.A_TO_Z -> allMovies.sortedBy { it.title }
            SortMode.Z_TO_A -> allMovies.sortedByDescending { it.title }
            SortMode.NONE -> allMovies
        }
        movieAdapter.updateMovies(sortedMovies)
    }

    private fun updatePaginationUI() {
        tvPageInfo.text = "Strona $currentPage z $totalPages"
        btnPrevPage.isEnabled = currentPage > 1
        btnNextPage.isEnabled = currentPage < totalPages
    }
}