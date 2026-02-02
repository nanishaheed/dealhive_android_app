package com.simats.dealhive.data.api

import com.simats.dealhive.data.model.AuthResponse
import com.simats.dealhive.data.model.CategoriesResponse
import com.simats.dealhive.data.model.CategoryDto
import com.simats.dealhive.data.model.ComparisonHistoryResponse
import com.simats.dealhive.data.model.CreateCategoryRequest
import com.simats.dealhive.data.model.CreateProductRequest
import com.simats.dealhive.data.model.ForgotPasswordRequest
import com.simats.dealhive.data.model.ImageUploadResponse
import com.simats.dealhive.data.model.LoginRequest
import com.simats.dealhive.data.model.MessageResponse
import com.simats.dealhive.data.model.OtpResponse
import com.simats.dealhive.data.model.Product
import com.simats.dealhive.data.model.ProductsResponse
import com.simats.dealhive.data.model.RegisterRequest
import com.simats.dealhive.data.model.ResetPasswordRequest
import com.simats.dealhive.data.model.SaveComparisonRequest
import com.simats.dealhive.data.model.SaveItemRequest
import com.simats.dealhive.data.model.SavedItemsResponse
import com.simats.dealhive.data.model.UserDto
import com.simats.dealhive.data.model.UsersListResponse
import com.simats.dealhive.data.model.VerifyOtpRequest
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Query

/**
 * API Service interface for PHP backend endpoints
 */
interface PhpApiService {
    
    // ==================== Products ====================
    
    @GET("products.php")
    suspend fun getProducts(
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): ProductsResponse
    
    @GET("products.php")
    suspend fun getProductById(@Query("id") id: Int): Product
    
    @GET("products.php")
    suspend fun getProductsByCategory(@Query("category") category: String): ProductsResponse
    
    @POST("products.php")
    suspend fun createProduct(@Body product: CreateProductRequest): MessageResponse
    
    @DELETE("products.php")
    suspend fun deleteProduct(@Query("id") productId: Int): MessageResponse
    
    @PUT("products.php")
    suspend fun updateProduct(
        @Query("id") productId: Int,
        @Body product: CreateProductRequest
    ): MessageResponse
    
    // ==================== Categories ====================
    
    @GET("categories.php")
    suspend fun getCategories(): CategoriesResponse
    
    @GET("categories.php")
    suspend fun getCategoryBySlug(@Query("slug") slug: String): CategoryDto
    
    @POST("categories.php")
    suspend fun createCategory(@Body category: CreateCategoryRequest): MessageResponse
    
    @DELETE("categories.php")
    suspend fun deleteCategory(@Query("id") categoryId: Int): MessageResponse
    
    @PUT("categories.php")
    suspend fun updateCategory(
        @Query("id") categoryId: Int,
        @Body category: CreateCategoryRequest
    ): MessageResponse
    
    // ==================== Saved Items ====================
    
    @GET("saved.php")
    suspend fun getSavedItems(@Query("user_id") userId: Int): SavedItemsResponse
    
    @POST("saved.php")
    suspend fun saveItem(@Body request: SaveItemRequest): MessageResponse
    
    @DELETE("saved.php")
    suspend fun removeSavedItem(
        @Query("user_id") userId: Int,
        @Query("product_id") productId: Int
    ): MessageResponse
    
    // ==================== Users ====================
    
    @POST("users.php?action=login")
    suspend fun login(@Body request: LoginRequest): AuthResponse
    
    @POST("users.php?action=register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse
    
    @GET("users.php")
    suspend fun getUserProfile(@Query("id") userId: Int): UserDto
    
    @GET("users.php?all=true")
    suspend fun getAllUsers(): UsersListResponse
    
    @PUT("users.php")
    suspend fun updateUserProfile(
        @Query("id") userId: Int,
        @Body updates: Map<String, String>
    ): MessageResponse
    
    @DELETE("users.php")
    suspend fun deleteUser(@Query("id") userId: Int): MessageResponse
    
    // ==================== Comparisons ====================
    
    @GET("comparisons.php")
    suspend fun getComparisonHistory(@Query("user_id") userId: Int): ComparisonHistoryResponse
    
    @POST("comparisons.php")
    suspend fun saveComparison(@Body request: SaveComparisonRequest): MessageResponse
    
    // ==================== Image Upload ====================
    
    @Multipart
    @POST("upload.php")
    suspend fun uploadImage(@Part image: MultipartBody.Part): ImageUploadResponse
    
    // ==================== Forgot Password ====================
    
    @POST("forgot_password.php")
    suspend fun sendPasswordResetOtp(@Body request: ForgotPasswordRequest): OtpResponse
    
    @POST("forgot_password.php?action=verify")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): OtpResponse
    
    @POST("forgot_password.php?action=reset")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): MessageResponse
}



