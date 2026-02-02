package com.simats.dealhive.ui.screens.home

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.simats.dealhive.LocalIsDarkMode
import com.simats.dealhive.data.model.Product
import com.simats.dealhive.data.api.RetrofitClient
import com.simats.dealhive.data.model.SaveItemRequest
import com.simats.dealhive.data.repository.AuthRepository
import com.simats.dealhive.ui.components.DealButton
import com.simats.dealhive.ui.components.DealHeader
import com.simats.dealhive.ui.theme.Primary
import com.simats.dealhive.ui.theme.Spacing
import com.simats.dealhive.ui.theme.Warning
import com.simats.dealhive.ui.theme.White
import com.simats.dealhive.util.ARModel
import com.simats.dealhive.util.ARUtils
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ProductDetailScreen(
    product: Product?,
    onNavigateBack: () -> Unit
) {
    val priceFormat = NumberFormat.getNumberInstance(Locale("en", "IN"))
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val authRepository = AuthRepository.Companion.getInstance(context)
    val userId = authRepository.getCurrentUserId()
    
    val isDarkMode = LocalIsDarkMode.current
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val textMutedColor = MaterialTheme.colorScheme.outline
    
    // Bookmark state
    var isBookmarked by remember { mutableStateOf(false) }
    var isBookmarkLoading by remember { mutableStateOf(false) }
    
    // Check if product is bookmarked
    LaunchedEffect(product?.id) {
        if (product != null && userId != null) {
            try {
                val savedItems = RetrofitClient.phpApiService.getSavedItems(userId)
                isBookmarked = savedItems.savedItems.any { it.productId.toString() == product.id }
            } catch (e: Exception) {
                Log.e("ProductDetail", "Failed to check bookmark", e)
            }
        }
    }
    
    fun toggleBookmark() {
        if (product == null || userId == null) return
        
        coroutineScope.launch {
            isBookmarkLoading = true
            try {
                if (isBookmarked) {
                    RetrofitClient.phpApiService.removeSavedItem(userId, product.id.toIntOrNull() ?: 0)
                    isBookmarked = false
                    Toast.makeText(context, "Removed from saved items", Toast.LENGTH_SHORT).show()
                } else {
                    RetrofitClient.phpApiService.saveItem(
                        SaveItemRequest(userId, product.id.toIntOrNull() ?: 0)
                    )
                    isBookmarked = true
                    Toast.makeText(context, "Added to saved items", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isBookmarkLoading = false
            }
        }
    }
    
    // Show loading if product is null
    if (product == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Primary)
                Spacer(modifier = Modifier.Companion.height(Spacing.md))
                Text(
                    text = "Loading product...",
                    color = textColor,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        return
    }
    
    // Check if AR is available for this product
    // Priority: stored ar_model URL > ARUtils keyword lookup (fallback for scraped products)
    val storedArModel = product.arModel
    val fallbackArModel = ARUtils.getARModelForProduct(
        product.title,
        product.specs?.get("Brand")
    )
    val hasArModel = !storedArModel.isNullOrBlank() || fallbackArModel != null
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        DealHeader(
            title = "Product Details",
            showBack = true,
            onBackClick = onNavigateBack,
            rightContent = {
                IconButton(onClick = { toggleBookmark() }) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = if (isBookmarked) "Remove bookmark" else "Add bookmark",
                        tint = if (isBookmarked) Primary else textColor
                    )
                }
            }
        )
        
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.xl)
        ) {
            Spacer(modifier = Modifier.Companion.height(Spacing.lg))
            
            // Product Image with AR Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(White),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = product.image,
                    contentDescription = product.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Spacing.xl),
                    contentScale = ContentScale.Fit
                )
                
                // AR Button - only show if AR model exists
                if (hasArModel) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                            .shadow(4.dp, RoundedCornerShape(24.dp))
                            .clip(RoundedCornerShape(24.dp))
                            .background(surfaceColor.copy(alpha = 0.95f))
                            .clickable {
                                // Use stored AR model if available, otherwise use fallback
                                if (!storedArModel.isNullOrBlank()) {
                                    // Construct full URL from relative path stored in DB
                                    val fullModelUrl = RetrofitClient.getModelUrl(storedArModel)
                                    val model = ARModel(glb = fullModelUrl)
                                    ARUtils.openARViewer(context, model, product.title)
                                } else if (fallbackArModel != null) {
                                    ARUtils.openARViewer(context, fallbackArModel, product.title)
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ViewInAr,
                                contentDescription = "View in AR",
                                tint = Primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "View in AR",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.Companion.height(Spacing.xl))
            
            // Title
            Text(
                text = product.title,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            
            Spacer(modifier = Modifier.Companion.height(Spacing.sm))
            
            // Rating
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Rating",
                    tint = Warning,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${product.rating}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "(${product.reviews} reviews)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textSecondaryColor
                )
            }
            
            Spacer(modifier = Modifier.Companion.height(Spacing.lg))
            
            // Price
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "₹${priceFormat.format(product.price)}",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Black,
                    color = textColor
                )
                product.originalPrice?.let { original ->
                    Spacer(modifier = Modifier.Companion.width(Spacing.md))
                    Text(
                        text = "₹${priceFormat.format(original)}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = textMutedColor,
                        textDecoration = TextDecoration.LineThrough
                    )
                    Spacer(modifier = Modifier.Companion.width(Spacing.sm))
                    val discount = ((original - product.price) * 100 / original)
                    Text(
                        text = "${discount}% off",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF22C55E)
                    )
                }
            }
            
            Spacer(modifier = Modifier.Companion.height(Spacing.xl))
            
            // Specifications Section
            Text(
                text = "Specifications",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            
            Spacer(modifier = Modifier.Companion.height(Spacing.md))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(surfaceColor)
            ) {
                Column(modifier = Modifier.Companion.padding(Spacing.lg)) {
                    product.specs?.entries?.forEachIndexed { index, (key, value) ->
                        if (index > 0) {
                            Spacer(modifier = Modifier.Companion.height(Spacing.md))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(surfaceVariantColor)
                            )
                            Spacer(modifier = Modifier.Companion.height(Spacing.md))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = key,
                                style = MaterialTheme.typography.bodyMedium,
                                color = textSecondaryColor,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = value,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = textColor,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }
        
        // Bottom Buy Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(surfaceColor)
                .navigationBarsPadding()
                .padding(horizontal = Spacing.xl, vertical = Spacing.md),
            horizontalArrangement = Arrangement.Center
        ) {
            DealButton(
                text = "Buy on ${product.source.replaceFirstChar { it.uppercase() }}",
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(product.url))
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

