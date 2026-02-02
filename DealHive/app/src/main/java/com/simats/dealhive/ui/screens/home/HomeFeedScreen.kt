package com.simats.dealhive.ui.screens.home

import android.util.Log
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.simats.dealhive.LocalIsDarkMode
import com.simats.dealhive.data.repository.AuthRepository
import com.simats.dealhive.data.api.RetrofitClient
import com.simats.dealhive.data.model.Product
import com.simats.dealhive.ui.theme.Primary
import com.simats.dealhive.ui.theme.Spacing
import com.simats.dealhive.ui.theme.White

@Composable
fun HomeFeedScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToCompare: () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSavedItems: () -> Unit,
    onNavigateToProduct: (String) -> Unit = {},
    onNavigateToCompareDb: () -> Unit = {}
) {
    val context = LocalContext.current
    val authRepository = remember { AuthRepository.Companion.getInstance(context) }
    val userAvatar = authRepository.getCurrentUserAvatar()
    val isDarkMode = LocalIsDarkMode.current
    
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Custom Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = Spacing.xl, vertical = Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Deal Hive",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Primary.copy(alpha = 0.12f))
                    .clickable { onNavigateToProfile() },
                contentAlignment = Alignment.Center
            ) {
                if (!userAvatar.isNullOrBlank()) {
                    AsyncImage(
                        model = userAvatar,
                        contentDescription = "Profile",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = Primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.xl)
        ) {
            // Hero Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Primary)
                    .padding(Spacing.xl)
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CompareArrows,
                            contentDescription = null,
                            tint = White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.Companion.height(Spacing.md))
                    
                    Text(
                        text = "Start a New Comparison",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                    
                    Text(
                        text = "Compare two products head-to-head and get an instant AI analysis.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    
                    Spacer(modifier = Modifier.Companion.height(Spacing.md))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(White)
                            .clickable { onNavigateToCompare() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Compare Products",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }
                }
            }
            
            // Action Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    ActionItem(
                        icon = Icons.Default.List,
                        label = "Browse Categories",
                        iconColor = Primary,
                        onClick = onNavigateToCategories
                    )
                    ActionItem(
                        icon = Icons.Default.Bookmark,
                        label = "Saved Items",
                        iconColor = Color(0xFFA855F7),
                        onClick = onNavigateToSavedItems
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    ActionItem(
                        icon = Icons.Default.History,
                        label = "History",
                        iconColor = Color(0xFFF97316),
                        onClick = onNavigateToHistory
                    )
                    ActionItem(
                        icon = Icons.Default.CompareArrows,
                        label = "Compare Products",
                        iconColor = Primary,
                        onClick = onNavigateToCompareDb
                    )
                }
            }
            
            // Featured Products Section
            FeaturedProductsSection(onNavigateToProduct)
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun FeaturedProductsSection(onNavigateToProduct: (String) -> Unit) {
    val productsState = remember { mutableStateOf<List<Product>>(emptyList()) }
    val isLoadingState = remember { mutableStateOf(true) }
    
    val products = productsState.value
    val isLoading = isLoadingState.value
    
    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.phpApiService.getProducts(limit = 10)
            productsState.value = response.products
        } catch (e: Exception) {
            Log.e("HomeFeed", "Failed to load products", e)
        } finally {
            isLoadingState.value = false
        }
    }
    
    val textColor = MaterialTheme.colorScheme.onBackground
    val surfaceColor = MaterialTheme.colorScheme.surface
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    Column {
        Text(
            text = "Featured Products",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        
        Spacer(modifier = Modifier.Companion.height(Spacing.md))
        
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else if (products.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(surfaceColor)
                    .padding(Spacing.lg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No products yet",
                    color = textSecondaryColor
                )
            }
        } else {
            for (product in products) {
                FeaturedProductItem(product, onNavigateToProduct)
                Spacer(modifier = Modifier.Companion.height(Spacing.md))
            }
        }
    }
}   

@Composable
private fun FeaturedProductItem(
    product: Product,
    onNavigateToProduct: (String) -> Unit
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurface
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textMutedColor = MaterialTheme.colorScheme.outline

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(surfaceColor)
            .clickable { onNavigateToProduct(product.id) }
            .padding(Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Product Image
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(surfaceVariantColor),
            contentAlignment = Alignment.Center
        ) {
            if (!product.image.isNullOrBlank()) {
                AsyncImage(
                    model = product.image,
                    contentDescription = product.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Smartphone,
                    contentDescription = null,
                    tint = textSecondaryColor,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.Companion.width(Spacing.md))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = product.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = textColor,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                Text(
                    text = "₹${product.price}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
                if (product.originalPrice != null && product.originalPrice > product.price) {
                    Text(
                        text = "₹${product.originalPrice}",
                        style = MaterialTheme.typography.bodySmall,
                        color = textMutedColor,
                        textDecoration = TextDecoration.LineThrough
                    )
                }
            }
            Text(
                text = product.source.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodySmall,
                color = textSecondaryColor
            )
        }
    }
}

@Composable
private fun ActionItem(
    icon: ImageVector,
    label: String,
    iconColor: Color,
    onClick: () -> Unit
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(surfaceColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.Companion.height(Spacing.md))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = textSecondaryColor
            )
        }
    }
}

@Composable
private fun RecentItem(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurface
    val textMutedColor = MaterialTheme.colorScheme.outline

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(surfaceColor)
            .padding(Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(surfaceVariantColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textMutedColor,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.Companion.width(Spacing.md))
        
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = textMutedColor
            )
        }
    }
}
