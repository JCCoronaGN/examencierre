package com.example.examencierre.ui.photos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.examencierre.R
import com.example.examencierre.data.local.entity.PhotoEntity
import com.example.examencierre.databinding.ItemPhotoBinding

class PhotosAdapter(
    private val onPhotoClick: (PhotoEntity) -> Unit
) : ListAdapter<PhotoEntity, PhotosAdapter.PhotoViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val binding = ItemPhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PhotoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PhotoViewHolder(
        private val binding: ItemPhotoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(photo: PhotoEntity) {
            binding.imageViewPhoto.load(photo.ruta) {
                crossfade(true)
                placeholder(R.drawable.ic_photo_placeholder)
                error(R.drawable.ic_photo_placeholder)
            }
            binding.textViewComment.text = photo.comentario
                ?.takeIf { it.isNotBlank() }
                ?: binding.root.context.getString(R.string.detail_no_comment)
            binding.textViewCoordinates.text = if (photo.latitud != null && photo.longitud != null) {
                binding.root.context.getString(
                    R.string.gps_coordinates_format,
                    photo.latitud,
                    photo.longitud
                )
            } else {
                binding.root.context.getString(R.string.detail_no_gps)
            }
            binding.root.setOnClickListener { onPhotoClick(photo) }
        }
    }

    private companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<PhotoEntity>() {
            override fun areItemsTheSame(oldItem: PhotoEntity, newItem: PhotoEntity): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: PhotoEntity, newItem: PhotoEntity): Boolean =
                oldItem == newItem
        }
    }
}
