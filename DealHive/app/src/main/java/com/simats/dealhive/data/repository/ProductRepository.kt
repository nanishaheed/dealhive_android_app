package com.simats.dealhive.data.repository

import com.simats.dealhive.data.api.RetrofitClient
import com.simats.dealhive.data.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProductRepository {
    
    private val apiService = RetrofitClient.apiService
    
    suspend fun scrapeProduct(url: String): Result<Product> {
        return withContext(Dispatchers.IO) {
            try {
                val product = apiService.scrapeProduct(url)
                Result.success(product)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun compareProducts(url1: String, url2: String): Result<Pair<Product, Product>> {
        return withContext(Dispatchers.IO) {
            try {
                val product1 = apiService.scrapeProduct(url1)
                val product2 = apiService.scrapeProduct(url2)
                Result.success(Pair(product1, product2))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
