package com.example.movieapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.movieapp.R
import com.example.movieapp.api.RetrofitClient

class ThumbnailGalleryAdapter(
    private val imagePaths: List<String>,
    private val onImageClick: (Int) -> Unit
) : RecyclerView.Adapter<ThumbnailGalleryAdapter.ThumbnailViewHolder>() {

    inner class ThumbnailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.ivThumbnail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThumbnailViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_thumbnail, parent, false)
        return ThumbnailViewHolder(view)
    }

    override fun onBindViewHolder(holder: ThumbnailViewHolder, position: Int) {
        val imagePath = imagePaths[position]
        val imageUrl = RetrofitClient.IMAGE_BASE_URL + imagePath

        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .placeholder(R.drawable.ic_launcher_background)
            .into(holder.imageView)

        holder.itemView.setOnClickListener {
            onImageClick(position)
        }
    }

    override fun getItemCount(): Int = imagePaths.size
}