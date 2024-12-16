package com.example.travelmatentt.fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.travelmatentt.R

class HotelAdapter(private val hotelList: List<HotelRecommendation>) : RecyclerView.Adapter<HotelAdapter.HotelViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HotelViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_hotel, parent, false)
        return HotelViewHolder(view)
    }

    override fun onBindViewHolder(holder: HotelViewHolder, position: Int) {
        val hotel = hotelList[position]
        holder.tvHotelName.text = hotel.hotelName
        holder.tvHotelRating.text = "Rating: ${hotel.hotelRating}"
        Glide.with(holder.itemView.context)
            .load(hotel.hotelImageUrl)
            .into(holder.ivHotelImage)
    }

    override fun getItemCount(): Int = hotelList.size

    class HotelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivHotelImage: ImageView = itemView.findViewById(R.id.ivHotelImage)
        val tvHotelName: TextView = itemView.findViewById(R.id.tvHotelName)
        val tvHotelRating: TextView = itemView.findViewById(R.id.tvHotelRating)
    }
}
