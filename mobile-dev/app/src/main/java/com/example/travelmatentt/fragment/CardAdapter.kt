package com.example.travelmatentt.fragment

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.travelmatentt.R
import com.example.travelmatentt.databinding.ItemCardBinding

class CardAdapter(
    private val context: Context,
    private val cardNames: List<String>,
    private val cardImages: List<Int>,
    private val cardRatings: List<Float>
) : RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding = ItemCardBinding.inflate(LayoutInflater.from(context), parent, false)
        return CardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val cardName = cardNames[position]
        val cardImage = cardImages[position]
        val cardRating = cardRatings[position]
        holder.bind(cardName, cardImage, cardRating)
    }

    override fun getItemCount(): Int = cardNames.size

    inner class CardViewHolder(private val binding: ItemCardBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(name: String, image: Int, rating: Float) {
            binding.tvCardTitle.text = name
            binding.ivCardImage.setImageResource(image)

            val stars = listOf(binding.star1, binding.star2, binding.star3, binding.star4, binding.star5)

            // Display stars based on rating
            for (i in stars.indices) {
                stars[i].setImageResource(
                    if (i < rating) R.drawable.star_24dp else R.drawable.baseline_star_24
                )
            }
        }
    }
}


