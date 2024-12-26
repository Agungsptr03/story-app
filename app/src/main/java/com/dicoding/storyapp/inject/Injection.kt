package com.dicoding.storyapp.inject

import android.content.Context
import com.dicoding.storyapp.data.UserConfig
import com.dicoding.storyapp.data.dataStore
import com.dicoding.storyapp.network.ApiConfig
import com.dicoding.storyapp.repo.StoryRepository
import com.dicoding.storyapp.repo.UserRepository

class Injection {
    companion object {
        fun provideUserRepository(context: Context): UserRepository {
            val pref = UserConfig.getInstance(context.dataStore)
            val apiService = ApiConfig.createApiService(pref)
            return UserRepository.getInstance(pref, apiService)
        }

        fun provideStoryRepository(context: Context): StoryRepository {
            val pref = UserConfig.getInstance(context.dataStore)
            val apiService = ApiConfig.createApiService(pref)
            return StoryRepository.getInstance(apiService)
        }
    }
}
