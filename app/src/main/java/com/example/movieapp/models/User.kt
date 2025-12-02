package com.example.movieapp.models

data class User(
    val id: Int = 0,
    val username: String,
    val passwordHash: String,
    val email: String
)