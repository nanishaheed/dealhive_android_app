package com.simats.dealhive.ui.screens.home

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Tablet
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.simats.dealhive.data.api.RetrofitClient
import com.simats.dealhive.data.model.CategoryDto
import com.simats.dealhive.ui.components.DealButton
import com.simats.dealhive.ui.components.DealHeader
import com.simats.dealhive.ui.theme.Primary
import com.simats.dealhive.ui.theme.Spacing

data class Category(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val count: String
)

// Map category slugs to icons and colors
private fun getCategoryIcon(slug: String): ImageVector {
    return when (slug.lowercase()) {
        "smartphones", "mobiles" -> Icons.Default.Smartphone
        "laptops" -> Icons.Default.Laptop
        "televisions", "tvs" -> Icons.Default.Tv
        "headphones" -> Icons.Default.Headphones
        "tablets" -> Icons.Default.Tablet
        "gaming" -> Icons.Default.Gamepad
        "cameras" -> Icons.Default.PhotoCamera
        "smartwatches" -> Icons.Default.Watch
        else -> Icons.Default.Category
    }
}

private fun getCategoryColor(slug: String): Color {
    return when (slug.lowercase()) {
        "smartphones", "mobiles" -> Color(0xFF3B82F6)
        "laptops" -> Color(0xFF22C55E)
        "televisions", "tvs" -> Color(0xFFA855F7)
        "headphones" -> Color(0xFFF97316)
        "tablets" -> Color(0xFFEAB308)
        "gaming" -> Color(0xFFEF4444)
        "cameras" -> Color(0xFF06B6D4)
        "smartwatches" -> Color(0xFF8B5CF6)
        else -> Color(0xFF64748B)
    }
}

@Composable
fun CategoriesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProductList: (String) -> Unit
) {
    var categories by remember { mutableStateOf<List<CategoryDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    // Fetch categories from backend
    LaunchedEffect(Unit) {
        isLoading = true
        error = null
        try {
            val response = RetrofitClient.phpApiService.getCategories()
            categories = response.categories
            Log.d("CategoriesScreen", "Loaded ${categories.size} categories from backend")
        } catch (e: Exception) {
            error = "Failed to load categories: ${e.message}"
            Log.e("CategoriesScreen", "Error loading categories", e)
        } finally {
            isLoading = false
        }
    }
    
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        DealHeader(
            title = "Categories",
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
                            text = "Loading categories...",
                            color = textSecondaryColor
                        )
                    }
                }
            }
            
            error != null && categories.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.Companion.padding(Spacing.xxl)
                    ) {
                        Text(
                            text = "Could not load categories",
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
                        text = "Browse by Category",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    
                    Text(
                        text = "Find the best deals across popular categories",
                        style = MaterialTheme.typography.bodyLarge,
                        color = textSecondaryColor,
                        modifier = Modifier.padding(top = Spacing.xs)
                    )
                    
                    Spacer(modifier = Modifier.Companion.height(Spacing.xl))
                    
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                        verticalArrangement = Arrangement.spacedBy(Spacing.md),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        items(categories) { categoryDto ->
                            val category = Category(
                                id = categoryDto.slug,
                                name = categoryDto.name,
                                icon = getCategoryIcon(categoryDto.slug),
                                color = getCategoryColor(categoryDto.slug),
                                count = "${categoryDto.productCount} items"
                            )
                            CategoryCard(
                                category = category,
                                onClick = { onNavigateToProductList(category.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryCard(
    category: Category,
    onClick: () -> Unit
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(surfaceColor)
            .clickable { onClick() }
            .padding(Spacing.lg)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(category.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = category.name,
                    tint = category.color,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Column {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = category.count,
                    style = MaterialTheme.typography.bodySmall,
                    color = textSecondaryColor
                )
            }
        }
    }
}

