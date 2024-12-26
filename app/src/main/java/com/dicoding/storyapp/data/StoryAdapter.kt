package com.dicoding.storyapp.data

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.storyapp.DetailActivity
import com.dicoding.storyapp.R
import com.dicoding.storyapp.databinding.ItemStoryBinding

class StoryAdapter : ListAdapter<ListStoryItem, StoryAdapter.StoryViewHolder>(DIFF_CALLBACK) {

    class StoryViewHolder(private val binding: ItemStoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindStory(story: ListStoryItem) {
            Glide.with(binding.root.context)
                .load(story.photoUrl)
                .placeholder(R.drawable.ic_image)
                .error(R.drawable.ic_image)
                .into(binding.ivItemPhoto)
            binding.ivItemName.text = story.name
            binding.ivItemDescription.text = story.description

            itemView.setOnClickListener {
                val detailIntent = Intent(itemView.context, DetailActivity::class.java)
                detailIntent.putExtra(STORY_KEY, story)

                val transitionOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    itemView.context as Activity,
                    Pair(binding.ivItemPhoto, "image"),
                    Pair(binding.ivItemName, "title"),
                    Pair(binding.ivItemDescription, "desc"),
                )
                itemView.context.startActivity(detailIntent, transitionOptions.toBundle())
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val storyItem = getItem(position)
        holder.bindStory(storyItem)
    }

    companion object {
        const val STORY_KEY = "story"
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ListStoryItem>() {
            override fun areItemsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}
