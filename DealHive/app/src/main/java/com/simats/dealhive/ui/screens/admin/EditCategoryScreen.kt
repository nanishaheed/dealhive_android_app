package com.simats.dealhive.ui.screens.admin

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.simats.dealhive.LocalIsDarkMode
import com.simats.dealhive.data.api.RetrofitClient
import com.simats.dealhive.data.model.CreateCategoryRequest
import com.simats.dealhive.ui.components.DealButton
import com.simats.dealhive.ui.components.DealHeader
import com.simats.dealhive.ui.theme.Primary
import com.simats.dealhive.ui.theme.Spacing
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

@Composable
fun EditCategoryScreen(
    categoryId: Int,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val isDarkMode = LocalIsDarkMode.current
    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onBackground
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textMutedColor = MaterialTheme.colorScheme.outline

    // Form fields
    var name by remember { mutableStateOf("") }
    var slug by remember { mutableStateOf("") }
    var image by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }
    
    // Load category data
    LaunchedEffect(categoryId) {
        try {
            val response = RetrofitClient.phpApiService.getCategories()
            val category = response.categories.find { it.id == categoryId }
            if (category != null) {
                name = category.name
                slug = category.slug
                image = category.image ?: ""
            } else {
                Toast.makeText(context, "Category not found", Toast.LENGTH_SHORT).show()
                onNavigateBack()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to load category", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { 
            coroutineScope.launch {
                isUploading = true
                try {
                    val inputStream = context.contentResolver.openInputStream(it)
                    val bytes = inputStream?.readBytes() ?: byteArrayOf()
                    inputStream?.close()
                    
                    val requestBody = bytes.toRequestBody("image/*".toMediaTypeOrNull())
                    val filename = "category_${System.currentTimeMillis()}.jpg"
                    val part = MultipartBody.Part.createFormData("image", filename, requestBody)
                    
                    val response = RetrofitClient.phpApiService.uploadImage(part)
                    image = response.url
                    Toast.makeText(context, "Image uploaded!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    isUploading = false
                }
            }
        }
    }
    
    fun updateCategory() {
        if (name.isBlank() || slug.isBlank()) {
            Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }
        
        coroutineScope.launch {
            isSubmitting = true
            try {
                val request = CreateCategoryRequest(
                    name = name,
                    slug = slug,
                    image = image.ifBlank { null }
                )
                RetrofitClient.phpApiService.updateCategory(categoryId, request)
                Toast.makeText(context, "Category updated!", Toast.LENGTH_SHORT).show()
                onNavigateBack()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to update: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isSubmitting = false
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        DealHeader(
            title = "Edit Category",
            showBack = true,
            onBackClick = onNavigateBack
        )
        
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(Spacing.xl)
            ) {
                // Image Upload Section
                Text("Category Image", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = textColor)
                Spacer(modifier = Modifier.Companion.height(Spacing.sm))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(surfaceVariantColor)
                        .clickable { if (!isUploading) imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (isUploading) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Primary)
                            Spacer(modifier = Modifier.Companion.height(Spacing.sm))
                            Text("Uploading...", color = textSecondaryColor)
                        }
                    } else if (image.isNotBlank()) {
                        AsyncImage(
                            model = image,
                            contentDescription = "Category image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Add, "Add Image", tint = Primary, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.Companion.height(Spacing.sm))
                            Text("Tap to upload image", color = textSecondaryColor)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.Companion.height(Spacing.xl))
                
                // Name
                Text("Category Name *", color = textSecondaryColor, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.Companion.height(Spacing.xs))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("e.g., Smartphones") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor, unfocusedTextColor = textColor,
                        focusedBorderColor = Primary, unfocusedBorderColor = textMutedColor
                    )
                )
                
                Spacer(modifier = Modifier.Companion.height(Spacing.lg))
                
                // Slug
                Text("URL Slug *", color = textSecondaryColor, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.Companion.height(Spacing.xs))
                OutlinedTextField(
                    value = slug,
                    onValueChange = { slug = it },
                    placeholder = { Text("e.g., smartphones") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor, unfocusedTextColor = textColor,
                        focusedBorderColor = Primary, unfocusedBorderColor = textMutedColor
                    )
                )
                Text("Used in URLs for this category.", style = MaterialTheme.typography.bodySmall, color = textMutedColor, modifier = Modifier.padding(top = Spacing.xs))
                
                Spacer(modifier = Modifier.Companion.height(Spacing.xxl))

                DealButton(
                    text = if (isSubmitting) "Updating..." else "Update Category",
                    onClick = { updateCategory() },
                    enabled = !isSubmitting && name.isNotBlank() && slug.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}
