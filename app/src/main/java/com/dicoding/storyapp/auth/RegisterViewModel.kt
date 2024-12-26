package com.dicoding.storyapp.auth

import androidx.lifecycle.ViewModel
import com.dicoding.storyapp.repo.UserRepository

class RegisterViewModel(private val repository: UserRepository) : ViewModel() {

    fun register(name: String, email: String, password: String) = repository.register(name, email, password)
}
