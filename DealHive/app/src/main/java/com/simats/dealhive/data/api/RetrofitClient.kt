package com.simats.dealhive.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // Backend scraping service (Node.js) - Change this IP when your network changes
    const val NODE_BASE_URL = "http://192.168.29.243:3000/"
    
    // PHP backend (XAMPP) - Change this IP when your network changes
    const val PHP_BASE_URL = "http://14.139.187.229:8081/oct/spic_730/dealhive/"
    
    // Helper function to get full model URL from relative path
    fun getModelUrl(relativePath: String): String {
        return if (relativePath.startsWith("http")) {
            relativePath // Already a full URL
        } else {
            NODE_BASE_URL.trimEnd('/') + relativePath
        }
    }
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(90, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(90, TimeUnit.SECONDS)
        .build()
    
    // Retrofit for scraping service (existing)
    private val scrapeRetrofit = Retrofit.Builder()
        .baseUrl(NODE_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    // Retrofit for PHP backend
    private val phpRetrofit = Retrofit.Builder()
        .baseUrl(PHP_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    // Existing scrape API service
    val apiService: ApiService = scrapeRetrofit.create(ApiService::class.java)
    
    // PHP backend API service
    val phpApiService: PhpApiService = phpRetrofit.create(PhpApiService::class.java)
}

