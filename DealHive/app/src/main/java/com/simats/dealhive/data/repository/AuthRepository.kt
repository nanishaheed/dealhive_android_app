package com.simats.dealhive.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.simats.dealhive.data.api.RetrofitClient
import com.simats.dealhive.data.model.AuthResponse
import com.simats.dealhive.data.model.LoginRequest
import com.simats.dealhive.data.model.RegisterRequest
import com.simats.dealhive.data.model.UserDto
import com.simats.dealhive.data.model.MessageResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

/**
 * Repository for handling user authentication and session management
 */
class AuthRepository(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREFS_NAME = "dealhive_auth"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_PHONE = "user_phone"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_USER_AVATAR = "user_avatar"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_IS_SUBSCRIBED = "is_subscribed"
        
        @Volatile
        private var instance: AuthRepository? = null
        
        fun getInstance(context: Context): AuthRepository {
            return instance ?: synchronized(this) {
                instance ?: AuthRepository(context.applicationContext).also { instance = it }
            }
        }
    }
    
    /**
     * Login user with email and password
     */
    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = LoginRequest(email, password)
                val response = RetrofitClient.phpApiService.login(request)
                
                // Save user session
                saveUserSession(response.user)
                
                Log.d("AuthRepository", "Login successful for user: ${response.user.email}")
                Result.success(response)
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("AuthRepository", "Login HTTP error: ${e.code()} - $errorBody")
                Result.failure(Exception(parseErrorMessage(errorBody) ?: "Invalid email or password"))
            } catch (e: Exception) {
                Log.e("AuthRepository", "Login error: ${e.message}", e)
                Result.failure(Exception("Connection failed. Please check your network."))
            }
        }
    }
    
    /**
     * Register new user
     */
    suspend fun register(name: String, email: String, password: String, phone: String? = null): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = RegisterRequest(name, email, password, phone)
                val response = RetrofitClient.phpApiService.register(request)
                
                // Save user session after successful registration
                saveUserSession(response.user)
                
                Log.d("AuthRepository", "Registration successful for user: ${response.user.email}")
                Result.success(response)
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("AuthRepository", "Register HTTP error: ${e.code()} - $errorBody")
                Result.failure(Exception(parseErrorMessage(errorBody) ?: "Registration failed"))
            } catch (e: Exception) {
                Log.e("AuthRepository", "Register error: ${e.message}", e)
                Result.failure(Exception("Connection failed. Please check your network."))
            }
        }
    }
    
    /**
     * Update user profile
     */
    suspend fun updateProfile(name: String, phone: String?, avatar: String? = null): Result<MessageResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = getCurrentUserId()
                if (userId == -1) {
                    return@withContext Result.failure(Exception("User not logged in"))
                }
                
                val updates = mutableMapOf<String, String>()
                updates["name"] = name
                if (!phone.isNullOrBlank()) {
                    updates["phone"] = phone
                }
                if (!avatar.isNullOrBlank()) {
                    updates["avatar"] = avatar
                }
                
                val response = RetrofitClient.phpApiService.updateUserProfile(userId, updates)
                
                // Update local session
                updateLocalSession(name, phone, avatar)
                
                Log.d("AuthRepository", "Profile updated successfully")
                Result.success(response)
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("AuthRepository", "Update profile HTTP error: ${e.code()} - $errorBody")
                Result.failure(Exception(parseErrorMessage(errorBody) ?: "Failed to update profile"))
            } catch (e: Exception) {
                Log.e("AuthRepository", "Update profile error: ${e.message}", e)
                Result.failure(Exception("Connection failed. Please check your network."))
            }
        }
    }
    
    /**
     * Save user session to SharedPreferences
     */
    private fun saveUserSession(user: UserDto) {
        prefs.edit().apply {
            putInt(KEY_USER_ID, user.id)
            putString(KEY_USER_NAME, user.name)
            putString(KEY_USER_EMAIL, user.email)
            putString(KEY_USER_PHONE, user.phone)
            putString(KEY_USER_ROLE, user.role ?: "user")
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }
    
    /**
     * Update local session after profile update
     */
    private fun updateLocalSession(name: String, phone: String?, avatar: String? = null) {
        prefs.edit().apply {
            putString(KEY_USER_NAME, name)
            if (!phone.isNullOrBlank()) {
                putString(KEY_USER_PHONE, phone)
            }
            if (!avatar.isNullOrBlank()) {
                putString(KEY_USER_AVATAR, avatar)
            }
            apply()
        }
    }
    
    /**
     * Logout user - clear session
     */
    fun logout() {
        prefs.edit().clear().apply()
        Log.d("AuthRepository", "User logged out")
    }
    
    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }
    
    /**
     * Get current user ID
     */
    fun getCurrentUserId(): Int {
        return prefs.getInt(KEY_USER_ID, -1)
    }
    
    /**
     * Get current user name
     */
    fun getCurrentUserName(): String? {
        return prefs.getString(KEY_USER_NAME, null)
    }
    
    /**
     * Get current user email
     */
    fun getCurrentUserEmail(): String? {
        return prefs.getString(KEY_USER_EMAIL, null)
    }
    
    /**
     * Get current user phone
     */
    fun getCurrentUserPhone(): String? {
        return prefs.getString(KEY_USER_PHONE, null)
    }
    
    /**
     * Get current user avatar URL
     */
    fun getCurrentUserAvatar(): String? {
        return prefs.getString(KEY_USER_AVATAR, null)
    }
    
    /**
     * Get current user role
     */
    fun getCurrentUserRole(): String {
        return prefs.getString(KEY_USER_ROLE, "user") ?: "user"
    }
    
    /**
     * Check if current user is admin
     */
    fun isAdmin(): Boolean {
        return getCurrentUserRole() == "admin"
    }
    
    /**
     * Check if user has active subscription
     */
    fun isSubscribed(): Boolean {
        return prefs.getBoolean(KEY_IS_SUBSCRIBED, false)
    }
    
    /**
     * Set user subscription status
     */
    fun setSubscribed(subscribed: Boolean) {
        prefs.edit().putBoolean(KEY_IS_SUBSCRIBED, subscribed).apply()
    }
    
    /**
     * Delete user account from server and logout locally
     */
    suspend fun deleteAccount(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = getCurrentUserId()
                if (userId != null) {
                    RetrofitClient.phpApiService.deleteUser(userId)
                }
                logout()
                Result.success(Unit)
            } catch (e: Exception) {
                logout() // Still logout locally even if API fails
                Result.failure(e)
            }
        }
    }
    
    /**
     * Parse error message from JSON response
     */
    private fun parseErrorMessage(errorBody: String?): String? {
        if (errorBody == null) return null
        return try {
            // Simple JSON parsing for {"error": "message"}
            val regex = """"error"\s*:\s*"([^"]+)"""".toRegex()
            regex.find(errorBody)?.groupValues?.get(1)
        } catch (e: Exception) {
            null
        }
    }
}

