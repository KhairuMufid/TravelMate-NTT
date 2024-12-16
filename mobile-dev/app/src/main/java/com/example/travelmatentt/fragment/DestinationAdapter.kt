package com.example.travelmatentt.fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.travelmatentt.R

class DestinationAdapter(private val destinationList: List<DestinationResponse>) :
    RecyclerView.Adapter<DestinationAdapter.DestinationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DestinationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_destination2, parent, false)
        return DestinationViewHolder(view)
    }

    override fun onBindViewHolder(holder: DestinationViewHolder, position: Int) {
        val destination = destinationList[position]
        holder.tvDestinationName.text = destination.nama_objek
        holder.tvDestinationDescription.text = destination.deskripsi
        Glide.with(holder.itemView.context)
            .load(destination.picture_url)
            .into(holder.ivDestinationImage)
    }

    override fun getItemCount(): Int = destinationList.size

    class DestinationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivDestinationImage: ImageView = itemView.findViewById(R.id.ivDestinationImage)
        val tvDestinationName: TextView = itemView.findViewById(R.id.tvDestinationName)
        val tvDestinationDescription: TextView = itemView.findViewById(R.id.tvDestinationDescription)
    }
}


