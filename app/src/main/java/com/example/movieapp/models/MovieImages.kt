package com.example.movieapp.models

import com.google.gson.annotations.SerializedName

data class MovieImages(
    @SerializedName("id")
    val id: Int,

    @SerializedName("backdrops")
    val backdrops: List<ImageData>
)

data class ImageData(
    @SerializedName("file_path")
    val filePath: String,

    @SerializedName("width")
    val width: Int,

    @SerializedName("height")
    val height: Int
)