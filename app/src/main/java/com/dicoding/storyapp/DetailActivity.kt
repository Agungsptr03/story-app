package com.dicoding.storyapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.dicoding.storyapp.data.ListStoryItem
import com.dicoding.storyapp.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        val storyItem = intent.getParcelableExtra<ListStoryItem>(STORY_KEY) ?: return

        Glide.with(this)
            .load(storyItem.photoUrl)
            .placeholder(R.drawable.ic_image)
            .error(R.drawable.ic_image)
            .into(binding.tvDetailPhoto)

        binding.tvDetailName.text = storyItem.name
        binding.tvDetailDescription.text = storyItem.description
    }

    companion object {
        const val STORY_KEY = "story"
    }
}
