package com.example.travelmatentt.fragment

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.travelmatentt.R
import com.example.travelmatentt.databinding.ItemDestinationBinding

class DestinationsAdapter(private val destinations: List<DestinationResponse>, private val onItemClick: (DestinationResponse) -> Unit) :
    RecyclerView.Adapter<DestinationsAdapter.DestinationViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DestinationViewHolder {
        val binding = ItemDestinationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DestinationViewHolder(binding)
    }


    override fun onBindViewHolder(holder: DestinationViewHolder, position: Int) {
        val destination = destinations[position]
        holder.bind(destination)
    }


    override fun getItemCount(): Int = destinations.size


    inner class DestinationViewHolder(private val binding: ItemDestinationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(destination: DestinationResponse) {

            binding.apply {
                tvNamaObjek.text = destination.nama_objek
                tvDeskripsi.text = destination.deskripsi
                tvAlamat.text = destination.alamat
                tvRating.text = destination.rating.toString()
                tvEstimasiHargaTiket.text = destination.estimasi_harga_tiket.toString()


                Glide.with(itemView.context)
                    .load(destination.picture_url)
                    .into(imageViewDestination)


                root.setOnClickListener { onItemClick(destination) }
            }
        }
    }
}



