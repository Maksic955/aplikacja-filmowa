package com.example.movieapp.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.movieapp.R
import com.example.movieapp.database.DatabaseHelper
import com.example.movieapp.ValidationUtils

class RegisterActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var etRegUsername: EditText
    private lateinit var etRegEmail: EditText
    private lateinit var etRegPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var btnBackToLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        dbHelper = DatabaseHelper(this)

        etRegUsername = findViewById(R.id.etRegUsername)
        etRegEmail = findViewById(R.id.etRegEmail)
        etRegPassword = findViewById(R.id.etRegPassword)
        btnRegister = findViewById(R.id.btnRegister)
        btnBackToLogin = findViewById(R.id.btnBackToLogin)

        btnRegister.setOnClickListener {
            val username = etRegUsername.text.toString().trim()
            val email = etRegEmail.text.toString().trim()
            val password = etRegPassword.text.toString()

            // Walidacja
            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Wypełnij wszystkie pola", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!ValidationUtils.isValidEmail(email)) {
                Toast.makeText(this, "Podaj poprawny adres email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!ValidationUtils.isValidPassword(password)) {
                val errorMessage = ValidationUtils.getPasswordError(password)
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (dbHelper.userExists(username)) {
                Toast.makeText(this, "Użytkownik już istnieje", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (dbHelper.registerUser(username, password, email)) {
                Toast.makeText(this, "Rejestracja pomyślna! Możesz się teraz zalogować", Toast.LENGTH_LONG).show()
                finish()
            } else {
                Toast.makeText(this, "Błąd rejestracji", Toast.LENGTH_SHORT).show()
            }
        }

        btnBackToLogin.setOnClickListener {
            finish()
        }
    }
}