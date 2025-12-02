package com.example.movieapp.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.movieapp.models.User
import java.security.MessageDigest

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "MovieGallery.db"
        private const val DATABASE_VERSION = 2  // ZWIĘKSZAMY WERSJĘ!

        // Tabela użytkowników
        private const val TABLE_USERS = "users"
        private const val COLUMN_ID = "id"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_PASSWORD = "password_hash"
        private const val COLUMN_EMAIL = "email"

        // Tabela ulubionych filmów
        private const val TABLE_FAVORITES = "favorites"
        private const val COLUMN_FAV_ID = "id"
        private const val COLUMN_FAV_USERNAME = "username"
        private const val COLUMN_FAV_MOVIE_ID = "movie_id"
        private const val COLUMN_FAV_MOVIE_TITLE = "movie_title"
        private const val COLUMN_FAV_MOVIE_POSTER = "movie_poster"
        private const val COLUMN_FAV_MOVIE_RATING = "movie_rating"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Tabela użytkowników
        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USERNAME TEXT UNIQUE NOT NULL,
                $COLUMN_PASSWORD TEXT NOT NULL,
                $COLUMN_EMAIL TEXT NOT NULL
            )
        """.trimIndent()
        db?.execSQL(createUsersTable)

        // Tabela ulubionych
        val createFavoritesTable = """
            CREATE TABLE $TABLE_FAVORITES (
                $COLUMN_FAV_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_FAV_USERNAME TEXT NOT NULL,
                $COLUMN_FAV_MOVIE_ID INTEGER NOT NULL,
                $COLUMN_FAV_MOVIE_TITLE TEXT NOT NULL,
                $COLUMN_FAV_MOVIE_POSTER TEXT,
                $COLUMN_FAV_MOVIE_RATING REAL,
                UNIQUE($COLUMN_FAV_USERNAME, $COLUMN_FAV_MOVIE_ID)
            )
        """.trimIndent()
        db?.execSQL(createFavoritesTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_FAVORITES")
        onCreate(db)
    }

    // Funkcja do hashowania hasła
    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    // Rejestracja użytkownika
    fun registerUser(username: String, password: String, email: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, username)
            put(COLUMN_PASSWORD, hashPassword(password))
            put(COLUMN_EMAIL, email)
        }

        return try {
            val result = db.insert(TABLE_USERS, null, values)
            result != -1L
        } catch (e: Exception) {
            false
        } finally {
            db.close()
        }
    }

    // Logowanie użytkownika
    fun loginUser(username: String, password: String): Boolean {
        val db = readableDatabase
        val hashedPassword = hashPassword(password)
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_ID),
            "$COLUMN_USERNAME = ? AND $COLUMN_PASSWORD = ?",
            arrayOf(username, hashedPassword),
            null, null, null
        )

        val result = cursor.count > 0
        cursor.close()
        db.close()
        return result
    }

    // Sprawdzenie czy użytkownik istnieje
    fun userExists(username: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_ID),
            "$COLUMN_USERNAME = ?",
            arrayOf(username),
            null, null, null
        )

        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }

    // ============ FUNKCJE DLA ULUBIONYCH ============

    // Dodanie filmu do ulubionych
    fun addFavorite(username: String, movieId: Int, title: String, posterPath: String?, rating: Double): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_FAV_USERNAME, username)
            put(COLUMN_FAV_MOVIE_ID, movieId)
            put(COLUMN_FAV_MOVIE_TITLE, title)
            put(COLUMN_FAV_MOVIE_POSTER, posterPath)
            put(COLUMN_FAV_MOVIE_RATING, rating)
        }

        return try {
            val result = db.insert(TABLE_FAVORITES, null, values)
            result != -1L
        } catch (e: Exception) {
            false
        } finally {
            db.close()
        }
    }

    // Usunięcie filmu z ulubionych
    fun removeFavorite(username: String, movieId: Int): Boolean {
        val db = writableDatabase
        return try {
            val result = db.delete(
                TABLE_FAVORITES,
                "$COLUMN_FAV_USERNAME = ? AND $COLUMN_FAV_MOVIE_ID = ?",
                arrayOf(username, movieId.toString())
            )
            result > 0
        } catch (e: Exception) {
            false
        } finally {
            db.close()
        }
    }

    // Sprawdzenie czy film jest w ulubionych
    fun isFavorite(username: String, movieId: Int): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_FAVORITES,
            arrayOf(COLUMN_FAV_ID),
            "$COLUMN_FAV_USERNAME = ? AND $COLUMN_FAV_MOVIE_ID = ?",
            arrayOf(username, movieId.toString()),
            null, null, null
        )

        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }

    // Pobranie wszystkich ulubionych filmów użytkownika
    fun getFavorites(username: String): List<FavoriteMovie> {
        val favorites = mutableListOf<FavoriteMovie>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_FAVORITES,
            null,
            "$COLUMN_FAV_USERNAME = ?",
            arrayOf(username),
            null, null, null
        )

        while (cursor.moveToNext()) {
            val movieId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FAV_MOVIE_ID))
            val title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FAV_MOVIE_TITLE))
            val posterPath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FAV_MOVIE_POSTER))
            val rating = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_FAV_MOVIE_RATING))

            favorites.add(FavoriteMovie(movieId, title, posterPath, rating))
        }

        cursor.close()
        db.close()
        return favorites
    }
}

// Model dla ulubionych filmów
data class FavoriteMovie(
    val id: Int,
    val title: String,
    val posterPath: String?,
    val rating: Double
)