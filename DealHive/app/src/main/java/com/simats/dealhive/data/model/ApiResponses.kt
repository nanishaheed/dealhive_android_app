package com.simats.dealhive.data.model

import com.google.gson.annotations.SerializedName

/**
 * API Response wrappers for PHP backend
 */

// Products API Response
data class ProductsResponse(
    @SerializedName("products")
    val products: List<Product>,
    @SerializedName("count")
    val count: Int,
    @SerializedName("total")
    val total: Int? = null
)

// Create Product Request (for saving scraped products)
data class CreateProductRequest(
    @SerializedName("source")
    val source: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("price")
    val price: Int,
    @SerializedName("original_price")
    val originalPrice: Int? = null,
    @SerializedName("rating")
    val rating: Float? = null,
    @SerializedName("reviews")
    val reviews: String? = null,
    @SerializedName("image")
    val image: String? = null,
    @SerializedName("url")
    val url: String,
    @SerializedName("ar_model")
    val arModel: String? = null,
    @SerializedName("specs")
    val specs: Map<String, String>? = null,
    @SerializedName("key_features")
    val keyFeatures: List<Map<String, String>>? = null,
    @SerializedName("category_id")
    val categoryId: Int? = null
)

// Create Category Request
data class CreateCategoryRequest(
    @SerializedName("name")
    val name: String,
    @SerializedName("slug")
    val slug: String,
    @SerializedName("image")
    val image: String? = null
)

// Categories API Response
data class CategoriesResponse(
    @SerializedName("categories")
    val categories: List<CategoryDto>,
    @SerializedName("count")
    val count: Int
)

data class CategoryDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("slug")
    val slug: String,
    @SerializedName("image")
    val image: String?,
    @SerializedName("product_count")
    val productCount: Int
)

// Saved Items API Response
data class SavedItemsResponse(
    @SerializedName("saved_items")
    val savedItems: List<SavedItemDto>,
    @SerializedName("count")
    val count: Int
)

data class SavedItemDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("product_id")
    val productId: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("price")
    val price: Int,
    @SerializedName("original_price")
    val originalPrice: Int?,
    @SerializedName("rating")
    val rating: Float?,
    @SerializedName("image")
    val image: String?,
    @SerializedName("source")
    val source: String?,
    @SerializedName("url")
    val url: String?,
    @SerializedName("created_at")
    val createdAt: String?
)

// Save Item Request
data class SaveItemRequest(
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("product_id")
    val productId: Int
)

// Generic message response
data class MessageResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("id")
    val id: Int? = null
)

// User Login/Register
data class LoginRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)

data class RegisterRequest(
    @SerializedName("name")
    val name: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("phone")
    val phone: String? = null
)

data class UserDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("phone")
    val phone: String?,
    @SerializedName("avatar")
    val avatar: String?,
    @SerializedName("role")
    val role: String? = "user",
    @SerializedName("created_at")
    val createdAt: String?
)

data class AuthResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("user")
    val user: UserDto
)

// Users List Response (for admin)
data class UsersListResponse(
    @SerializedName("users")
    val users: List<UserDto>,
    @SerializedName("count")
    val count: Int
)

// Comparison History API Response
data class ComparisonHistoryResponse(
    @SerializedName("comparisons")
    val comparisons: List<ComparisonDto>,
    @SerializedName("count")
    val count: Int
)

data class ComparisonDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("product1_title")
    val product1Title: String,
    @SerializedName("product1_image")
    val product1Image: String?,
    @SerializedName("product2_title")
    val product2Title: String,
    @SerializedName("product2_image")
    val product2Image: String?,
    @SerializedName("ai_verdict")
    val aiVerdict: String?,
    @SerializedName("created_at")
    val createdAt: String?
)

// Save Comparison Request
data class SaveComparisonRequest(
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("product1_id")
    val product1Id: Int,
    @SerializedName("product2_id")
    val product2Id: Int,
    @SerializedName("winner_id")
    val winnerId: Int? = null,
    @SerializedName("ai_verdict")
    val aiVerdict: String? = null
)

// Image Upload Response
data class ImageUploadResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("url")
    val url: String,
    @SerializedName("filename")
    val filename: String
)

// Forgot Password Request
data class ForgotPasswordRequest(
    @SerializedName("email")
    val email: String
)

// Verify OTP Request
data class VerifyOtpRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("otp")
    val otp: String
)

// Reset Password Request
data class ResetPasswordRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("new_password")
    val newPassword: String
)

// OTP Response
data class OtpResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("debug")
    val debug: String? = null
)

// Model Upload Response (for .usdz files from Node.js backend)
data class ModelUploadResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("url")
    val url: String,
    @SerializedName("filename")
    val filename: String,
    @SerializedName("originalName")
    val originalName: String? = null,
    @SerializedName("size")
    val size: Long? = null
)
