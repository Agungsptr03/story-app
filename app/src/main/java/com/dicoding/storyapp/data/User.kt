package com.dicoding.storyapp.data

data class User(
    val email: String,
    val token: String,
    val isLogin: Boolean = false
)
