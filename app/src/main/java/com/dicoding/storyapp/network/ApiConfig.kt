package com.dicoding.storyapp.network

import com.dicoding.storyapp.BuildConfig
import com.dicoding.storyapp.data.UserConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiConfig {
    fun createApiService(userConfig: UserConfig): ApiService {
        val loggingInterceptor = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
        } else {
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.NONE
            }
        }

        val authInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val token = runBlocking { userConfig.getUserSession().first().token }
            val newRequest = originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
            chain.proceed(newRequest)
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(ApiService::class.java)
    }
}
