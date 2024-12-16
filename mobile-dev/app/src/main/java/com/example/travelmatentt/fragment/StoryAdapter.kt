package com.example.travelmatentt.fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.travelmatentt.R
import com.example.travelmatentt.databinding.ItemStoryBinding
class StoryAdapter : RecyclerView.Adapter<StoryAdapter.StoryViewHolder>() {
    private var stories: List<Story> = emptyList()
    fun submitList(newStories: List<Story>) {
        stories = newStories
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_story, parent, false)
        return StoryViewHolder(view)
    }
    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = stories[position]
        holder.bind(story)
    }
    override fun getItemCount(): Int = stories.size
    inner class StoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(story: Story) {
            // Bind the story data to the UI components (e.g., TextViews, ImageViews)
            // Example: itemView.findViewById<TextView>(R.id.storyContent).text = story.content
        }
    }
}

