package com.dicoding.storyapp.model

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dicoding.storyapp.auth.LoginViewModel
import com.dicoding.storyapp.auth.RegisterViewModel
import com.dicoding.storyapp.inject.Injection
import com.dicoding.storyapp.repo.StoryRepository
import com.dicoding.storyapp.repo.UserRepository

class ViewModelFactory(
    private val userRepo: UserRepository,
    private val storyRepo: StoryRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ViewModelMain::class.java) -> {
                ViewModelMain(storyRepo) as T
            }
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(userRepo) as T
            }
            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> {
                RegisterViewModel(userRepo) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        @Volatile
        private var instance: ViewModelFactory? = null

        @JvmStatic
        fun getInstance(context: Context): ViewModelFactory {
            return instance ?: synchronized(this) {
                instance ?: ViewModelFactory(
                    Injection.provideUserRepository(context),
                    Injection.provideStoryRepository(context)
                ).also { instance = it }
            }
        }
    }
}
