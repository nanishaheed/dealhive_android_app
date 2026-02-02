package com.simats.dealhive.ui.screens.compare

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompareArrows
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.simats.dealhive.LocalIsDarkMode
import com.simats.dealhive.data.api.RetrofitClient
import com.simats.dealhive.data.model.Product
import com.simats.dealhive.ui.components.DealButton
import com.simats.dealhive.ui.components.DealHeader
import com.simats.dealhive.ui.theme.Primary
import com.simats.dealhive.ui.theme.Spacing
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CompareDbProductsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCompare: (Product, Product) -> Unit
) {
    val priceFormat = NumberFormat.getNumberInstance(Locale("en", "IN"))
    
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedProduct1 by remember { mutableStateOf<Product?>(null) }
    var selectedProduct2 by remember { mutableStateOf<Product?>(null) }
    
    val isDarkMode = LocalIsDarkMode.current
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    
    // Fetch products from database
    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.phpApiService.getProducts(limit = 50)
            products = response.products
        } catch (e: Exception) {
            Log.e("CompareDb", "Failed to load products", e)
        } finally {
            isLoading = false
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        DealHeader(
            title = "Compare Products",
            showBack = true,
            onBackClick = onNavigateBack
        )
        
        // Selected products preview
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(surfaceColor)
                .padding(Spacing.lg),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product 1 slot
            SelectedProductSlot(
                product = selectedProduct1,
                label = "Product 1",
                onClear = { selectedProduct1 = null }
            )
            
            Icon(
                imageVector = Icons.Default.CompareArrows,
                contentDescription = "Compare",
                tint = Primary,
                modifier = Modifier.size(32.dp)
            )
            
            // Product 2 slot
            SelectedProductSlot(
                product = selectedProduct2,
                label = "Product 2",
                onClear = { selectedProduct2 = null }
            )
        }
        
        Spacer(modifier = Modifier.Companion.height(Spacing.sm))
        
        // Compare button
        if (selectedProduct1 != null && selectedProduct2 != null) {
            DealButton(
                text = "Compare Now",
                onClick = { onNavigateToCompare(selectedProduct1!!, selectedProduct2!!) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg)
            )
            Spacer(modifier = Modifier.Companion.height(Spacing.sm))
        }
        
        // Products list
        Text(
            text = "Select two products to compare",
            style = MaterialTheme.typography.bodyMedium,
            color = textSecondaryColor,
            modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm)
        )
        
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else if (products.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No products available", color = textSecondaryColor)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                items(products) { product ->
                    val isSelected = product.id == selectedProduct1?.id || product.id == selectedProduct2?.id
                    
                    ProductSelectItem(
                        product = product,
                        isSelected = isSelected,
                        priceFormat = priceFormat,
                        onClick = {
                            when {
                                product.id == selectedProduct1?.id -> selectedProduct1 = null
                                product.id == selectedProduct2?.id -> selectedProduct2 = null
                                selectedProduct1 == null -> selectedProduct1 = product
                                selectedProduct2 == null -> selectedProduct2 = product
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectedProductSlot(
    product: Product?,
    label: String,
    onClear: () -> Unit
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onSurface
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (product != null) surfaceVariantColor else backgroundColor)
            .border(
                width = 2.dp,
                color = if (product != null) Primary else surfaceVariantColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { if (product != null) onClear() },
        contentAlignment = Alignment.Center
    ) {
        if (product != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AsyncImage(
                    model = product.image,
                    contentDescription = product.title,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit
                )
                Text(
                    text = product.title.take(15) + if (product.title.length > 15) "..." else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor,
                    maxLines = 1
                )
            }
        } else {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = textSecondaryColor
            )
        }
    }
}

@Composable
private fun ProductSelectItem(
    product: Product,
    isSelected: Boolean,
    priceFormat: NumberFormat,
    onClick: () -> Unit
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) Primary.copy(alpha = 0.2f) else surfaceColor)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) Primary else surfaceColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Product image
        AsyncImage(
            model = product.image,
            contentDescription = product.title,
            modifier = Modifier
                .size(70.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White),
            contentScale = ContentScale.Fit
        )
        
        Spacer(modifier = Modifier.Companion.width(Spacing.md))
        
        // Product info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = product.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = textColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "₹${priceFormat.format(product.price)}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Primary
            )
            Text(
                text = product.source.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelSmall,
                color = textSecondaryColor
            )
        }
        
        // Selection indicator
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Selected",
                tint = Primary,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
