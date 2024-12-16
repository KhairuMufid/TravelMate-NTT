package com.example.travelmatentt.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.travelmatentt.data.response.Recommendation
import com.example.travelmatentt.databinding.ItemRecommendationBinding


class RecommendationAdapter(
    private val onClick: (Recommendation) -> Unit
) : ListAdapter<Recommendation, RecommendationAdapter.RecommendationViewHolder>(
    RecommendationDiffCallback()
) {
    class RecommendationDiffCallback : DiffUtil.ItemCallback<Recommendation>() {
        override fun areItemsTheSame(oldItem: Recommendation, newItem: Recommendation): Boolean {
            return oldItem.destinasi_wisata_id == newItem.destinasi_wisata_id
        }

        override fun areContentsTheSame(oldItem: Recommendation, newItem: Recommendation): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendationViewHolder {
        val binding =
            ItemRecommendationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecommendationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecommendationViewHolder, position: Int) {
        val recommendation = getItem(position)
        holder.bind(recommendation)
    }

    inner class RecommendationViewHolder(private val binding: ItemRecommendationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(recommendation: Recommendation) {
            binding.tvDestinationName.text = recommendation.nama_objek
            binding.tvDescription.text = recommendation.deskripsi


            Glide.with(binding.ivPicture.context)
                .load(recommendation.picture_url)
                .into(binding.ivPicture)

            binding.root.setOnClickListener {
                onClick(recommendation)
            }
        }
    }
}
