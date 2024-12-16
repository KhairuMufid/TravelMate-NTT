package com.example.travelmatentt.fragment

import androidx.recyclerview.widget.DiffUtil


class WishlistDiffCallback : DiffUtil.ItemCallback<WishlistItem>() {
    override fun areItemsTheSame(oldItem: WishlistItem, newItem: WishlistItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: WishlistItem, newItem: WishlistItem): Boolean {
        return oldItem == newItem
    }
}
