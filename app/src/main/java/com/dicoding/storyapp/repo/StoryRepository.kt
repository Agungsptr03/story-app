package com.dicoding.storyapp.repo

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.dicoding.storyapp.data.StoryResponse
import com.dicoding.storyapp.data.UploadResponse
import com.dicoding.storyapp.network.ApiService
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File

class StoryRepository private constructor(
    private val apiService: ApiService
) {

    fun getAllStory(): LiveData<Output<StoryResponse>> = liveData {
        emit(Output.Loading)
        try {
            val response = apiService.getAllStory()
            emit(Output.Success(response))
        } catch (e: HttpException) {
            Log.e("getAllStories", "HTTP Exception: ${e.message}")
            emit(handleHttpError(e))
        } catch (e: Exception) {
            Log.e("getAllStories", "General Exception: ${e.message}")
            emit(Output.Error(e.message.toString()))
        }
    }

    fun uploadStory(
        imageFile: File,
        description: String
    ): LiveData<Output<UploadResponse>> = liveData {
        emit(Output.Loading)
        val requestBody = description.toRequestBody("text/plain".toMediaType())
        val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
        val multipartBody = MultipartBody.Part.createFormData(
            "photo",
            imageFile.name,
            requestImageFile
        )
        try {
            val response = apiService.uploadStory(multipartBody, requestBody)
            emit(Output.Success(response))
        } catch (e: HttpException) {
            Log.e("uploadStory", "HTTP Exception: ${e.message}")
            emit(handleUploadError(e))
        } catch (e: Exception) {
            Log.e("uploadStory", "General Exception: ${e.message}")
            emit(Output.Error(e.message.toString()))
        }
    }

    private fun handleHttpError(exception: HttpException): Output<StoryResponse> {
        return try {
            val errorResponse = exception.response()?.errorBody()?.string()
            val gson = Gson()
            val parsedError = gson.fromJson(errorResponse, StoryResponse::class.java)
            Output.Success(parsedError)
        } catch (e: Exception) {
            Log.e("handleHttpError", "Error parsing error response: ${e.message}")
            Output.Error("Error: ${e.message}")
        }
    }

    private fun handleUploadError(exception: HttpException): Output<UploadResponse> {
        return try {
            val errorResponse = exception.response()?.errorBody()?.string()
            val gson = Gson()
            val parsedError = gson.fromJson(errorResponse, UploadResponse::class.java)
            Output.Success(parsedError)
        } catch (e: Exception) {
            Log.e("handleUploadError", "Error parsing error response: ${e.message}")
            Output.Error("Error: ${e.message}")
        }
    }

    companion object {
        @Volatile
        private var instance: StoryRepository? = null
        fun getInstance(apiService: ApiService): StoryRepository =
            instance ?: synchronized(this) {
                instance ?: StoryRepository(apiService)
            }.also { instance = it }
    }
}
