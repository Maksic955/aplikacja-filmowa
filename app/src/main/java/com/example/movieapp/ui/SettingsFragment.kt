package com.example.movieapp.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.movieapp.R

class SettingsFragment : Fragment() {

    private lateinit var switchDarkMode: Switch
    private lateinit var btnLogout: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        switchDarkMode = view.findViewById(R.id.switchDarkMode)
        btnLogout = view.findViewById(R.id.btnLogout)

        // Wczytaj zapisane ustawienie ciemnego motywu
        val sharedPrefs = requireActivity().getSharedPreferences("MovieGalleryPrefs", Context.MODE_PRIVATE)
        val isDarkMode = sharedPrefs.getBoolean("dark_mode", false)
        switchDarkMode.isChecked = isDarkMode

        // Obsługa zmiany motywu
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean("dark_mode", isChecked).apply()

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        // Wylogowanie
        btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun logout() {
        // Usuń dane zalogowanego użytkownika
        val sharedPrefs = requireActivity().getSharedPreferences("MovieGalleryPrefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().remove("logged_in_user").apply()

        Toast.makeText(requireContext(), "Wylogowano pomyślnie", Toast.LENGTH_SHORT).show()

        // Przekieruj do ekranu logowania
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}