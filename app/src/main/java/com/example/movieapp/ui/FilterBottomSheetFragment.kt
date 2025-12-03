package com.example.movieapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.movieapp.R
import com.example.movieapp.api.RetrofitClient
import com.example.movieapp.models.Genre
import kotlinx.coroutines.launch

class FilterBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var linearLayoutGenres: LinearLayout
    private lateinit var radioGroupLanguage: RadioGroup
    private lateinit var radioGroupRating: RadioGroup
    private lateinit var btnApplyFilter: Button
    private lateinit var btnClearFilter: Button

    private var genres: List<Genre> = emptyList()
    private var selectedGenres = mutableSetOf<Int>()
    private var selectedLanguage: String? = null
    private var selectedRatingMin: Double? = null
    private var selectedRatingMax: Double? = null

    var onFilterApplied: ((
        genres: Set<Int>,
        language: String?,
        ratingMin: Double?,
        ratingMax: Double?
    ) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_filter_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        linearLayoutGenres = view.findViewById(R.id.linearLayoutGenres)
        radioGroupLanguage = view.findViewById(R.id.radioGroupLanguage)
        radioGroupRating = view.findViewById(R.id.radioGroupRating)
        btnApplyFilter = view.findViewById(R.id.btnApplyFilter)
        btnClearFilter = view.findViewById(R.id.btnClearFilter)

        loadGenres()
        setupLanguageOptions()
        setupRatingOptions()

        btnApplyFilter.setOnClickListener {
            applyFilters()
        }

        btnClearFilter.setOnClickListener {
            clearFilters()
        }
    }

    private fun loadGenres() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getGenres(RetrofitClient.API_KEY)
                genres = response.genres
                displayGenres()
            } catch (e: Exception) {
                // Błąd ładowania gatunków
            }
        }
    }

    private fun displayGenres() {
        linearLayoutGenres.removeAllViews()

        genres.forEach { genre ->
            val checkBox = CheckBox(requireContext())
            checkBox.text = genre.name
            checkBox.isChecked = selectedGenres.contains(genre.id)
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedGenres.add(genre.id)
                } else {
                    selectedGenres.remove(genre.id)
                }
            }
            linearLayoutGenres.addView(checkBox)
        }
    }

    private fun setupLanguageOptions() {
        radioGroupLanguage.setOnCheckedChangeListener { _, checkedId ->
            selectedLanguage = when (checkedId) {
                R.id.radioLangAll -> null
                R.id.radioLangPolish -> "pl"
                R.id.radioLangEnglish -> "en"
                R.id.radioLangFrench -> "fr"
                R.id.radioLangGerman -> "de"
                R.id.radioLangSpanish -> "es"
                else -> null
            }
        }
    }

    private fun setupRatingOptions() {
        radioGroupRating.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioRatingAll -> {
                    selectedRatingMin = null
                    selectedRatingMax = null
                }
                R.id.radioRating9Plus -> {
                    selectedRatingMin = 9.0
                    selectedRatingMax = 10.0
                }
                R.id.radioRating8Plus -> {
                    selectedRatingMin = 8.0
                    selectedRatingMax = 10.0
                }
                R.id.radioRating7Plus -> {
                    selectedRatingMin = 7.0
                    selectedRatingMax = 10.0
                }
                R.id.radioRating5to7 -> {
                    selectedRatingMin = 5.0
                    selectedRatingMax = 7.0
                }
                R.id.radioRatingBelow5 -> {
                    selectedRatingMin = 0.0
                    selectedRatingMax = 5.0
                }
            }
        }
    }

    private fun applyFilters() {
        onFilterApplied?.invoke(
            selectedGenres,
            selectedLanguage,
            selectedRatingMin,
            selectedRatingMax
        )
        dismiss()
    }

    private fun clearFilters() {
        selectedGenres.clear()
        selectedLanguage = null
        selectedRatingMin = null
        selectedRatingMax = null

        displayGenres()
        radioGroupLanguage.check(R.id.radioLangAll)
        radioGroupRating.check(R.id.radioRatingAll)

        onFilterApplied?.invoke(
            emptySet(),
            null,
            null,
            null
        )
        dismiss()
    }
}