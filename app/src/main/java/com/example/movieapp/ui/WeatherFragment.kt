package com.example.movieapp.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.example.movieapp.R
import com.example.movieapp.api.WeatherRetrofitClient
import kotlinx.coroutines.launch

fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}

class WeatherFragment : Fragment() {

    private lateinit var tvCityName: TextView
    private lateinit var tvCoordinates: TextView
    private lateinit var tvTemperature: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvFeelsLike: TextView
    private lateinit var tvHumidity: TextView
    private lateinit var tvPressure: TextView
    private lateinit var tvWind: TextView
    private lateinit var ivWeatherIcon: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var weatherContainer: View
    private lateinit var tvError: TextView
    private lateinit var btnRefresh: Button

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Launcher dla uprawnień lokalizacji
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            getLocationAndWeather()
        } else {
            showError("Brak uprawnień do lokalizacji")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_weather, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicjalizacja widoków
        tvCityName = view.findViewById(R.id.tvCityName)
        tvCoordinates = view.findViewById(R.id.tvCoordinates)
        tvTemperature = view.findViewById(R.id.tvTemperature)
        tvDescription = view.findViewById(R.id.tvDescription)
        tvFeelsLike = view.findViewById(R.id.tvFeelsLike)
        tvHumidity = view.findViewById(R.id.tvHumidity)
        tvPressure = view.findViewById(R.id.tvPressure)
        tvWind = view.findViewById(R.id.tvWind)
        ivWeatherIcon = view.findViewById(R.id.ivWeatherIcon)
        progressBar = view.findViewById(R.id.progressBar)
        weatherContainer = view.findViewById(R.id.weatherContainer)
        tvError = view.findViewById(R.id.tvError)
        btnRefresh = view.findViewById(R.id.btnRefreshWeather)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        btnRefresh.setOnClickListener {
            checkPermissionsAndGetWeather()
        }

        // Automatyczne załadowanie pogody przy starcie
        checkPermissionsAndGetWeather()
    }

    private fun checkPermissionsAndGetWeather() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getLocationAndWeather()
            }
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getLocationAndWeather()
            }
            else -> {
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    private fun getLocationAndWeather() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            showError("Brak uprawnień do lokalizacji")
            return
        }

        showLoading()

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                loadWeather(location.latitude, location.longitude)
            } else {
                showError("Nie można uzyskać lokalizacji. Upewnij się, że GPS jest włączony.")
            }
        }.addOnFailureListener {
            showError("Błąd pobierania lokalizacji: ${it.message}")
        }
    }

    private fun loadWeather(latitude: Double, longitude: Double) {
        lifecycleScope.launch {
            try {
                val weather = WeatherRetrofitClient.apiService.getCurrentWeather(
                    latitude = latitude,
                    longitude = longitude,
                    apiKey = WeatherRetrofitClient.API_KEY
                )

                displayWeather(weather)
            } catch (e: Exception) {
                showError("Błąd ładowania pogody: ${e.message}")
            }
        }
    }

    private fun displayWeather(weather: com.example.movieapp.models.WeatherResponse) {
        hideLoading()
        weatherContainer.visibility = View.VISIBLE
        tvError.visibility = View.GONE

        // Lokalizacja
        tvCityName.text = weather.cityName
        tvCoordinates.text = "Współrzędne: ${String.format("%.2f", weather.coord.lat)}, ${String.format("%.2f", weather.coord.lon)}"

        // Temperatura i opis
        tvTemperature.text = "${weather.main.temp.toInt()}°C"
        tvDescription.text = weather.weather.firstOrNull()?.description?.capitalize() ?: "Brak opisu"

        // Szczegóły
        tvFeelsLike.text = "Odczuwalna: ${weather.main.feelsLike.toInt()}°C"
        tvHumidity.text = "Wilgotność: ${weather.main.humidity}%"
        tvPressure.text = "Ciśnienie: ${weather.main.pressure} hPa"
        tvWind.text = "Wiatr: ${weather.wind.speed} m/s"

        // Ikona pogody
        val iconCode = weather.weather.firstOrNull()?.icon ?: "01d"
        val iconUrl = "${WeatherRetrofitClient.ICON_BASE_URL}${iconCode}@2x.png"
        Glide.with(this)
            .load(iconUrl)
            .into(ivWeatherIcon)
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        weatherContainer.visibility = View.GONE
        tvError.visibility = View.GONE
    }

    private fun hideLoading() {
        progressBar.visibility = View.GONE
    }

    private fun showError(message: String) {
        hideLoading()
        weatherContainer.visibility = View.GONE
        tvError.visibility = View.VISIBLE
        tvError.text = message
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
}