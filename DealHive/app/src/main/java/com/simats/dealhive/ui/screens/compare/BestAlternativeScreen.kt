package com.simats.dealhive.ui.screens.compare

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.simats.dealhive.LocalIsDarkMode
import com.simats.dealhive.ui.components.DealButton
import com.simats.dealhive.ui.components.DealHeader
import com.simats.dealhive.ui.theme.Primary
import com.simats.dealhive.ui.theme.Spacing
import com.simats.dealhive.ui.theme.Success
import com.simats.dealhive.ui.theme.Warning
import com.simats.dealhive.ui.theme.White
import java.text.NumberFormat
import java.util.Locale

data class AlternativeProduct(
    val title: String,
    val price: Int,
    val rating: Float,
    val image: String,
    val reason: String
)

private val alternatives = listOf(
    AlternativeProduct(
        title = "Google Pixel 8 Pro",
        price = 99999,
        rating = 4.6f,
        image = "https://m.media-amazon.com/images/I/71GFdU1bMML._SX679_.jpg",
        reason = "Best camera for the price"
    ),
    AlternativeProduct(
        title = "OnePlus 12",
        price = 64999,
        rating = 4.4f,
        image = "https://m.media-amazon.com/images/I/71K85IMj1YL._SX679_.jpg",
        reason = "Fastest charging speed"
    ),
    AlternativeProduct(
        title = "Xiaomi 14 Ultra",
        price = 89999,
        rating = 4.3f,
        image = "https://m.media-amazon.com/images/I/71Ks0Z4KIPL._SX679_.jpg",
        reason = "Best value for specs"
    )
)

@Composable
fun BestAlternativeScreen(
    onNavigateBack: () -> Unit
) {
    val priceFormat = NumberFormat.getNumberInstance(Locale("en", "IN"))
    val isDarkMode = LocalIsDarkMode.current
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceColor = MaterialTheme.colorScheme.surface
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        DealHeader(
            title = "Alternatives",
            showBack = true,
            onBackClick = onNavigateBack
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.xl)
        ) {
            // AI Recommendation Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Primary.copy(alpha = 0.15f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "AI RECOMMENDATIONS",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }
            
            Spacer(modifier = Modifier.Companion.height(Spacing.md))
            
            Text(
                text = "Better Alternatives",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            
            Text(
                text = "Based on your comparison, here are some products that might suit you better.",
                style = MaterialTheme.typography.bodyLarge,
                color = textSecondaryColor,
                modifier = Modifier.padding(top = Spacing.xs)
            )
            
            Spacer(modifier = Modifier.Companion.height(Spacing.xxl))
            
            // Alternative products
            alternatives.forEachIndexed { index, product ->
                AlternativeCard(
                    product = product,
                    rank = index + 1,
                    priceFormat = priceFormat
                )
                
                if (index < alternatives.size - 1) {
                    Spacer(modifier = Modifier.Companion.height(Spacing.md))
                }
            }
            
            Spacer(modifier = Modifier.Companion.height(Spacing.xl))
            
            // Compare all button
            DealButton(
                text = "Compare All Alternatives",
                onClick = { /* Navigate to multi-comparison */ }
            )
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun AlternativeCard(
    product: AlternativeProduct,
    rank: Int,
    priceFormat: NumberFormat
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(surfaceColor)
            .padding(Spacing.lg)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank badge
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (rank == 1) Success.copy(alpha = 0.2f)
                        else Primary.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#$rank",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (rank == 1) Success else Primary
                )
            }
            
            Spacer(modifier = Modifier.Companion.width(Spacing.md))
            
            // Product Image
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(White)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = product.image,
                    contentDescription = product.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
            
            Spacer(modifier = Modifier.Companion.width(Spacing.md))
            
            // Product Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                
                Text(
                    text = product.reason,
                    style = MaterialTheme.typography.bodySmall,
                    color = textSecondaryColor
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "₹${priceFormat.format(product.price)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                    
                    Spacer(modifier = Modifier.Companion.width(Spacing.md))
                    
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Warning,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${product.rating}",
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondaryColor
                    )
                }
            }
        }
    }
}
