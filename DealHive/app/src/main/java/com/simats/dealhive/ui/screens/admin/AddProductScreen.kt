package com.simats.dealhive.ui.screens.admin

import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.simats.dealhive.LocalIsDarkMode
import com.simats.dealhive.data.api.RetrofitClient
import com.simats.dealhive.data.model.CreateProductRequest
import com.simats.dealhive.data.model.KeyFeature
import com.simats.dealhive.data.model.CategoryDto
import com.simats.dealhive.ui.components.DealButton
import com.simats.dealhive.ui.components.DealHeader
import com.simats.dealhive.ui.theme.Primary
import com.simats.dealhive.ui.theme.Spacing
import com.simats.dealhive.ui.theme.White
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import kotlin.collections.iterator

/**
 * Maps a spec key/value to an appropriate icon name
 */
fun getIconForSpec(key: String, value: String): String {
    val text = (key + " " + value).lowercase()
    return when {
        text.contains("battery") || text.contains("playtime") || text.contains("hour") -> "Battery"
        text.contains("sound") || text.contains("audio") || text.contains("music") || 
            text.contains("dsee") || text.contains("driver") || text.contains("bass") -> "Music"
        text.contains("bluetooth") || text.contains("wireless") || text.contains("wifi") ||
            text.contains("multipoint") || text.contains("connection") -> "Bluetooth"
        text.contains("mic") || text.contains("call") || text.contains("voice") -> "Mic"
        text.contains("camera") || text.contains("mp") || text.contains("megapixel") -> "Camera"
        text.contains("display") || text.contains("screen") || text.contains("amoled") ||
            text.contains("oled") || text.contains("lcd") || text.contains("inch") -> "Display"
        text.contains("ram") || text.contains("storage") || text.contains("memory") ||
            text.contains("gb") || text.contains("tb") -> "Memory"
        text.contains("processor") || text.contains("cpu") || text.contains("chip") ||
            text.contains("snapdragon") || text.contains("helio") || text.contains("bionic") -> "Processor"
        text.contains("charging") || text.contains("usb") || text.contains("type-c") -> "Charging"
        text.contains("weight") || text.contains("dimension") || text.contains("size") -> "Phone"
        else -> "Star"
    }
}

/**
 * Gets the Icon for a given icon name string
 */
@Composable
fun getIconVector(iconName: String): ImageVector {
    return when (iconName) {
        "Battery" -> Icons.Default.BatteryFull
        "Music" -> Icons.Default.MusicNote
        "Bluetooth" -> Icons.Default.Bluetooth
        "Mic" -> Icons.Default.Mic
        "Camera" -> Icons.Default.CameraAlt
        "Display" -> Icons.Default.Tv
        "Memory" -> Icons.Default.Memory
        "Processor" -> Icons.Default.Memory
        "Charging" -> Icons.Default.BatteryFull
        "Phone" -> Icons.Default.PhoneAndroid
        else -> Icons.Default.Star
    }
}

/**
 * Generates key features from specs map
 * Takes top 4-5 most important specs and maps them to icon+label format
 */
fun generateKeyFeatures(specs: Map<String, String>?): List<KeyFeature> {
    if (specs.isNullOrEmpty()) return emptyList()
    
    // Priority keywords for selecting important specs
    val priorityKeywords = listOf(
        "battery", "playtime", "hour",
        "bluetooth", "wireless", 
        "driver", "sound", "audio",
        "mic", "call",
        "charging", "usb",
        "weight"
    )
    
    val features = mutableListOf<KeyFeature>()
    val usedKeys = mutableSetOf<String>()
    
    // First pass: add priority specs
    for (keyword in priorityKeywords) {
        if (features.size >= 4) break
        for ((key, value) in specs) {
            if (usedKeys.contains(key)) continue
            if (key.lowercase().contains(keyword) || value.lowercase().contains(keyword)) {
                val icon = getIconForSpec(key, value)
                val label = if (value.length > 30) value.take(27) + "..." else value
                features.add(KeyFeature(icon = icon, label = label))
                usedKeys.add(key)
                break
            }
        }
    }
    
    // Second pass: fill remaining slots with other specs
    for ((key, value) in specs) {
        if (features.size >= 4) break
        if (usedKeys.contains(key)) continue
        val icon = getIconForSpec(key, value)
        val label = if (value.length > 30) value.take(27) + "..." else value
        features.add(KeyFeature(icon = icon, label = label))
        usedKeys.add(key)
    }
    
    return features
}

