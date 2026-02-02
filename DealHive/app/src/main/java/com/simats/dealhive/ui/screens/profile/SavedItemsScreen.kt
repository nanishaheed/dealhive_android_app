package com.simats.dealhive.ui.screens.profile

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.simats.dealhive.LocalIsDarkMode
import com.simats.dealhive.data.api.RetrofitClient
import com.simats.dealhive.data.model.SavedItemDto
import com.simats.dealhive.data.repository.AuthRepository
import com.simats.dealhive.ui.components.DealButton
import com.simats.dealhive.ui.components.DealHeader
import com.simats.dealhive.ui.theme.Primary
import com.simats.dealhive.ui.theme.Spacing
import com.simats.dealhive.ui.theme.Warning
import com.simats.dealhive.ui.theme.White
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SavedItemsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    val context = LocalContext.current
    val priceFormat = NumberFormat.getNumberInstance(Locale("en", "IN"))
    val coroutineScope = rememberCoroutineScope()
    val authRepository = remember { AuthRepository.Companion.getInstance(context) }
    
    val isDarkMode = LocalIsDarkMode.current
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textMutedColor = MaterialTheme.colorScheme.outline
    
    // Get user ID from authenticated session
    val currentUserId = authRepository.getCurrentUserId()
    
    var savedItems by remember { mutableStateOf<List<SavedItemDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    // Fetch saved items from backend
    LaunchedEffect(currentUserId) {
        isLoading = true
        error = null
        try {
            val response = RetrofitClient.phpApiService.getSavedItems(currentUserId)
            savedItems = response.savedItems
            Log.d("SavedItemsScreen", "Loaded ${savedItems.size} saved items from backend")
        } catch (e: Exception) {
            error = "Failed to load saved items: ${e.message}"
            Log.e("SavedItemsScreen", "Error loading saved items", e)
        } finally {
            isLoading = false
        }
    }
    
    // Function to remove item from saved
    fun removeFromSaved(item: SavedItemDto) {
        coroutineScope.launch {
            try {
                RetrofitClient.phpApiService.removeSavedItem(currentUserId, item.productId)
                savedItems = savedItems.filter { it.id != item.id }
                Log.d("SavedItemsScreen", "Removed item ${item.productId} from saved")
            } catch (e: Exception) {
                Log.e("SavedItemsScreen", "Error removing saved item", e)
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        DealHeader(
            title = "Saved Items",
            showBack = true,
            onBackClick = onNavigateBack
        )
        
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Primary)
                        Spacer(modifier = Modifier.Companion.height(Spacing.md))
                        Text(
                            text = "Loading saved items...",
                            color = textSecondaryColor
                        )
                    }
                }
            }
            
            error != null && savedItems.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.Companion.padding(Spacing.xxl)
                    ) {
                        Text(
                            text = "Could not load saved items",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.Companion.height(Spacing.xs))
                        Text(
                            text = error ?: "Unknown error",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textSecondaryColor,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.Companion.height(Spacing.xl))
                        DealButton(
                            text = "Go Back",
                            onClick = onNavigateBack
                        )
                    }
                }
            }
            
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = Spacing.xl)
                ) {
                    Spacer(modifier = Modifier.Companion.height(Spacing.lg))
                    
                    Text(
                        text = "Your Bookmarks",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    
                    Text(
                        text = "${savedItems.size} products saved",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textSecondaryColor,
                        modifier = Modifier.padding(top = Spacing.xs)
                    )
                    
                    Spacer(modifier = Modifier.Companion.height(Spacing.xl))
                    
                    if (savedItems.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BookmarkBorder,
                                    contentDescription = null,
                                    tint = textMutedColor,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.Companion.height(Spacing.md))
                                Text(
                                    text = "No saved items",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = textMutedColor
                                )
                                Spacer(modifier = Modifier.Companion.height(Spacing.xs))
                                Text(
                                    text = "Start saving products to compare later!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = textSecondaryColor
                                )
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                            verticalArrangement = Arrangement.spacedBy(Spacing.md),
                            contentPadding = PaddingValues(bottom = 100.dp)
                        ) {
                            items(savedItems) { item ->
                                SavedProductCard(
                                    item = item,
                                    priceFormat = priceFormat,
                                    onClick = { onNavigateToDetail(item.productId.toString()) },
                                    onRemove = { removeFromSaved(item) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SavedProductCard(
    item: SavedItemDto,
    priceFormat: NumberFormat,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(surfaceColor)
            .clickable { onClick() }
    ) {
        Column {
            // Image with bookmark
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(White) // Keep white for product background visibility
                    .padding(Spacing.md),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = item.image,
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = "Remove bookmark",
                        tint = Primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Column(
                modifier = Modifier.Companion.padding(Spacing.md)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "₹${priceFormat.format(item.price)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    if (item.rating != null) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Warning,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${item.rating}",
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondaryColor
                        )
                    }
                }
            }
        }
    }
}

