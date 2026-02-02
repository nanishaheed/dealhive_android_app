package com.simats.dealhive.data.api

import com.simats.dealhive.data.model.Product
import com.simats.dealhive.data.model.ModelUploadResponse
import okhttp3.MultipartBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {
    
    @GET("scrape")
    suspend fun scrapeProduct(@Query("url") url: String): Product
    
    @Multipart
    @POST("upload/model")
    suspend fun uploadModel(@Part model: MultipartBody.Part): ModelUploadResponse
}