@Composable
fun AddProductScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val isDarkMode = LocalIsDarkMode.current
    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onBackground
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textMutedColor = MaterialTheme.colorScheme.outline

    // Form fields
    var title by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var originalPrice by remember { mutableStateOf("") }
    var source by remember { mutableStateOf("amazon") }
    var url by remember { mutableStateOf("") }
    var image by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }
    
    // Specs and Key Features
    var specs by remember { mutableStateOf<Map<String, String>?>(null) }
    var keyFeatures by remember { mutableStateOf<List<KeyFeature>>(emptyList()) }
    var isFetching by remember { mutableStateOf(false) }
    
    // AR Model
    var arModel by remember { mutableStateOf("") }
    var isUploadingAr by remember { mutableStateOf(false) }
    
    // Category selection
    var categories by remember { mutableStateOf<List<CategoryDto>>(emptyList()) }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }
    var isLoadingCategories by remember { mutableStateOf(true) }
    
    // Fetch categories on load
    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.phpApiService.getCategories()
            categories = response.categories
        } catch (e: Exception) {
            Log.e("AddProduct", "Failed to load categories", e)
        } finally {
            isLoadingCategories = false
        }
    }
    
    // Function to fetch product data from scraper
    fun fetchProductData() {
        if (url.isBlank()) {
            Toast.makeText(context, "Please enter a product URL first", Toast.LENGTH_SHORT).show()
            return
        }
        
        coroutineScope.launch {
            isFetching = true
            try {
                val product = RetrofitClient.apiService.scrapeProduct(url)
                
                // Auto-fill all fields
                title = product.title
                price = product.price.toString()
                originalPrice = product.originalPrice?.toString() ?: ""
                image = product.image
                source = product.source
                specs = product.specs
                
                // Generate key features from specs
                keyFeatures = generateKeyFeatures(product.specs)
                
                Toast.makeText(context, "Product data fetched successfully!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("AddProduct", "Failed to fetch product data", e)
                Toast.makeText(context, "Failed to fetch: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                isFetching = false
            }
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
                    val filename = "image_${System.currentTimeMillis()}.jpg"
                    val part = MultipartBody.Part.createFormData("image", filename, requestBody)
                    
                    val response = RetrofitClient.phpApiService.uploadImage(part)
                    image = response.url
                    Toast.makeText(context, "Image uploaded!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Log.e("AddProduct", "Upload failed", e)
                    Toast.makeText(context, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    isUploading = false
                }
            }
        }
    }
    
    fun submitProduct() {
        if (title.isBlank() || price.isBlank() || url.isBlank()) {
            Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (selectedCategoryId == null) {
            Toast.makeText(context, "Please select a category", Toast.LENGTH_SHORT).show()
            return
        }
        
        coroutineScope.launch {
            isSubmitting = true
            try {
                // Convert KeyFeature list to Map list for JSON serialization
                val keyFeaturesMapList = keyFeatures.map { 
                    mapOf("icon" to it.icon, "label" to it.label) 
                }
                
                val request = CreateProductRequest(
                    source = source,
                    title = title,
                    price = price.toIntOrNull() ?: 0,
                    originalPrice = originalPrice.toIntOrNull(),
                    url = url,
                    image = image.ifBlank { null },
                    arModel = arModel.ifBlank { null },
                    specs = specs,
                    keyFeatures = keyFeaturesMapList.ifEmpty { null },
                    categoryId = selectedCategoryId
                )
                RetrofitClient.phpApiService.createProduct(request)
                Toast.makeText(context, "Product added successfully!", Toast.LENGTH_SHORT).show()
                onNavigateBack()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to add product: ${e.message}", Toast.LENGTH_SHORT).show()
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
            title = "Add Product",
            showBack = true,
            onBackClick = onNavigateBack
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.xl)
        ) {
            // Product URL with Fetch Button
            Text("Product URL *", color = textSecondaryColor, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.Companion.height(Spacing.xs))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    placeholder = { Text("Enter Amazon/Flipkart URL") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = textMutedColor
                    ),
                    singleLine = true
                )
                
                // Fetch Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isFetching) surfaceVariantColor else Primary)
                        .clickable(enabled = !isFetching) { fetchProductData() }
                        .padding(horizontal = Spacing.md, vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isFetching) {
                        CircularProgressIndicator(
                            color = Primary,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = "Fetch",
                            tint = White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            Text(
                text = "Tap the download icon to auto-fill from URL",
                style = MaterialTheme.typography.bodySmall,
                color = textMutedColor
            )
            
            Spacer(modifier = Modifier.Companion.height(Spacing.lg))
            
            // Image Upload Section
            Text(
                text = "Product Image",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Spacer(modifier = Modifier.Companion.height(Spacing.sm))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(surfaceVariantColor)
                    .clickable { 
                        if (!isUploading) {
                            imagePickerLauncher.launch("image/*")
                        }
                    },
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
                        contentDescription = "Product image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Image",
                            tint = Primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.Companion.height(Spacing.sm))
                        Text("Tap to upload image", color = textSecondaryColor)
                    }
                }
            }
            
            Spacer(modifier = Modifier.Companion.height(Spacing.lg))
            
            // AR Model Upload Section
            Text("AR Model (.glb / .usdz)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = textColor)
            Text("Optional - Upload 3D model for AR view (GLB for Android, USDZ for iOS)", color = textSecondaryColor, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.Companion.height(Spacing.sm))
            
            val arModelPickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.OpenDocument()
            ) { uri: Uri? ->
                uri?.let { 
                    coroutineScope.launch {
                        isUploadingAr = true
                        try {
                            val inputStream = context.contentResolver.openInputStream(it)
                            val bytes = inputStream?.readBytes() ?: byteArrayOf()
                            inputStream?.close()
                            
                            // Get the original file extension
                            val originalFileName = context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                                cursor.moveToFirst()
                                if (nameIndex >= 0) cursor.getString(nameIndex) else null
                            }
                            val extension = originalFileName?.substringAfterLast('.', "glb") ?: "glb"
                            
                            val requestBody = bytes.toRequestBody("application/octet-stream".toMediaTypeOrNull())
                            val filename = "model_${System.currentTimeMillis()}.$extension"
                            val part = MultipartBody.Part.createFormData("model", filename, requestBody)
                            
                            // Use Node.js backend for .usdz files (stores in public/models)
                            val response = RetrofitClient.apiService.uploadModel(part)
                            // Store only the relative path - full URL constructed when needed
                            arModel = response.url
                            Toast.makeText(context, "AR Model uploaded!", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Log.e("AddProduct", "AR Model upload failed", e)
                            Toast.makeText(context, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        } finally {
                            isUploadingAr = false
                        }
                    }
                }
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(surfaceVariantColor)
                    .clickable { 
                        if (!isUploadingAr) {
                            arModelPickerLauncher.launch(arrayOf("*/*"))
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isUploadingAr) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(color = Primary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.Companion.width(Spacing.sm))
                        Text("Uploading AR model...", color = textSecondaryColor)
                    }
                } else if (arModel.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.ViewInAr,
                            contentDescription = "AR Model",
                            tint = Primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.Companion.width(Spacing.sm))
                        Text("AR Model uploaded ✓", color = Primary, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.ViewInAr,
                            contentDescription = "Add AR Model",
                            tint = textSecondaryColor,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.Companion.width(Spacing.sm))
                        Text("Tap to upload .glb or .usdz file", color = textSecondaryColor)
                    }
                }
            }
            
            Spacer(modifier = Modifier.Companion.height(Spacing.xl))
            
            // Title
            Text("Title *", color = textSecondaryColor, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.Companion.height(Spacing.xs))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Enter product title") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = textMutedColor
                )
            )
            
            Spacer(modifier = Modifier.Companion.height(Spacing.lg))
            
            // Price Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Price *", color = textSecondaryColor, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.Companion.height(Spacing.xs))
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it.filter { c -> c.isDigit() } },
                        placeholder = { Text("₹ Price") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = textMutedColor
                        )
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Original Price", color = textSecondaryColor, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.Companion.height(Spacing.xs))
                    OutlinedTextField(
                        value = originalPrice,
                        onValueChange = { originalPrice = it.filter { c -> c.isDigit() } },
                        placeholder = { Text("₹ MRP") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = textMutedColor
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.Companion.height(Spacing.lg))
            
            // Source
            Text("Source", color = textSecondaryColor, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.Companion.height(Spacing.xs))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                listOf("amazon", "flipkart", "other").forEach { s ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (source == s) Primary else surfaceVariantColor)
                            .clickable { source = s }
                            .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
                    ) {
                        Text(
                            text = s.replaceFirstChar { it.uppercase() },
                            color = if (source == s) White else textSecondaryColor
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.Companion.height(Spacing.lg))
            
            // Category Dropdown
            Text("Category *", color = textSecondaryColor, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.Companion.height(Spacing.xs))
            Box {
                OutlinedTextField(
                    value = categories.find { it.id == selectedCategoryId }?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text(if (isLoadingCategories) "Loading..." else "Select category") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { if (!isLoadingCategories) isCategoryDropdownExpanded = true },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = textMutedColor
                    ),
                    trailingIcon = {
                        Icon(
                            imageVector = if (isCategoryDropdownExpanded) 
                                Icons.Default.KeyboardArrowUp 
                            else 
                                Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand",
                            tint = textSecondaryColor,
                            modifier = Modifier.clickable { 
                                if (!isLoadingCategories) isCategoryDropdownExpanded = !isCategoryDropdownExpanded 
                            }
                        )
                    }
                )

                DropdownMenu(
                    expanded = isCategoryDropdownExpanded,
                    onDismissRequest = { isCategoryDropdownExpanded = false },
                    modifier = Modifier.background(surfaceColor)
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name, color = textColor) },
                            onClick = {
                                selectedCategoryId = category.id
                                isCategoryDropdownExpanded = false
                            }
                        )
                    }
                }
            }
            
            // Display Specs if available
            if (!specs.isNullOrEmpty()) {
                Spacer(modifier = Modifier.Companion.height(Spacing.xl))
                
                Text(
                    text = "📋 Fetched Specifications",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Spacer(modifier = Modifier.Companion.height(Spacing.sm))
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(surfaceVariantColor)
                        .padding(Spacing.md)
                ) {
                    specs?.entries?.forEachIndexed { index, (key, value) ->
                        if (index > 0) {
                            Spacer(modifier = Modifier.Companion.height(Spacing.xs))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = key,
                                style = MaterialTheme.typography.bodySmall,
                                color = textSecondaryColor,
                                modifier = Modifier.weight(0.4f)
                            )
                            Text(
                                text = value,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = textColor,
                                modifier = Modifier.weight(0.6f)
                            )
                        }
                    }
                }
            }
            
            // Display Key Features if available
            if (keyFeatures.isNotEmpty()) {
                Spacer(modifier = Modifier.Companion.height(Spacing.lg))
                
                Text(
                    text = "✨ Generated Key Features",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Spacer(modifier = Modifier.Companion.height(Spacing.sm))
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .background(Primary.copy(alpha = 0.05f))
                        .padding(Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    keyFeatures.forEach { feature ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            Icon(
                                imageVector = getIconVector(feature.icon),
                                contentDescription = feature.icon,
                                tint = Primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = feature.label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = textColor
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.Companion.height(Spacing.xxl))
            
            // Submit Button
            DealButton(
                text = if (isSubmitting) "Adding..." else "Add Product",
                onClick = { submitProduct() },
                enabled = !isSubmitting && title.isNotBlank() && price.isNotBlank() && url.isNotBlank() && selectedCategoryId != null,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
