package com.example.movieapp

import android.util.Patterns

object ValidationUtils {

    fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPassword(password: String): Boolean {
        // Minimum 4 znaki, przynajmniej jedna wielka litera, jedna cyfra, jeden znak specjalny
        if (password.length < 4) return false

        val hasUpperCase = password.any { it.isUpperCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecialChar = password.any { !it.isLetterOrDigit() }

        return hasUpperCase && hasDigit && hasSpecialChar
    }

    fun getPasswordError(password: String): String {
        return when {
            password.length < 4 -> "Hasło musi mieć minimum 4 znaki"
            !password.any { it.isUpperCase() } -> "Hasło musi zawierać przynajmniej jedną wielką literę"
            !password.any { it.isDigit() } -> "Hasło musi zawierać przynajmniej jedną cyfrę"
            !password.any { !it.isLetterOrDigit() } -> "Hasło musi zawierać przynajmniej jeden znak specjalny"
            else -> ""
        }
    }
}