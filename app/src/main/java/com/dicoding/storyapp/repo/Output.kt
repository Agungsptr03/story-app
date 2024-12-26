package com.dicoding.storyapp.repo

sealed class Output<out R> private constructor() {
    data class Success<out T>(val data: T) : Output<T>()
    data class Error(val error: String) : Output<Nothing>()
    data object Loading : Output<Nothing>()
}
