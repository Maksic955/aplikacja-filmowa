package com.example.movieapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.movieapp.R
import com.example.movieapp.api.RetrofitClient

class FullscreenImageAdapter(
    private val imagePaths: List<String>
) : RecyclerView.Adapter<FullscreenImageAdapter.FullscreenImageViewHolder>() {

    inner class FullscreenImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.ivFullscreenImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FullscreenImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_fullscreen_image, parent, false)
        return FullscreenImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: FullscreenImageViewHolder, position: Int) {
        val imagePath = imagePaths[position]
        val imageUrl = RetrofitClient.IMAGE_BASE_URL + imagePath

        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .placeholder(R.drawable.ic_launcher_background)
            .into(holder.imageView)
    }

    override fun getItemCount(): Int = imagePaths.size
}