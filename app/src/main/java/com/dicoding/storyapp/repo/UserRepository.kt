package com.dicoding.storyapp.repo

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.dicoding.storyapp.data.LoginResponse
import com.dicoding.storyapp.data.RegisterResponse
import com.dicoding.storyapp.data.UserConfig
import com.dicoding.storyapp.network.ApiService
import com.google.gson.Gson
import retrofit2.HttpException

class UserRepository private constructor(
    private val userConfig: UserConfig,
    private val apiService: ApiService
) {
    fun register(name: String, email: String, password: String): LiveData<Output<RegisterResponse>> = liveData {
        emit(Output.Loading)
        try {
            val response = apiService.register(name, email, password)
            emit(Output.Success(response))
        } catch (e: HttpException) {
            Log.e("postRegister", "HTTP Exception: ${e.message}")
            try {
                val errorResponse = e.response()?.errorBody()?.string()
                val gson = Gson()
                val parsedError = gson.fromJson(errorResponse, RegisterResponse::class.java)
                emit(Output.Success(parsedError))
            } catch (parseException: Exception) {
                Log.e("postRegister", "Error parsing response: ${parseException.message}")
                emit(Output.Error("Error parsing HTTP exception response"))
            }
        } catch (e: Exception) {
            Log.e("postRegister", "General Exception: ${e.message}")
            emit(Output.Error(e.message.toString()))
        }
    }

    fun login(email: String, password: String): LiveData<Output<LoginResponse>> = liveData {
        emit(Output.Loading)
        try {
            val response = apiService.login(email, password)
            emit(Output.Success(response))
        } catch (e: HttpException) {
            Log.e("postLogin", "HTTP Exception: ${e.message}")
            try {
                val errorResponse = e.response()?.errorBody()?.string()
                val gson = Gson()
                val parsedError = gson.fromJson(errorResponse, LoginResponse::class.java)
                emit(Output.Success(parsedError))
            } catch (parseException: Exception) {
                Log.e("postLogin", "Error parsing response: ${parseException.message}")
                emit(Output.Error("Error parsing HTTP exception response"))
            }
        } catch (e: Exception) {
            Log.e("postLogin", "General Exception: ${e.message}")
            emit(Output.Error(e.message.toString()))
        }
    }

    companion object {
        @Volatile
        private var instance: UserRepository? = null

        fun getInstance(
            userConfig: UserConfig,
            apiService: ApiService
        ): UserRepository =
            instance ?: synchronized(this) {
                instance ?: UserRepository(userConfig, apiService).also { instance = it }
            }
    }
}
