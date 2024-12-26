package com.dicoding.storyapp.model

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dicoding.storyapp.repo.StoryRepository
import java.io.File

class ViewModelMain(
    private val storyRepository: StoryRepository
) : ViewModel() {
    private var _currentImageUri = MutableLiveData<Uri?>()
    val currentImageUri: MutableLiveData<Uri?> = _currentImageUri

    fun getAllStory() = storyRepository.getAllStory()

    fun uploadStory(imageFile: File, desc: String) = storyRepository.uploadStory(imageFile, desc)

    fun setCurrentImageUri(uri: Uri?) {
        _currentImageUri.value = uri
    }
}
