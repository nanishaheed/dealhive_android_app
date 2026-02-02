package com.simats.dealhive.ui.screens.profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.simats.dealhive.LocalIsDarkMode
import com.simats.dealhive.data.api.RetrofitClient
import com.simats.dealhive.data.repository.AuthRepository
import com.simats.dealhive.ui.components.DealButton
import com.simats.dealhive.ui.components.DealHeader
import com.simats.dealhive.ui.components.DealInput
import com.simats.dealhive.ui.theme.Primary
import com.simats.dealhive.ui.theme.Spacing
import com.simats.dealhive.ui.theme.White
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val authRepository = remember { AuthRepository.Companion.getInstance(context) }
    
    val isDarkMode = LocalIsDarkMode.current
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    val errorColor = MaterialTheme.colorScheme.error
    
    // Load current user data from session
    var fullName by remember { mutableStateOf(authRepository.getCurrentUserName() ?: "") }
    var email by remember { mutableStateOf(authRepository.getCurrentUserEmail() ?: "") }
    var phone by remember { mutableStateOf(authRepository.getCurrentUserPhone() ?: "") }
    var avatarUrl by remember { mutableStateOf(authRepository.getCurrentUserAvatar() ?: "") }
    
    var isLoading by remember { mutableStateOf(false) }
    var isUploadingPhoto by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Validation helper functions
    fun isValidName(name: String): Boolean {
        return name.all { it.isLetter() || it.isWhitespace() }
    }
    
    fun isValidPhone(phoneNum: String): Boolean {
        return phoneNum.isEmpty() || (phoneNum.length == 10 && phoneNum.all { it.isDigit() })
    }
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { 
            coroutineScope.launch {
                isUploadingPhoto = true
                try {
                    val inputStream = context.contentResolver.openInputStream(it)
                    val bytes = inputStream?.readBytes() ?: byteArrayOf()
                    inputStream?.close()
                    
                    val requestBody = bytes.toRequestBody("image/*".toMediaTypeOrNull())
                    val filename = "avatar_${System.currentTimeMillis()}.jpg"
                    val part = MultipartBody.Part.createFormData("image", filename, requestBody)
                    
                    val response = RetrofitClient.phpApiService.uploadImage(part)
                    avatarUrl = response.url
                    Toast.makeText(context, "Photo uploaded!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    isUploadingPhoto = false
                }
            }
        }
    }
    
    fun saveChanges() {
        if (fullName.isBlank()) {
            errorMessage = "Name cannot be empty"
            return
        }
        if (!isValidName(fullName)) {
            errorMessage = "Name must contain only letters (no numbers or special characters)"
            return
        }
        if (phone.isNotBlank() && !isValidPhone(phone)) {
            errorMessage = "Mobile number must be exactly 10 digits"
            return
        }
        
        errorMessage = null
        isLoading = true
        
        coroutineScope.launch {
            val result = authRepository.updateProfile(
                name = fullName.trim(),
                phone = phone.trim().ifBlank { null },
                avatar = avatarUrl.ifBlank { null }
            )
            isLoading = false
            
            result.fold(
                onSuccess = {
                    Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                    onNavigateBack()
                },
                onFailure = { error ->
                    errorMessage = error.message ?: "Failed to update profile"
                }
            )
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        DealHeader(
            title = "Edit Profile",
            showBack = true,
            onBackClick = onNavigateBack
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.Companion.height(Spacing.lg))
            
            // Profile Picture with Upload
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier.clickable { 
                    if (!isUploadingPhoto) {
                        imagePickerLauncher.launch("image/*")
                    }
                }
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isUploadingPhoto) {
                        CircularProgressIndicator(
                            color = Primary,
                            modifier = Modifier.size(40.dp)
                        )
                    } else if (avatarUrl.isNotBlank()) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile Picture",
                            tint = Primary,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }
                
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Change Photo",
                        tint = White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.Companion.height(Spacing.md))
            
            Text(
                text = if (isUploadingPhoto) "Uploading..." else "Tap to change photo",
                style = MaterialTheme.typography.bodySmall,
                color = if (isUploadingPhoto) Primary else textSecondaryColor
            )
            
            Spacer(modifier = Modifier.Companion.height(Spacing.xxl))
            
            // Error Message
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = errorColor,
                    modifier = Modifier.padding(bottom = Spacing.md)
                )
            }
            
            // Form
            DealInput(
                value = fullName,
                onValueChange = { newValue ->
                    // Only allow letters and spaces
                    if (newValue.all { it.isLetter() || it.isWhitespace() }) {
                        fullName = newValue
                        errorMessage = null
                    }
                },
                label = "Full Name",
                placeholder = "Enter your name"
            )

            DealInput(
                value = email,
                onValueChange = { /* Email is read-only */ },
                label = "Email Address",
                placeholder = "Enter your email",
                keyboardType = KeyboardType.Email
            )

            DealInput(
                value = phone,
                onValueChange = { newValue ->
                    // Only allow digits and max 10 characters
                    if (newValue.all { it.isDigit() } && newValue.length <= 10) {
                        phone = newValue
                        errorMessage = null
                    }
                },
                label = "Phone Number",
                placeholder = "Enter 10-digit mobile number",
                keyboardType = KeyboardType.Phone
            )
            
            Spacer(modifier = Modifier.Companion.height(Spacing.xl))
            
            // Save Button with Loading State
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Primary,
                        modifier = Modifier.size(48.dp)
                    )
                } else {
                    DealButton(
                        text = "Save Changes",
                        onClick = { saveChanges() },
                        enabled = fullName.isNotBlank() && !isUploadingPhoto
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
