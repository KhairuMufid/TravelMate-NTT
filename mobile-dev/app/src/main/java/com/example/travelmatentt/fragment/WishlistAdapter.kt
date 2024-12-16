package com.example.travelmatentt.fragment

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.travelmatentt.R
import com.example.travelmatentt.databinding.ItemWishlistBinding

class WishlistAdapter(private val onItemClick: (Destination) -> Unit) :
    ListAdapter<WishlistItem, WishlistAdapter.WishlistViewHolder>(WishlistDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WishlistViewHolder {
        val binding = ItemWishlistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WishlistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WishlistViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class WishlistViewHolder(private val binding: ItemWishlistBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: WishlistItem) {

            item.destination?.let { destination ->

                binding.tvDestinationName.text = destination.nama_objek ?: "Nama Objek Tidak Ditemukan"

                binding.tvDestinationDescription.text = destination.deskripsi ?: "Deskripsi Tidak Tersedia"

                if (!destination.picture_url.isNullOrEmpty()) {
                    Glide.with(binding.root.context)
                        .load(destination.picture_url)
                        .into(binding.ivDestinationImage)
                } else {
                    binding.ivDestinationImage.setImageResource(R.drawable.uploadimage)
                }

                binding.root.setOnClickListener {
                    onItemClick(destination)
                }

                binding.root.setOnClickListener {
                    val context = binding.root.context
                    val intent = Intent(context, DetailWishlistActivity::class.java).apply {
                        putExtra("nama_objek", destination.nama_objek)
                        putExtra("deskripsi", destination.deskripsi)
                        putExtra("picture_url", destination.picture_url)
                        putExtra("rating", destination.rating)
                        putExtra("alamat", destination.alamat)
                        putExtra("estimasi_harga_tiket", destination.estimasi_harga_tiket)
                    }
                    context.startActivity(intent)
                }
            } ?: run {

                binding.tvDestinationName.text = "Destinasi Tidak Tersedia"
                binding.tvDestinationDescription.text = "Tidak ada deskripsi untuk destinasi ini."
                binding.ivDestinationImage.setImageResource(R.drawable.uploadimage) // Gambar placeholder
            }
        }
    }
}
