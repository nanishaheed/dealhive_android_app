package com.simats.dealhive.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.simats.dealhive.LocalIsDarkMode
import com.simats.dealhive.data.model.KeyFeature
import com.simats.dealhive.data.model.Product
import com.simats.dealhive.ui.theme.Primary
import com.simats.dealhive.ui.theme.Spacing
import com.simats.dealhive.ui.theme.Warning
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit,
    onCompare: (() -> Unit)? = null,
    onViewAR: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val priceFormat = NumberFormat.getNumberInstance(Locale("en", "IN"))
    val context = LocalContext.current
    val isDarkMode = LocalIsDarkMode.current
    
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textMutedColor = MaterialTheme.colorScheme.outline
    val whiteColor = Color.White

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(surfaceColor)
            .clickable { onClick() }
            .padding(Spacing.lg)
    ) {
        Column {
            // Header Row: Image + Basic Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Product Image with improved loading
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isDarkMode) surfaceVariantColor else whiteColor)
                        .padding(Spacing.sm),
                    contentAlignment = Alignment.Center
                ) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(product.image)
                            .crossfade(true)
                            .build(),
                        contentDescription = product.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                        loading = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(if (isDarkMode) surfaceVariantColor else whiteColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "...",
                                    color = textSecondaryColor
                                )
                            }
                        },
                        error = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(if (isDarkMode) surfaceVariantColor else whiteColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "Product",
                                    tint = textSecondaryColor,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                    )
                }
                
                Spacer(modifier = Modifier.Companion.width(Spacing.lg))
                
                // Product Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.title,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = onSurfaceColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    product.specs?.get("Display")?.let { display ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = display,
                            style = MaterialTheme.typography.bodyMedium,
                            color = textSecondaryColor
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Rating
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Warning,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = product.rating.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = textSecondaryColor
                        )
                    }
                }
            }
            
            // Features Grid
            product.keyFeatures?.takeIf { it.isNotEmpty() }?.let { features ->
                Spacer(modifier = Modifier.Companion.height(Spacing.xl))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    features.take(4).chunked(2).forEach { chunk ->
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            chunk.forEach { feature ->
                                FeatureItem(feature, textSecondaryColor)
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.Companion.height(Spacing.xl))
            
            // Footer: Price + Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "₹${priceFormat.format(product.price)}",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    color = onSurfaceColor
                )
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // AR Button
                    onViewAR?.let { arCallback ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Primary,
                                            Primary.copy(alpha = 0.8f)
                                        )
                                    )
                                )
                                .clickable { arCallback() }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                // AR Badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(whiteColor.copy(alpha = 0.2f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "AR",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = whiteColor
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "View in AR",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = whiteColor
                                )
                            }
                        }
                    }
                    
                    // Compare Button
                    onCompare?.let {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Primary)
                                .clickable { it() }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "Add to Comparison",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = whiteColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureItem(feature: KeyFeature, textColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = getFeatureIcon(feature.icon),
            contentDescription = feature.label,
            tint = textColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = feature.label,
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun getFeatureIcon(iconName: String): ImageVector {
    return when (iconName.lowercase()) {
        "monitor" -> Icons.Default.Monitor
        "cpu" -> Icons.Default.Memory
        "camera" -> Icons.Default.Camera
        "zap" -> Icons.Outlined.Bolt
        "layers" -> Icons.Default.Memory
        "smartphone" -> Icons.Default.Phone
        "harddrive" -> Icons.Default.Save
        "activity" -> Icons.Default.Speed
        else -> Icons.Outlined.Inventory2
    }
}

