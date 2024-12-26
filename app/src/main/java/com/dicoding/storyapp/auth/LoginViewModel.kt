package com.dicoding.storyapp.auth

import androidx.lifecycle.ViewModel
import com.dicoding.storyapp.repo.UserRepository

class LoginViewModel(private val repository: UserRepository) : ViewModel() {

    fun login(email: String, password: String) = repository.login(email, password)
}
